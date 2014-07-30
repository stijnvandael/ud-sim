package aggregator.agent.controllers.scheduler.objectives;

import java.util.ArrayList;

import utils.NullOutputStream;
import aggregator.modules.communication.FlexGraph;
import ilog.concert.*;
import ilog.cplex.*;
public class QSolver_old implements AggregatorObjective {
	
//	public static void main(String[] args) {
//		
//		// flexpower
//		double[] Pmax = {12000.0,12000.0,12000.0,12000.0,12000.0,12000.0,12000.0,12000.0,12000.0,12000.0,12000.0,0.0};
//		
//		// flexgraph
//		double[] Emin = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,200.0,1800.0,3000.0};
//		double[] Emax = {0.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0,3000.0};
//		
//		// bids
//		int firstBidQuarter = 0;
//		int[] bids = {2,2};
//		int bidSize = 12000;
//		
//		new QSolver().solve(Pmax, Emin, Emax, firstBidQuarter, bids, bidSize);
//	}
	
	private int quartersLeftInFirstBid;
	private int bidSize;
	private double[] powerPath;
	private double[] energyPath;
	private double[] regPower;
	private int[] bidsN;
	
	public boolean solve(long[] Pmax, long[] Preg, long[] Emin, long[] Emax, int quartersLeftInFirstBid, int[] bids, int bidSize, ArrayList<FlexGraph> flexGraphs) {
		return solve(convertToDoubles(Pmax), convertToDoubles(Preg), convertToDoubles(Emin), convertToDoubles(Emax), quartersLeftInFirstBid, bids, bidSize, flexGraphs);
	}
	
