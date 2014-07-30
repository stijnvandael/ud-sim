package main;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;
import aggregator.agent.AggregatorAgent;
import aggregator.modules.AggregatorModuleManager;
import communication.network.socket.SocketClient;
import communication.network.socket.SocketServer;
import electricvehicle.agent.EVAgent;
import electricvehicle.modules.EVModuleManager;


public class MultiThreadBuilder {
	
	public static EVAgent buildEVAgent(boolean useBins) {
		//Optimization parameters
		Duration optimizationInterval = new Duration(15*60*1000);
		
		//Regulation parameters
		Duration deltaT = new Duration(900*1000);
		
		//EV parameters
		String evId = "ev1";
		EnergyValue capacity = new EnergyValue(35000);
		EnergyValue minEnergy = new EnergyValue(10*1000);
		EnergyValue maxEnergy = new EnergyValue(33000);
		EnergyValue currentEnergy = new EnergyValue(20*1000);
		EnergyValue requiredEnergy = new EnergyValue(35*1000);
		PowerValue maxChargePower = new PowerValue(-12000);
		PowerValue maxDischargePower = new PowerValue(12000);
		PowerValue binPower = new PowerValue(11000);
		Duration chargeTime = new Duration(3*3600*1000);

		//Communication
		SocketClient client = new  SocketClient(evId, "localhost", 9999);
		
		//Create agent and modules
		EVModuleManager ev = new EVModuleManager(evId, capacity, minEnergy, maxEnergy, currentEnergy, requiredEnergy, maxChargePower, maxDischargePower, chargeTime, client);
		EVAgent evAgent = new EVAgent(ev, deltaT, optimizationInterval, binPower, useBins);
		return evAgent;
	}

	public static AggregatorAgent buildAggregatorAgent(boolean naieve, boolean useBins, long bidSize) {
		//Optimization parameters
		Duration optimizationInterval = new Duration(15*60*1000);
		
		//Communication
		SocketServer server = new SocketServer("aggregator",9999);
		
		//Create agent and modules
		AggregatorModuleManager aggregator = new AggregatorModuleManager("UDaggregator", server, "data/pjmprofile.csv", bidSize);
		AggregatorAgent aggregatorAgent = new AggregatorAgent(aggregator, optimizationInterval, naieve, useBins);
		return aggregatorAgent;
	}
	
}
