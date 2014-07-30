package units;

import org.joda.time.Duration;

public final class EnergyValue {

	private final long energyInWh;
	
	public EnergyValue(long energyInWh) {
		this.energyInWh = energyInWh;
	}
	
	public long compareTo(EnergyValue arg0) {
		if(energyInWh < arg0.inWH()) {
			return -1;
		}else
		if(energyInWh > arg0.inWH()) {
			return 1;
		}
		return 0;
	}

	public long inWH() {
		return energyInWh;
	}

	public long inKWH() {
		return (long) (energyInWh/1000.0);
	}

	public long inMWH() {
		return (long) (energyInWh/1000000.0);
	}

	public EnergyValue minus(EnergyValue energyMagnitude) {
		return new EnergyValue(energyInWh - energyMagnitude.inWH());
	}

	public EnergyValue plus(EnergyValue energyMagnitude) {
		return new EnergyValue(energyInWh + energyMagnitude.inWH());
	}

	public PowerValue divide(Duration duration) {
		return new PowerValue((long) (energyInWh/(duration.getStandardSeconds()/3600.0)));
	}
	
	public Duration divide(PowerValue powerMagnitude) {
		return new Duration((long)((energyInWh*3600000.0)/(double)powerMagnitude.inWatt()));
	}
	
	public String toString() {
		return inKWH() + "kWh";
	}
	
	public EnergyValue copy() {
		return new EnergyValue(energyInWh);
	}
	
	@Override
	public boolean equals(Object other) {
		EnergyValue o = (EnergyValue) other;
		if(this.energyInWh == o.inWH()){
			return true;
		}else{
			return false;
		}
	}

}