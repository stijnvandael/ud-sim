package aggregator.modules.communication;

import java.util.ArrayList;

import communication.messages.ScheduleMessage;
import utils.ArrayMath;

public class FlexGraph {
	
	private ArrayList<EVInformation> evs;
	
	private long[] energyMin;
	private long[] energyMax;
	private long[] powerMax;
	private long[] regPowerMax;
	
	public FlexGraph(long[] energyMin, long[] energyMax, long[] powerMax, long[] regPowerMax, EVInformation ev) {
		evs = new ArrayList<EVInformation>();
		evs.add(ev);
		this.energyMin = energyMin;
		this.energyMax = energyMax;
		this.powerMax = powerMax;
		this.regPowerMax = regPowerMax;
	}
	
	public FlexGraph() {
		evs = new ArrayList<EVInformation>();
		this.energyMin = new long[0];
		this.energyMax = new long[0];
		this.powerMax = new long[0];
		this.regPowerMax = new long[0];
	}

	public boolean inSameBin(FlexGraph f2) {
		if(getEnergyMin().length != f2.getEnergyMax().length) {
			return false;
		}
		for(int i = 0; i < getEnergyMin().length; i++) {
			if((getEnergyMin()[i] == 0 && f2.getEnergyMin()[i] != 0)||(getEnergyMin()[i] != 0 && f2.getEnergyMin()[i] == 0)) {
				return false;
			}
		}
		return true;
	}

	public void aggregate(FlexGraph f) {
		this.evs.addAll(f.evs);
		this.energyMin = ArrayMath.sumAccArray(energyMin, f.energyMin);
		this.energyMax = ArrayMath.sumAccArray(energyMax, f.energyMax);
		this.powerMax = ArrayMath.sumArray(powerMax, f.powerMax);
		this.regPowerMax = ArrayMath.sumArray(regPowerMax, f.regPowerMax);
	}
	
	public long[] getEnergyMin() {
		return energyMin;
	}

	public long[] getEnergyMax() {
		return energyMax;
	}
	
	public long[] getPowerMax() {
		return powerMax;
	}
	
	public long[] getRegPowerMax() {
		return regPowerMax;
	}
	
	/**
	 * Solution fields and methods for path
	 */
	
	private double[] solutionPath;

	public double[] getSolutionPath() {
		return solutionPath;
	}

	public void setSolutionPath(double[] ds) {
		this.solutionPath = ds;
	}

	public long[] getPercentagePlan() {
		if(solutionPath == null) {
			return new long[energyMin.length];
		}
		
		long[] percentagePlan = new long[energyMin.length];
		for(int i = 0; i < percentagePlan.length; i++) {
			long numerator = ((long)Math.ceil(solutionPath[i]) - energyMin[i]);
			long denominator = energyMax[i] - energyMin[i];
			long percentage;
			if(denominator == 0) {
				percentage = 0;
			}else{
				percentage = (numerator*1000)/denominator;
			}
			percentagePlan[i] = percentage;
		}
		
		return percentagePlan;
		
	}
	
	public void sendPercPath(ScheduleMessage message) {
		for(EVInformation info: evs) {
			info.getSession().send(message);
		}
	}
	
	public int getNrOfTimeslots() {
		return this.energyMin.length -1;
	}

}