	public boolean solve(double[] Pmax, double[] PregMax, double[] Emin, double[] Emax, int quartersLeftInFirstBid, int[] bids, int bidSize, ArrayList<FlexGraph> flexGraphs) {
		double[] Pmin = new double[Pmax.length];
		// Pre-process data
		if((Pmin.length - quartersLeftInFirstBid) %4 != 0) {
			int extraQuarters = 4 - ((Pmin.length - quartersLeftInFirstBid)%4);
			Pmin = concat(Pmin, new double[extraQuarters]);
			Pmax = concat(Pmax, new double[extraQuarters]);
			PregMax = concat(PregMax, new double[extraQuarters]);
			double[] endE = new double[extraQuarters];
			for(int i = 0; i < endE.length; i++) {
				endE[i] = Emin[Emin.length-1];
			}
			Emin = concat(Emin, endE);
			Emax = concat(Emax, endE);
		}
		this.quartersLeftInFirstBid = quartersLeftInFirstBid;
		
		// Solve optimization problem
		try {
			IloCplex cplex = new IloCplex();
			
			//for all evs
			ArrayList<IloNumVar[]> flexpowers = new ArrayList<IloNumVar[]>();
			ArrayList<IloNumVar[]> flexenergies = new ArrayList<IloNumVar[]>();
			for(int i = 0; i < flexGraphs.size(); i++) {
				FlexGraph flexGraph = flexGraphs.get(i);
				double[] energyMin = convertToDoubles(flexGraph.getEnergyMin());
				double[] energyMax = convertToDoubles(flexGraph.getEnergyMax());
				double[] powerMax = convertToDoubles(flexGraph.getPowerMax());
				// charging power
				IloNumVar[] p = cplex.numVarArray(powerMax.length, new double[powerMax.length], powerMax);
				// path
				IloNumVar[] e = cplex.numVarArray(energyMin.length, energyMin, energyMax);
				for(int j = 0; j < p.length; j++) {
					cplex.addEq(cplex.diff(e[j+1],e[j]), cplex.prod(1.0/4, p[j]));
				}
				flexpowers.add(p);
				flexenergies.add(e);
			}
			
			//total charging power
			IloNumVar[] p = cplex.numVarArray(Pmin.length, Pmin, Pmax);
			
			//individual bin constraints
			for(int t = 0; t < p.length; t++) {
				ArrayList<IloNumVar> tempVars = new ArrayList<IloNumVar>();
				for(IloNumVar[] var: flexpowers) {
					if(var.length >= t + 1) {
						tempVars.add(var[t]);	
					}
				}
				IloNumVar[] tempVars_array = new IloNumVar[tempVars.size()];
				tempVars.toArray(tempVars_array);
				cplex.addEq(p[t], cplex.sum(tempVars_array));
			}
						
			// path
			IloNumVar[] e = cplex.numVarArray(Emin.length, Emin, Emax);
			for(int i = 0; i < p.length; i++) {
				cplex.addEq(cplex.diff(e[i+1],e[i]), cplex.prod(1.0/4, p[i]));
			}
			
			// Emin < path < Emax
			for(int i = 0; i < e.length; i++) {
				cplex.addGe(e[i], Emin[i]);
				cplex.addLe(e[i], Emax[i]);
			}
			
			// regulation power in each optimziation interval
			IloNumVar[] regPowerQ = cplex.numVarArray(Pmin.length, Pmin, PregMax);
			
			//calculate regulation power in each optimization interval
			for(int i = 0; i < regPowerQ.length; i++) {
				cplex.addEq(regPowerQ[i], cplex.diff(PregMax[i], p[i]));
			}
			
			// regulation bids in each hour
			// 1. max nr of bids per houd
			int hours = (int)Math.ceil(quartersLeftInFirstBid/4.0) + (int)Math.ceil((regPowerQ.length-quartersLeftInFirstBid)/4.0);
			int[] maxNrOfBidsPerHour = new int[hours];
			//init with max nr of bids per hour
			for(int i = 0; i < maxNrOfBidsPerHour.length ; i++) {
				maxNrOfBidsPerHour[i] = Integer.MAX_VALUE;
			}
			if(quartersLeftInFirstBid > 0) {
				maxNrOfBidsPerHour[0] = bids[0];
				try{
					maxNrOfBidsPerHour[1] = bids[1];
				}catch(IndexOutOfBoundsException ew) {
					System.out.print("Emin = ");
					printArray(Emin);
					
					System.out.print("Emax = ");
					printArray(Emax);
					
					System.out.print("Pmax = ");
					printArray(Pmax);
					
					System.out.println("FirstBidQuarter = " + quartersLeftInFirstBid);
					
					System.out.print("Bids = ");
					printArray(bids);		
					
					System.out.println("Bidsize = " + bidSize);
				}
			}else{
				maxNrOfBidsPerHour[0] = bids[1];
			}
			// 2. bids per hour
			IloIntVar[] bidsH = cplex.intVarArray(hours, new int[hours], maxNrOfBidsPerHour);
			//limit bids by provided regulation power
			for(int i = 0; i < regPowerQ.length; i++) {
				//convert Q to H
				int h = (int) Math.ceil(((i+1) - quartersLeftInFirstBid)/4.0);
				if(quartersLeftInFirstBid == 0) {
					h--;
				}
				//bidsH*bidSize <= regPowerQ[i]
				if(i > 0) {
					//reserve some extra regulation power
					cplex.addLe(cplex.prod(bidsH[h],bidSize), cplex.prod(0.95, regPowerQ[i]));	
				}else{
					cplex.addLe(cplex.prod(bidsH[h],bidSize), cplex.prod(1, regPowerQ[i]));	
				}
				//System.out.println("h = " + h + ", i=" + i);
			}

			// create model and solve it
			//cplex.addMaximize(cplex.scalProd(p,prices));
			double[] rewards = new double[bidsH.length];
			//init to rewards = 1 for all, but two first hours
			for(int i = 0; i < rewards.length; i++) {
				rewards[i] = 1;
			}
			rewards[0] = 100;
			if(rewards.length > 1) {
				rewards[1] = 100;	
			}
			IloLinearNumExpr maximization = cplex.scalProd(bidsH,rewards);
			IloObjective maxObjective = cplex.addMaximize(maximization);
			
			cplex.setOut(null);
			cplex.solve();
//			if(cplex.solve()) {
//				cplex.output().println("Solution status = " + cplex.getStatus());
//				cplex.output().println("Solution value = " + cplex.getObjValue());
//
//				System.out.print("Emin = ");
//				printArray(Emin);
//				
//				System.out.print("Emax = ");
//				printArray(Emax);
//				
//				System.out.print("Pmax = ");
//				printArray(Pmax);
//				
//				powerPath = cplex.getValues(p);
//				System.out.print("Power = ");
//				printArray(powerPath);
//
//				energyPath = cplex.getValues(e);
//				System.out.print("Energy = ");
//				printArray(energyPath);
//				
//				regPower = cplex.getValues(regPowerQ);
//				System.out.print("RegPower = ");
//				printArray(regPower);
//				
//				bidsN = new int[bidsH.length];
//				for(int i = 0; i < bidsN.length; i++) {
//					bidsN[i] = (int) Math.round(cplex.getValue(bidsH[i]));
//				}
				//shift bids right if no quarter left in current bid slot -- hack-ish
				if(quartersLeftInFirstBid == 0) {
					bidsN = concat(new int[1], bidsN);
				}
//				
//				System.out.print("Bids = ");
//				printArray(bidsN);
//				
//				this.bidSize = bidSize;
//			}
			
			// stage two
			
			//System.out.println((int) );
//			System.out.println("sum = " + cplex.getValue(cplex.sum(bidsH)));
//			System.out.println(bidsH[0].getUB());;
//			System.out.println((int) Math.round(cplex.getValue(bidsH[0])));
//			bidsH[0].setMin((int) Math.round(cplex.getValue(bidsH[0])));
//			.setUB(cplex.getValue(bidsH[0]));
			//cplex.addEq(bidsH[1], (int) Math.round(cplex.getValue(bidsH[1])));
			//cplex.addGe(bidsH[1], (int) Math.round(cplex.getValue(bidsH[1])));
			double bid1 = cplex.getValue(bidsH[0]);
			double bid2 = 0;
			if(bidsH.length > 1) {
				bid2 = cplex.getValue(bidsH[1]);	
			}
			int totalBids = 0;
			for(int i = 0; i < bidsH.length; i++) {
				totalBids = totalBids + (int) Math.round(cplex.getValue(bidsH[i]));
			}
			cplex.addGe(cplex.sum(bidsH), totalBids);
			cplex.addLe(bid1, bidsH[0]);
			//cplex.getValue(bidsH[1]);
			if(bidsH.length > 1) {
				cplex.addLe(bid2, bidsH[1]);
			}
			
			cplex.remove(maxObjective);
			IloQuadNumExpr minimization = cplex.quadNumExpr();
			double[] coeff = new double[p.length];
			for(int i = 0; i < p.length; i++) {
				coeff[i] = 1;
			}
			minimization.addTerms(coeff, p, p);
			cplex.addMinimize(minimization);

			cplex.setOut(null);
			if(cplex.solve()) {
//				cplex.output().println("Solution status = " + cplex.getStatus());
//				cplex.output().println("Solution value = " + cplex.getObjValue());
//
//				System.out.print("Emin = ");
//				printArray(Emin);
//				
//				System.out.print("Emax = ");
//				printArray(Emax);
//				
//				System.out.print("Pmax = ");
//				printArray(Pmax);
//				
				powerPath = cplex.getValues(p);
				System.out.print("Power = ");
				printArray(powerPath);

				energyPath = cplex.getValues(e);
//				System.out.print("Energy = ");
//				printArray(energyPath);
				
				regPower = cplex.getValues(regPowerQ);
				System.out.print("RegPower = ");
				printArray(regPower);
				
				bidsN = new int[bidsH.length];
				for(int i = 0; i < bidsN.length; i++) {
					bidsN[i] = (int) Math.round(cplex.getValue(bidsH[i]));
				}
				//shift bids right if no quarter left in current bid slot -- hack-ish
				if(quartersLeftInFirstBid == 0) {
					bidsN = concat(new int[1], bidsN);
				}
				
				System.out.print("Bids = ");
				printArray(bidsN);
				
				this.bidSize = bidSize;
			}
			
			for(int i = 0; i < flexpowers.size(); i++) {
				flexGraphs.get(i).setSolutionPath(cplex.getValues(flexenergies.get(i)));
			}
			return true;
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
			e.printStackTrace();
			return false;
		}
	}
	
