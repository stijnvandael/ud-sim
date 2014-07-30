package main;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;
import aggregator.agent.AggregatorAgent;
import aggregator.gui.AggregatorGui;
import aggregator.modules.AggregatorModuleManager;
import clock.SimTime;
import clock.Time;
import clock.Time.SimulationMode;
import communication.network.simple.SimpleClient;
import communication.network.simple.SimpleServer;
import communication.network.socket.SocketClient;
import communication.network.socket.SocketServer;
import electricvehicle.agent.EVAgent;
import electricvehicle.gui.EVGui;
import electricvehicle.modules.EVModuleManager;


public class MainSimulation {

	public static void main(String[] args) throws InterruptedException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		createSingleThreadSimulation();
		//createMultiThreadSimulation();
	}
	
	private static void createSingleThreadSimulation() throws InterruptedException {
		Time.simMode = SimulationMode.SIMTIME;
		
		//build agents
		boolean naieve = false;
		boolean useBins = true;
		AggregatorAgent aggregatorAgent = SingleThreadBuilder.buildAggregatorAgent(naieve, useBins, 10000);
		long currentE = 15000;
		long requiredE = 25000;
		EVAgent evAgent1 = SingleThreadBuilder.buildEVAgent("ev1", currentE, requiredE, 0, 0, 0, 0, 0, useBins);
//		EVAgent evAgent2 = SingleThreadBuilder.buildEVAgent();

		//run Aggregator agent
		aggregatorAgent.start();
		new AggregatorGui(aggregatorAgent);
		
		//run EV agent
		evAgent1.start();
		new EVGui(evAgent1);
		
//		//run EV agent
//		evAgent2.start();
//		new EVGui(evAgent2);
		
		//start main thread
		if(Time.getInstance().simMode.equals(SimulationMode.SIMTIME)) {
			((SimTime)Time.getInstance()).run(new Duration(10*3600*1000));
		}
		
	}
	
	private static void createMultiThreadSimulation() throws InterruptedException {
		Time.simMode = SimulationMode.REALTIME;
		Time.speedUpFactor = 200;
		
		//build agents
		boolean naieve = false;
		boolean useBins = true;
		AggregatorAgent aggregatorAgent = MultiThreadBuilder.buildAggregatorAgent(naieve, useBins, 10000);
		EVAgent evAgent = MultiThreadBuilder.buildEVAgent(useBins);
		
		//run Aggregator agent
		aggregatorAgent.start();
		new AggregatorGui(aggregatorAgent);
		
		Thread.sleep(3000);
		
		//run EV agent
		evAgent.start();
		new EVGui(evAgent);
	}
	
}