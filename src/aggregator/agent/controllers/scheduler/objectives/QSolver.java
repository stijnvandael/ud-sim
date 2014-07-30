package aggregator.agent.controllers.scheduler.objectives;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import utils.ArrayMath;
import utils.NullOutputStream;
import aggregator.modules.communication.EVInformation;
import aggregator.modules.communication.FlexGraph;
import ilog.concert.*;
import ilog.cplex.*;
public class QSolver implements AggregatorObjective {
	
	public static void main(String[] args) {
//		//create problem setting
//		long[] Pmax = {40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40};
//		long[] Preg = Arrays.copyOf(Pmax, Pmax.length);
//		long[] Emin = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,20,30,40,50,60,70,80,90};
//		long[] Emax = {0,10,20,30,40,50,60,70,80,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90};
//		int quartersLeftInFirstBid = 3;
//		int[] bids = {1,2};
//		int bidSize = 20;
//		ArrayList<FlexGraph> flexGraphs = new ArrayList<FlexGraph>();
//		FlexGraph flexGraph = new FlexGraph(Emin, Emax, Pmax, Preg, null) ;
//		flexGraphs.add(flexGraph);		
//		QSolver solver = new QSolver();
//		boolean solved = solver.solve(Pmax, Preg, Emin, Emax, quartersLeftInFirstBid, bids, bidSize, flexGraphs);
		//create problem setting
		long[] Pmax = {11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000, 11000};
		long[] Preg = {12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000, 12000};
		long[] Emin = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3000};
		long[] Emax = {0, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000};
		int quartersLeftInFirstBid = 1;
		int[] bids = {1,1};
		int bidSize = 10000;
		ArrayList<FlexGraph> flexGraphs = new ArrayList<FlexGraph>();
		FlexGraph flexGraph = new FlexGraph(Emin, Emax, Pmax, Preg, null) ;
		flexGraphs.add(flexGraph);		
		QSolver solver = new QSolver();
		boolean solved = solver.solve(Pmax, Preg, Emin, Emax, quartersLeftInFirstBid, bids, bidSize, flexGraphs);
	}
	
	//input
	private int totalTime;
	private double[] Pmax_tot;
	private double[] PregMax_tot;
	private double[] Emin_tot;
	private double[] Emax_tot;
	private int quartersLeftInFirstBid;
	private int[] bids;
	private int bidSize;
	private ArrayList<FlexGraph> flexGraphs;
	
	//output
	private boolean success_stage1;
	private boolean success_stage2;
	private boolean success_stage3;
	private boolean success_stage4;
	private boolean success_stage5;
	private double[] powerPath;
	private double[] energyPath;
	private double[] regPower;
	private int[] bidsPlanning;
	private BufferedWriter writer;
		
	public boolean solve(long[] Pmax, long[] Preg, long[] Emin, long[] Emax, int quartersLeftInFirstBid, int[] bids, int bidSize, ArrayList<FlexGraph> flexGraphs) {
		try{
			solve(ArrayMath.convertToDoubles(Pmax), ArrayMath.convertToDoubles(Preg), ArrayMath.convertToDoubles(Emin), ArrayMath.convertToDoubles(Emax), quartersLeftInFirstBid, bids, bidSize, flexGraphs);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Concert exception caught: " + e);
			return false;
		}
	}
	
	public void solve(double[] Pmax_tot, double[] PregMax_tot, double[] Emin_tot, double[] Emax_tot, int quartersLeftInFirstBid, int[] bids, int bidSize, ArrayList<FlexGraph> flexGraphs) throws Exception {
		//init input variables
		this.success_stage1 = false;
		this.success_stage2 = false;
		this.success_stage3 = false;
		this.success_stage4 = false;
		this.success_stage5 = false;
		this.Pmax_tot = Pmax_tot;
		this.PregMax_tot = PregMax_tot;
		this.Emin_tot = Emin_tot;
		this.Emax_tot = Emax_tot;
		this.bids = bids;
		this.flexGraphs = flexGraphs;
		this.totalTime = Pmax_tot.length;
		this.quartersLeftInFirstBid = quartersLeftInFirstBid;
		this.bidSize = bidSize;
		
		//Pmin is zero
		double[] Pmin_tot = new double[Pmax_tot.length];
		
		//if no quarters left, no bids allowed in first hour
	    if(quartersLeftInFirstBid == 0) {
	    	bids[0] = 0;
	    }

		IloCplex cplex = new IloCplex();

		/**
		 *  EV CONSTRAINTS - QUARTERLY
		 */
		//for all evs
		ArrayList<IloNumVar[]> flexpowers = new ArrayList<IloNumVar[]>();
		ArrayList<IloNumVar[]> flexenergies = new ArrayList<IloNumVar[]>();
		for(int i = 0; i < flexGraphs.size(); i++) {
			FlexGraph flexGraph = flexGraphs.get(i);
			double[] energyMin = ArrayMath.convertToDoubles(flexGraph.getEnergyMin());
			double[] energyMax = ArrayMath.convertToDoubles(flexGraph.getEnergyMax());
			double[] powerMax = ArrayMath.convertToDoubles(flexGraph.getPowerMax());
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
		IloNumVar[] flexPower_tot = cplex.numVarArray(Pmin_tot.length, Pmin_tot, Pmax_tot);
		
		//individual bin constraints
		for(int t = 0; t < flexPower_tot.length; t++) {
			ArrayList<IloNumVar> tempVars = new ArrayList<IloNumVar>();
			for(IloNumVar[] var: flexpowers) {
				if(var.length >= t + 1) {
					tempVars.add(var[t]);	
				}
			}
			IloNumVar[] tempVars_array = new IloNumVar[tempVars.size()];
			tempVars.toArray(tempVars_array);
			cplex.addEq(flexPower_tot[t], cplex.sum(tempVars_array));
		}
					
		// path
		IloNumVar[] solutionPath = cplex.numVarArray(Emin_tot.length, Emin_tot, Emax_tot);
		for(int i = 0; i < flexPower_tot.length; i++) {
			cplex.addEq(cplex.diff(solutionPath[i+1],solutionPath[i]), cplex.prod(1.0/4, flexPower_tot[i]));
		}
		
		// Emin < path < Emax
		for(int i = 0; i < solutionPath.length; i++) {
			cplex.addGe(solutionPath[i], Emin_tot[i]);
			cplex.addLe(solutionPath[i], Emax_tot[i]);
		}

		// regulation power in each optimziation interval
		IloNumVar[] Preg_tot = cplex.numVarArray(Pmin_tot.length, Pmin_tot, PregMax_tot);
		
		//calculate regulation power in each optimization interval
		for(int i = 0; i < Preg_tot.length; i++) {
			cplex.addEq(Preg_tot[i], cplex.diff(PregMax_tot[i], flexPower_tot[i]));
		}

		/**
		 * OPTIMIZATION PROBLEM -- STAGE 1 -- MINIMIZE MARKET BID ERROR (QP)
		 */
		//add optimization objective and solve
	    int firstOptimizationIntervals = Math.min(quartersLeftInFirstBid+4, totalTime);
	    if(bids[0] > 0 || bids[1] > 0) {
		    IloNumExpr objective_stage1[] = new IloNumExpr[firstOptimizationIntervals];
		    for(int t = 0; t < firstOptimizationIntervals; t++) {
		            int h = (int)Math.ceil(((t+1) - quartersLeftInFirstBid)/4.0);
		            objective_stage1[t] = cplex.square(cplex.diff(Preg_tot[t], bids[h]*bidSize + 0.1*bidSize));
			}
		    IloObjective minimizeObjective_stage1 = cplex.addMinimize(cplex.sum(objective_stage1));
		    this.success_stage1 = cplex.solve();
		    
		    // add regulation capacity for submitted bids as constraints
		    double[] Preg_tot_constraint = cplex.getValues(Preg_tot);
		    for(int t = 0; t < firstOptimizationIntervals; t++) {
			    cplex.addGe(Preg_tot[t], Preg_tot_constraint[t]);
		    }
		    
		    //remove optimization objective
		    cplex.remove(minimizeObjective_stage1);
		    cplex.setOut(null);	    	
	    }
	    
		/**
		 * OPTIMIZATION PROBLEM -- STAGE 2 -- maximize future bids (MILP)
		 */
	    //add optimization objective and solve
	    // number of bid hours left for submitting bids
		int bidHours = Math.max(0, (int)Math.floor((totalTime - quartersLeftInFirstBid - 4)/4.0));
		// bids per hour
		IloNumVar[] flexBids = cplex.intVarArray(bidHours, 0, Integer.MAX_VALUE);
		//if no bid hours left, skip to stage 4
		if(bidHours > 0) {
			// limit bids by provided regulation power
			for (int t = 0; t < bidHours*4; t++) {
				cplex.addLe(cplex.prod(flexBids[(int)Math.ceil(t/4)],bidSize), Preg_tot[quartersLeftInFirstBid+4+t]);
			}
			IloObjective maximizeObjective_stage2 = cplex.addMaximize(cplex.sum(flexBids));
		    this.success_stage2 = cplex.solve();
		    
		    // add nr. of bids as a constraint
		    cplex.addGe(cplex.sum(flexBids), cplex.getValue(cplex.sum(flexBids)));

		    //remove optimization objective
		    cplex.remove(maximizeObjective_stage2);
		    cplex.setOut(null);

		    /**
		     * OPTIMIZATION PROBLEN -- STAGE 3 -- rest flexibility (MILP)
		     */
		    //add optimization objective and solve
		    IloNumExpr[] extraFlexibility = new IloNumExpr[bidHours*4];
		    for (int t = 0; t < bidHours*4; t++) {
		    	extraFlexibility[t] = cplex.min(cplex.diff(Preg_tot[quartersLeftInFirstBid+4+t], cplex.prod(flexBids[(int)Math.ceil(t/4)], bidSize)), 0.2*bidSize);
		    }
			IloObjective maximizeObjective_stage3 = cplex.addMaximize(cplex.sum(extraFlexibility));
		    this.success_stage3 = cplex.solve();

		    // add extra regulation power as constraint
		    cplex.addGe(cplex.sum(extraFlexibility), cplex.getValue(cplex.sum(extraFlexibility)));
		    
		    //remove optimization objective
		    cplex.remove(maximizeObjective_stage3);
		    cplex.setOut(null);			
		}

	    /**
	     * OPTIMIZATION PROBLEM -- STAGE 4 -- flatten load (MIQP)
	     */
	    IloNumExpr objective_stage4[] = new IloNumExpr[flexPower_tot.length];
	    for(int t = 0; t < flexPower_tot.length; t++) {
	    	objective_stage4[t] = cplex.square(flexPower_tot[t]);
		}
		IloObjective minimizeObjective_stage4 = cplex.addMinimize(cplex.sum(objective_stage4));
	    this.success_stage4 = cplex.solve();
	    
	    //if no bids left, no need for stage 5 optimization
	    if(bidHours > 0) {
		    //add quadratic load as constraint
		    cplex.addLe(cplex.sum(objective_stage4), cplex.getValue(cplex.sum(objective_stage4)));
		    
		    //remove optimization objective
		    cplex.remove(minimizeObjective_stage4);
		    cplex.setOut(null);
		    
		    /**
		     * OPTIMIZATION PROBLEM -- STAGE 5 -- value early bids more than late bids (MIQP)
		     */
		    int[] weights = new int[flexBids.length];
		    for(int i = 0; i < weights.length; i++) {
		    	weights[i] = weights.length - i;
		    }
		    IloObjective maximizeObjective_stage5 = cplex.addMaximize(cplex.scalProd(flexBids, weights));
		    this.success_stage5 = cplex.solve();
	    }
	    
	    //store solution
	    powerPath = cplex.getValues(flexPower_tot);
	    energyPath = cplex.getValues(solutionPath);
	    regPower = cplex.getValues(Preg_tot);
	    bidsPlanning = new int[flexBids.length];
		for(int i = 0; i < flexBids.length; i++) {
			bidsPlanning[i] = (int) Math.round(cplex.getValue(flexBids[i]));
		}
		
		for(int i = 0; i < flexpowers.size(); i++) {
			flexGraphs.get(i).setSolutionPath(cplex.getValues(flexenergies.get(i)));
		}
		
	}

	public void printSolution() {
		System.out.print("Emin = ");
		ArrayMath.printArray(this.Emin_tot);
		
		System.out.print("Emax = ");
		ArrayMath.printArray(Emax_tot);
		
		System.out.print("Pmax = ");
		ArrayMath.printArray(Pmax_tot);
		
		
		System.out.print("Power = ");
		ArrayMath.printArray(powerPath);

		
		System.out.print("solutionPath = ");
		ArrayMath.printArray(energyPath);
		
		
		System.out.print("regulationPower = ");
		ArrayMath.printArray(regPower);
		
		System.out.print("Bidplanning = ");
		ArrayMath.printArray(bidsPlanning);
	}
	
	public long[] getSolutionPath() {
		long[] solution = new long[this.energyPath.length];
		for(int i = 0; i < solution.length; i++) {
			solution[i] = Math.round(energyPath[i]);
		}
		return solution;
	}
	
	public int[] getBidPlanning() {
		return bidsPlanning;
	}
	
}