	public long[] getSolutionPath() {
		long[] solution = new long[this.energyPath.length];
		for(int i = 0; i < solution.length; i++) {
			solution[i] = Math.round(energyPath[i]);
		}
		return solution;
	}
	
	public int[] getBidPlanning() {
		return bidsN;
	}
	
	public long[] getRegPower() {
		return convertToLongs(regPower);
	}

	public long[] getReqPowerInHours() {
		if(quartersLeftInFirstBid == 0) {
			long[] reqPower = new long[bidsN.length-1];
			for(int i = 1; i < reqPower.length; i++) {
				reqPower[i-1] = bidsN[i]*this.bidSize;
			}			
			return reqPower;
		}else{
			long[] reqPower = new long[bidsN.length];
			for(int i = 0; i < reqPower.length; i++) {
				reqPower[i] = bidsN[i]*this.bidSize;
			}			
			return reqPower;
		}
	}
	
	public long[] getReqPowerInQuarters() {
		long[] regPowerInQuarters = new long[regPower.length];
		for(int i = 0; i < regPower.length; i++) {
			//convert Q to H
			int h = (int) Math.ceil(((i+1) - quartersLeftInFirstBid)/4.0);
			if(quartersLeftInFirstBid == 0) {
				h--;
			}
			regPowerInQuarters[i] = bidsN[h]*this.bidSize;
		}
		return regPowerInQuarters;
	}
	
	public void printArray(double[] array) {
		System.out.print("[");
		for(int i = 0; i < array.length; i++) {
			System.out.print(array[i] + ",");
		}
		System.out.println("]");
	}
	
	public void printArray(int[] array) {
		System.out.print("[");
		for(int i = 0; i < array.length; i++) {
			System.out.print(array[i] + ",");
		}
		System.out.println("]");
	}
	
	private double[] convertToDoubles(long[] input)
	{
	    if (input == null)
	    {
	        return null; // Or throw an exception - your choice
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	private long[] convertToLongs(double[] input)
	{
	    if (input == null)
	    {
	        return null; // Or throw an exception - your choice
	    }
	    long[] output = new long[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = (long) input[i];
	    }
	    return output;
	}
	
	private double[] concat(double[] A, double[] B) {
	   int aLen = A.length;
	   int bLen = B.length;
	   double[] C= new double[aLen+bLen];
	   System.arraycopy(A, 0, C, 0, aLen);
	   System.arraycopy(B, 0, C, aLen, bLen);
	   return C;
	}
	
	private int[] concat(int[] A, int[] B) {
		   int aLen = A.length;
		   int bLen = B.length;
		   int[] C= new int[aLen+bLen];
		   System.arraycopy(A, 0, C, 0, aLen);
		   System.arraycopy(B, 0, C, aLen, bLen);
		   return C;
		}
}