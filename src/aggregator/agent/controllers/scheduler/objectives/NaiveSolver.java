package aggregator.agent.controllers.scheduler.objectives;

import java.util.ArrayList;
import java.util.Arrays;

import utils.ArrayMath;
import aggregator.modules.communication.FlexGraph;
public class NaiveSolver implements AggregatorObjective {
		
	//output
	private long[] energyPath;
	private int[] bidsPlanning;
		
	public boolean solve(long[] Pmax_tot, long[] PregMax_tot, long[] Emin_tot, long[] Emax_tot, int quartersLeftInFirstBid, int[] bids, int bidSize, ArrayList<FlexGraph> flexGraphs) {
		//global path is same as EMin_tot
	    energyPath = Arrays.copyOf(Emin_tot,Emin_tot.length);
	    //local paths are same as EMin of each flexgraph
		for(FlexGraph f: flexGraphs) {
			f.setSolutionPath(ArrayMath.convertToDoubles(f.getEnergyMin()));
		}
		//calculate bids
		int bidHours = Math.max(0, (int)Math.floor((Pmax_tot.length - quartersLeftInFirstBid - 4)/4.0));
		this.bidsPlanning = new int[bidHours];
		int bidnr = 0;
		for(int n = (quartersLeftInFirstBid+4)+4; n < energyPath.length-1; n = n + 4) {
			//every last quarter, check available regulation power to bid
			//because this last quarter will be the most constraint in terms of energy charging and max power
			bidsPlanning[bidnr] = (int)Math.floor((PregMax_tot[n-1] - (energyPath[n] - energyPath[n-1])*4)/bidSize);
			bidnr++;
		}
		return true;
	}
		
	public long[] getSolutionPath() {
		return energyPath;
	}
	
	public int[] getBidPlanning() {
		return bidsPlanning;
	}
	
}