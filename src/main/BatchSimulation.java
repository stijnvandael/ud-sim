package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import logger.AggregatorLogger;
import logger.EVLogger;

import org.joda.time.Duration;

import aggregator.agent.AggregatorAgent;
import aggregator.agent.controllers.scheduler.Scheduler;
import aggregator.gui.AggregatorGui;
import clock.SimRunnable;
import clock.SimTime;
import clock.Time;
import clock.Time.SimulationMode;
import electricvehicle.agent.EVAgent;
import electricvehicle.gui.EVGui;

public class BatchSimulation extends SimRunnable {
	
	public static void main(String[] args) {
		new BatchSimulation();
	}
	
	private List<EVTuple> tuples;
	
	//in-simulation parameters
	AggregatorAgent aggregatorAgent;
	
	//loggers
	AggregatorLogger aggregatorLogger;
	EVLogger evLogger;
	
	String baseDir = "results/experiment1";
	
	boolean useBins;
	
	public BatchSimulation() {
		try {
			//read EV tuples
//			tuples = new CSVReader("data/evtest.csv").readTuples();
			String scenario = "data/evprofile_test.csv";
			tuples = new CSVReader(scenario).readTuples();
			Time.simMode = SimulationMode.SIMTIME;
			//find endtime
			int endTime = 0;
			for(EVTuple t: tuples) {
				if(t.getDepartureTime() > endTime) {
					endTime = t.getDepartureTime();
				}
			}
			//register this class as main thread
			((SimTime)Time.getInstance()).setMainThread(this);
			//start aggregator
			//experiment 1
//			boolean naieve = true;
//			useBins = false;
			//experiment 2
			boolean naieve = false;
			useBins = false;
			//experiment 3
//			boolean naieve = false;
//			useBins = false;
			//store copy of EV scenario
			File scenarioCopy = new File(baseDir + "/scenario.csv");
			if(!scenarioCopy.exists()) {
				Files.copy(new File(scenario).toPath(), scenarioCopy.toPath());	
			}
			//start aggregator
			long bidSize = 10000;
			startAggregator(naieve, useBins, bidSize);
			//start main thread
			((SimTime)Time.getInstance()).run(new Duration(900*1000+ endTime*1000));
			aggregatorLogger.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void step() {
		if(tuples.isEmpty()) {
			stopAggregator();
		}
		Iterator<EVTuple> it = tuples.iterator();
		while(it.hasNext()) {
			EVTuple ev = it.next();
			if(ev.getArrivalTime() == Time.getInstance().getDateTime().getMillis()/1000) {
				startEV(ev.getEvName(), ev.getArrivalTime(), ev.getDepartureTime(), ev.getCurrentEnergy(), ev.getRequestedEnergy(), ev.getMinimumEnergy(), ev.getMaximumEnergy(), ev.getMaximumPower(), useBins);
			}else
			if(ev.getDepartureTime() == Time.getInstance().getDateTime().getMillis()/1000) {
				Time.getInstance().removeThread(ev.getEvName());
				it.remove();
				evLogger.stop();
			}
		}
	}

	@Override
	protected void stop() {
		// TODO Auto-generated method stub		
	}
	
	private void startEV(String evName, long arrivalTime, long departureTime, long currentE, long requiredE, long minE, long maxE, long maxP, boolean useBins) {
		//build new EV agent
		EVAgent evAgent = SingleThreadBuilder.buildEVAgent(evName, arrivalTime, departureTime, currentE, requiredE, minE, maxE, maxP, useBins);
		//create log
		evLogger = new EVLogger(evAgent, baseDir);
		Time.getInstance().scheduleAtFixedRate("evLogger", evLogger, 1000, 0);
		//start
		evAgent.start();
//		new EVGui(evAgent);
	}
	
	private void startAggregator(boolean naieve, boolean useBins, long bidSize) {
		aggregatorAgent = SingleThreadBuilder.buildAggregatorAgent(naieve, useBins, bidSize);
		//create log
		aggregatorLogger = new AggregatorLogger(aggregatorAgent, baseDir);
		Time.getInstance().scheduleAtFixedRate("aggregatorlog", aggregatorLogger, 1000, 0);
		//start aggregator
		aggregatorAgent.start();
		new AggregatorGui(aggregatorAgent);
	}
	
	private void stopAggregator() {
		aggregatorAgent.stop();
	}
	
}
