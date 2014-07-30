package clock;

import java.util.TimerTask;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

/**
 * 
 * Wrapper class for DateTime.
 * 
 * @author stijnvandael
 *
 */
public abstract class Time {
	
	/**
	 * Singleton pattern
	 */
	
	private static Time instance = null;
	
	protected Time() {
	      // Exists only to defeat instantiation.
	}
	
	public static Time getInstance() {
		if(instance == null) {
			if(simMode == SimulationMode.REALTIME) {
				instance = new RealTime();
			}else
			if(simMode == SimulationMode.SIMTIME) {
				instance = new SimTime();
			}
		}
		return instance;
	}
	
    public static long speedUpFactor = 10;
    public static SimulationMode simMode = SimulationMode.SIMTIME;
    public enum SimulationMode {
    	REALTIME, SIMTIME
	}
	
	public abstract DateTime getDateTime();
	
	public abstract void scheduleHourlyTask(String id, TimerTask task);
	
	public abstract void scheduleQuarterlyTask(String id, TimerTask task);

	public abstract void scheduleAtFixedRate(String id, TimerTask task, long period, long begin);

	public abstract void scheduleThread(String id, SimRunnable runnable);
	
	public abstract void removeThread(String id);
	
	public String toString() {
		return this.getDateTime().toString();
	}

	public abstract void scheduleRandom(String id, TimerTask task, long period);

}
