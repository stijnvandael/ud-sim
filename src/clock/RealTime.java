package clock;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

public class RealTime extends Time {

    private DateMidnight midnightTime;
    private Hashtable<String, TimeManager> timeManagers;
    
    public RealTime() {
		this.midnightTime = new DateMidnight();
		this.timeManagers = new Hashtable<String, TimeManager>();
    }

	public DateTime getDateTime() {
		DateTime actualTime = new DateTime();
		return new DateTime(midnightTime.getMillis() + (actualTime.getMillis() - midnightTime.getMillis())*speedUpFactor);
	}
	
	public void scheduleHourlyTask(String id, TimerTask task) {
		long currentTime = getDateTime().getMillis();
		long beginNextHour = (long) ((Math.ceil(currentTime/(3600.0*1000.0)))*3600*1000);
		long timeToGo = beginNextHour - getDateTime().getMillis();
		long realTimeToGo = timeToGo/speedUpFactor;
		Timer timer = new Timer();
		timer.schedule(
			      task,
			      realTimeToGo,
			      (3600*1000)/speedUpFactor
			    );
		storeTimer(id, timer);
	}
	
	public void scheduleQuarterlyTask(String id, TimerTask task) {
		long currentTime = getDateTime().getMillis();
		long beginNextQuarter = (long) ((Math.ceil(currentTime/(900.0*1000.0)))*900*1000);
		long timeToGo = beginNextQuarter - getDateTime().getMillis();
		long realTimeToGo = timeToGo/speedUpFactor;
		task.run();
		Timer timer = new Timer();
		timer.schedule(
			      task,
			      realTimeToGo,
			      (900*1000)/speedUpFactor
			    );
		storeTimer(id, timer);
	}

	public void scheduleAtFixedRate(String id, TimerTask task, long period, long begin) {
		if(speedUpFactor > period) {
			throw new IllegalArgumentException("Timertask is too fast for simulator speedup");
		}
		Timer timer = new Timer();
		timer.schedule(
			      task,
			      begin/speedUpFactor,
			      period/speedUpFactor
			    );
		storeTimer(id, timer);
	}

	@Override
	public void scheduleThread(String id, SimRunnable runnable) {
		Thread t = new Thread(runnable);
		t.start();
		storeThread(id, runnable);
	}
	
	public DateTime getRefTime() {
		return midnightTime.toDateTime();
	}
	
	public void kill(String id) {
		if(timeManagers.containsKey(id)) {
			timeManagers.get(id).stopAll();
			timeManagers.remove(id);
		}
	}
	
	private void storeTimer(String id, Timer timer) {
		if(timeManagers.containsKey(id)) {
			timeManagers.get(id).addTimer(timer);
		}else{
			TimeManager timeManager = new  TimeManager();
			timeManager.addTimer(timer);
			timeManagers.put(id, timeManager);
		}
	}
	
	private void storeThread(String id, SimRunnable runnable) {
		if(timeManagers.containsKey(id)) {
			timeManagers.get(id).addRunnable(runnable);
		}else{
			TimeManager timeManager = new  TimeManager();
			timeManager.addRunnable(runnable);
			timeManagers.put(id, timeManager);
		}
	}

	@Override
	public void removeThread(String id) {
		timeManagers.remove(id);
	}

	@Override
	public void scheduleRandom(String id, TimerTask task, long period) {
		if(speedUpFactor > period) {
			throw new IllegalArgumentException("Timertask is too fast for simulator speedup");
		}
		Timer timer = new Timer();
		timer.schedule(
			      task,
			      period/speedUpFactor,
			      period/speedUpFactor
			    );
		storeTimer(id, timer);
	}
	
}

class TimeManager {
	
	private ArrayList<Timer> timers;
	private ArrayList<SimRunnable> threads;
	
	public TimeManager() {
		this.timers = new ArrayList<Timer>();
		this.threads = new ArrayList<SimRunnable>();
	}
	
	public void addTimer(Timer timer) {
		this.timers.add(timer);
	}
	
	public void addRunnable(SimRunnable runnable) {
		this.threads.add(runnable);
	}
	
	public void stopAll() {
		for(Timer t: timers) {
			t.cancel();
		}
		for(SimRunnable s: threads) {
			s.stopRunning();
		}
	}
	
}