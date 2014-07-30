package electricvehicle.modules;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;
import clock.Time;

import communication.network.Client;

import electricvehicle.modules.battery.BatteryModule;
import electricvehicle.modules.communication.CommunicationModule;

public class EVModuleManager {

	private final String id;
	private final BatteryModule batteryModule;
	private final CommunicationModule communicationModule;
	private final EnergyValue requiredEnergy;
	
	private final Duration chargeTime;
	private DateTime arrivalTime;
	private DateTime departureTime;

	public EVModuleManager(String id, EnergyValue capacity, EnergyValue minEnergy, EnergyValue maxEnergy, EnergyValue currentEnergy, EnergyValue requiredEnergy, PowerValue maxChargePower, PowerValue maxDischargePower, Duration chargeTime, Client client) {
		this.id = id;
		//create modules
		batteryModule = new BatteryModule(capacity, minEnergy, maxEnergy, currentEnergy, maxChargePower, maxDischargePower);
		this.communicationModule = new CommunicationModule(client);
		//init parameters
		this.requiredEnergy = requiredEnergy;
		this.chargeTime = chargeTime;
		this.arrivalTime = Time.getInstance().getDateTime();
		this.departureTime = arrivalTime.plus(chargeTime);
	}
	
	public void init() {
		this.communicationModule.initCommunication();
	}
	
	public String getId() {
		return id;
	}

	public BatteryModule getBatteryModule() {
		return batteryModule;
	}
	
	public CommunicationModule getCommunicationModule() {
		return communicationModule;
	}

	public DateTime getArrivalTime() {
		return arrivalTime;
	}

	public DateTime getDepartureTime() {
		return departureTime;
	}

	public EnergyValue getCapacity() {
		return batteryModule.getCapacity();
	}
	
	public EnergyValue getMaxEnergy() {
		return batteryModule.getMaxEnergy();
	}
	
	public EnergyValue getMinEnergy(DateTime t) {
		EnergyValue e;
		if(t.isBefore(departureTime)) {
			e = new EnergyValue(Math.max(batteryModule.getMinEnergy().inWH(),  getMinRequiredEnergy(t).inWH()));
		}else{
			e = requiredEnergy;
		}
		return e;
	}
	
	public EnergyValue getMinRequiredEnergy(DateTime t) {
		return new EnergyValue(requiredEnergy.plus(batteryModule.getPmin().multiply(new Duration(t, departureTime))).inWH());
	}

	public EnergyValue getRequiredEnergy() {
		return requiredEnergy;
	}

	public void stop() {
		communicationModule.stop();
	}

}
