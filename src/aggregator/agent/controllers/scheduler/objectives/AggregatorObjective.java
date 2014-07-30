package aggregator.agent.controllers.scheduler.objectives;

import java.util.ArrayList;

import org.joda.time.DateTime;

import aggregator.modules.bids.BidModule;
import aggregator.modules.communication.FlexGraph;

/**
 * Interface for an aggregator objective.
 * 
 * @author stijn
 *
 */
public interface AggregatorObjective {

	/**
	 * Optimize EV fleet charging according to aggregator objective (optimization interval = quarter).
	 * 
	 * @param flexPowerMax
	 *        Maximum (dis)charging power of EV fleet (kW).
	 * @param flexEnergyMin
	 *        Minimum limit of flexgraph (kWh).
	 * @param flexEnergyMax
	 *        Maximum limit of flexgraph (kWh).
	 * @param quartersLeftInFirstBid
	 *        Quarters left to optimize in the first bid.
	 * @param bids
	 * 		  Two first bids, which were already submitted (array size = 2!);
	 * @param bidSize
	 * 		  Size of a bid (kWh).
	 */
	public boolean solve(long[] flexPowerMax, long[] PregMax, long[] flexEnergyMin, long[] flexEnergyMax, int quartersLeftInFirstBid, int[] bids, int bidSize, ArrayList<FlexGraph> flexGraphs);
	
	public long[] getSolutionPath();
	
	public int[] getBidPlanning();
	
}
