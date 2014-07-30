package main;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;
import aggregator.agent.AggregatorAgent;
import aggregator.modules.AggregatorModuleManager;
import communication.network.Server;
import communication.network.simple.SimpleClient;
import communication.network.simple.SimpleServer;
import electricvehicle.agent.EVAgent;
import electricvehicle.modules.EVModuleManager;


public class SingleThreadBuilder {
	
	private static SimpleServer server;
	
	public static EVAgent buildEVAgent(String evName, long arrivalTime, long departureTime, long currentE, long requiredE, long minE, long maxE, long maxP, boolean useBins) {
		//Optimization parameters
		Duration optimizationInterval = new Duration(15*60*1000);
		
		//Regulation parameters
		Duration deltaT = new Duration(900*1000);

		//EV parameters
		EnergyValue capacity = new EnergyValue(35000);
		EnergyValue minEnergy = new EnergyValue(minE);
		EnergyValue maxEnergy = new EnergyValue(maxE);
		EnergyValue currentEnergy = new EnergyValue(currentE);
		EnergyValue requiredEnergy = new EnergyValue(requiredE);
		PowerValue maxChargePower = new PowerValue(-maxP);
		PowerValue maxDischargePower = new PowerValue(maxP);
		PowerValue binPower = new PowerValue(11000);
		Duration chargeTime = new Duration((departureTime - arrivalTime)*1000);

		//communication network
		if(server == null) {
			throw new IllegalArgumentException("First build aggregator");
		}
		SimpleClient client = new  SimpleClient(evName, server);
		
		//Create simulation
		EVModuleManager ev = new EVModuleManager(evName, capacity, minEnergy, maxEnergy, currentEnergy, requiredEnergy, maxChargePower, maxDischargePower, chargeTime, client);
		EVAgent evAgent = new EVAgent(ev, deltaT, optimizationInterval, binPower, useBins);
		
		return evAgent;
	}

	public static AggregatorAgent buildAggregatorAgent(boolean naieve, boolean useBins, long bidSize) {
		//Optimization parameters
		Duration optimizationInterval = new Duration(15*60*1000);
		
		//Create agent and modules
		server = new SimpleServer();	
		AggregatorModuleManager aggregator = new AggregatorModuleManager("UDaggregator", server, "data/pjmprofile.csv", bidSize);
		AggregatorAgent aggregatorAgent = new AggregatorAgent(aggregator, optimizationInterval, naieve, useBins);

		return aggregatorAgent;
	}
	
}
