package units;

import org.joda.time.Duration;

public final class PowerValue {

	private final long powerInWatt;
	
	public PowerValue(long powerInWatt) {
		this.powerInWatt = powerInWatt;
	}

	public long compareTo(PowerValue o) {
		if(powerInWatt < o.inWatt()) {
			return -1;
		}else
		if(powerInWatt > o.inWatt()) {
			return 1;
		}
		return 0;
	}

	
	public long inWatt() {
		return powerInWatt;
	}

	
	public long inKWatt() {
		return (long) (powerInWatt/1000.0);
	}

	
	public long inMWatt() {
		return (long) (powerInWatt/1000000.0);
	}

	
	public PowerValue minus(PowerValue powerMagnitude) {
		return new PowerValue(powerInWatt - powerMagnitude.inWatt());
	}

	
	public PowerValue plus(PowerValue powerMagnitude) {
		return new PowerValue(powerInWatt + powerMagnitude.inWatt());
	}

	
	public EnergyValue multiply(Duration duration) {
		return new EnergyValue((long)(((powerInWatt*duration.getStandardSeconds())/3600.0)));
	}
	
	public String toString() {
		return inWatt() + "W";
	}
	
	public PowerValue copy() {
		return new PowerValue(powerInWatt);
	}
	
	public boolean equals(PowerValue other) {
		if(this.inWatt() == other.inWatt()) {
			return true;
		}else{
			return false;
		}
	}

}