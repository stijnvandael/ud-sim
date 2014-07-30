package aggregator.agent.controllers.scheduler;


import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import utils.ArrayMath;
import aggregator.agent.controllers.scheduler.objectives.AggregatorObjective;
import aggregator.agent.controllers.scheduler.objectives.NaiveSolver;
import aggregator.agent.controllers.scheduler.objectives.QSolver;
import aggregator.modules.AggregatorModuleManager;
import aggregator.modules.communication.FlexGraph;
import clock.Time;
import communication.messages.ScheduleMessage;

public class Scheduler {
	
	private final AggregatorObjective objective;
	
	private final AggregatorModuleManager aggregator;
	private final Duration intervalDuration = new Duration(900*1000);
	int intervalsPerBid = 4;
	
	//config booleans
	private boolean naieve;
	private boolean useBins;

	//current schedule
	private DateTime optimizationSyncTime;
	private long[] flexMin;
	private long[] flexMax;
	private long[] flexPower;
	private long[] flexRegMax;
	private long[] percentagePlan;
	
	public Scheduler(AggregatorModuleManager aggregator, boolean naieve, boolean useBins) {
		this.naieve = naieve;
		this.useBins = useBins;
		this.aggregator = aggregator;
		if(!naieve) {
			this.objective = new QSolver();	
		}else{
			this.objective = new NaiveSolver();
		}
		clearSchedule();
		initSchedulerTimer();
	}
	
	public void clearSchedule() {
		optimizationSyncTime = null;
		flexMin = new long[0];
		flexMax = new long[0];
		flexPower = new long[0];
		flexRegMax = new long[0];
		percentagePlan = new long[0];
	}
	
	public void initSchedulerTimer() {
		//rate of scheduling
		Duration scheduleRate = new Duration(30*1000);
		//create timer to calculate schedule based on current scheduling information
        TimerTask task = new TimerTask() {
          @Override
          public void run() {
        	  //STEP 1: gather information about fleet and calculate sync times
        	  //current time
        	  DateTime currentTime = Time.getInstance().getDateTime();
        	  //check if new interval
        	  if(!getOptimSynchronizationTime(currentTime).equals(optimizationSyncTime)) {
        		  clearSchedule();
        	  }
        	  //next optimization synchronization time
        	  optimizationSyncTime = getOptimSynchronizationTime(currentTime);
        	  //bins
        	  ArrayList<FlexGraph> flexGraphs;
        	  if(useBins) {
        		  flexGraphs = aggregator.getEVSessionManager().getFlexGraphsInBins(optimizationSyncTime);
        	  }else{
            	  flexGraphs = aggregator.getEVSessionManager().getFlexGraphs(optimizationSyncTime);
        	  }
        	  
        	  //flexgraph for optimization synctime
        	  FlexGraph totalFlexGraph = aggregator.getEVSessionManager().getTotalFlexGraph(optimizationSyncTime);
        	  flexMin = totalFlexGraph.getEnergyMin();
        	  //flexgraphmax for optimization synctime
        	  flexMax = totalFlexGraph.getEnergyMax();
        	  //flexmax for optimization synctime
        	  flexPower = totalFlexGraph.getPowerMax();
        	  flexRegMax = totalFlexGraph.getRegPowerMax();
        	  
        	  //STEP 2: if flexgraph is present, calculate percentagePlan with Objective
        	  if(flexMin.length != 0) {
        		  //STEP 2a: calculate flexpath within flexgraph
        		  int currentQuarter = aggregator.getBidModule().getCurrentOptimizationQuarter(currentTime);
        		  int quartersLeftInFirstBid = 4 - currentQuarter;
        		  int[] firstTwobidNrs = aggregator.getBidModule().getBidNrs(currentTime, 2);
        		  int bidSize = (int) aggregator.getBidModule().getBidSize();
        		  objective.solve(flexPower, flexRegMax, flexMin, flexMax, quartersLeftInFirstBid, firstTwobidNrs, bidSize, flexGraphs);
        		  long[] flexPath = objective.getSolutionPath();
        		  //STEP 2c: send flexpath in percentage to EVs
        		  saveGlobalPath(flexPath); //just for visualisation
        		  sendNewBinPaths(flexGraphs); //send messages to EVs
        	  }

        	  //STEP 3: automatically send bids
        	  if(aggregator.getBidModule().isNewBidInterval(currentTime, scheduleRate)) {
					int[] bids = objective.getBidPlanning();
					if(bids != null && bids.length >= 1) {
						aggregator.getBidModule().addFirstFreeBid(currentTime, bids[0]);
					}else{
						System.out.println("Warning, no bids!");
					}
        	  }
          }
        };
        Time.getInstance().scheduleAtFixedRate(aggregator.getId(), task, scheduleRate.getMillis(), new Random().nextInt((int)scheduleRate.getMillis()));
	}
	
	private void saveGlobalPath(long[] flexPath) {
		percentagePlan = new long[flexMin.length];
		for(int i = 0; i < percentagePlan.length; i++) {
			long numerator = (flexPath[i] - flexMin[i]);
			long denominator = flexMax[i] - flexMin[i];
			long percentage;
			if(denominator == 0) {
				percentage = 0;
			}else{
				percentage = (numerator*1000)/denominator;
			}
			percentagePlan[i] = percentage;
		} 
	}
	
	private void sendNewBinPaths(ArrayList<FlexGraph> flexGraphs) {
		for(FlexGraph flexGraph: flexGraphs) {
			long[] percentagePlan = flexGraph.getPercentagePlan();
			ScheduleMessage message = new ScheduleMessage(aggregator.getId(), optimizationSyncTime.getMillis(), percentagePlan);
			flexGraph.sendPercPath(message);
		}
	}
	
	/**
	* calculate next synchronization time
	*/
	public DateTime getOptimSynchronizationTime(DateTime time) {
		return time.plusMillis((int) (intervalDuration.getMillis()-(time.getMillisOfDay()%intervalDuration.getMillis())));
	}
	
	/**
	 * GETTERS AND SETTERS
	 */
	public DateTime getOptimizationSyncTime() {
		return optimizationSyncTime;
	}

	public long[] getFlexGraph() {
		return flexMin;
	}

	public long[] getFlexGraphMax() {
		return flexMax;
	}

	public long[] getFlexMax() {
		return flexPower;
	}
	
	public long[] getRegPowerMax() {
		return flexRegMax;
	}

	public long[] getFlexPath() {
		if(this.flexMin.length != percentagePlan.length) {
			return new long[0];
		}
		long[] flexPath = new long[this.flexMin.length];
		//add percentage path
		for(int i = 0; i < this.flexMin.length; i++) {
			//relative position in flex graph
			long newE = flexMin[i] + (percentagePlan[i] * (flexMax[i] - flexMin[i]))/1000;
			//add new energy to flexpath
			flexPath[i] = newE;
		}
		return flexPath;
	}

	public long[] getPercentagePlan() {
		return percentagePlan;
	}
	
	

}
