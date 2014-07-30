package electricvehicle.modules.battery;

import org.joda.time.DateTime;

import units.EnergyValue;
import units.PowerValue;
import clock.Time;
import exceptions.BatteryException;

public class BatteryModule {

	//power parameters
	private PowerValue maxDischargePower;
	private PowerValue maxChargePower;
	private DateTime controlTime;
	private PowerValue power;
	
	//energy parameters
	private final EnergyValue capacity;
	private final EnergyValue maxEnergy;
	private final EnergyValue minEnergy;
	private EnergyValue currentEnergy;
	
	public BatteryModule(EnergyValue capacity, EnergyValue minEnergy, EnergyValue maxEnergy, EnergyValue currentEnergy, PowerValue maxChargePower, PowerValue maxDischargePower) {
		this.capacity = capacity;
		this.minEnergy = minEnergy;
		this.maxEnergy = capacity;
		this.currentEnergy = currentEnergy;
		this.maxChargePower = maxChargePower;
		this.maxDischargePower = maxDischargePower;
		//set initial power to 0
		this.power = new PowerValue(0);
	}
	
	public PowerValue getCurrentPower() {
		return power;
	}

	public void setPower(PowerValue power) throws BatteryException {
		if(power.inWatt() > maxDischargePower.inWatt() || power.inWatt() < maxChargePower.inWatt()) {
			throw new BatteryException("power out of range: " + maxDischargePower + " < " + power + " < " + maxChargePower);
		}
		//check if this power was already set
		if(this.power.inWatt() == power.inWatt()) {
			return;
		}
		this.currentEnergy = getCurrentEnergy();
		this.controlTime = Time.getInstance().getDateTime();
		this.power = power;
	}

	public EnergyValue getCapacity() {
		return capacity;
	}

	public EnergyValue getCurrentEnergy() {
		if(this.power.inWatt() == 0) {
			return currentEnergy;
		}else {
			long durationFromPreviousControl = Time.getInstance().getDateTime().getMillis() - controlTime.getMillis();
			return new EnergyValue(currentEnergy.inWH() - (long)((power.inWatt()*durationFromPreviousControl)/(3600.0*1000.0)));
		}
	}

	public PowerValue getPmax() {
		return maxDischargePower;
	}
	
	public PowerValue getPmin() {
		return maxChargePower;
	}

	public EnergyValue getMaxEnergy() {
		return maxEnergy;
	}

	public EnergyValue getMinEnergy() {
		return minEnergy;
	}

}
