package clock;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class SimTime extends Time {

	private int currentMillis;
	
	private ArrayList<SimTask> tasks;
	private ArrayList<RandomSimTask> randomTasks;
	private SimRunnable mainThread;
	private Hashtable<String, SimRunnable> threads;
	
	public SimTime() {
		tasks = new ArrayList<SimTask>();
		randomTasks = new ArrayList<RandomSimTask>();
		threads = new Hashtable<String, SimRunnable>();
	}
	
	/**
	 * MAIN loop for simulation-based time
	 */
	public void run(Duration simDuration) {
		for(currentMillis = 0; currentMillis < simDuration.getMillis(); currentMillis = currentMillis + 1000) {
			mainThread.step();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(SimTask task: tasks) {
				if((task.getBegin() + currentMillis) % task.getPeriod() == 0) {
					task.getTask().run();
				}
			}
			for(RandomSimTask task: randomTasks) {
				if(currentMillis >= task.getExecutionTime()) {
					task.getTask().run();
					task.setExecutionTime(currentMillis + new Random().nextInt((int) task.getPeriod()));
				}
			}
			for(SimRunnable r: threads.values()) {
				r.step();
			}
		}
	}
	
	@Override
	public DateTime getDateTime() {
		return new DateTime(currentMillis);
	}

	@Override
	public void scheduleHourlyTask(String id, TimerTask task) {
		scheduleAtFixedRate(id, task, 3600*1000, 0);
	}

	@Override
	public void scheduleQuarterlyTask(String id, TimerTask task) {
		scheduleAtFixedRate(id, task, 900*1000, 0);
	}

	@Override
	public void scheduleAtFixedRate(String id, TimerTask task, long period, long begin) {
		if(period%1000 != 0) {
			new IllegalArgumentException("Only second-based tasks allowed");
		}
		tasks.add(new SimTask(task, period, begin));
	}
	
	@Override
	public void scheduleRandom(String id, TimerTask task, long period) {
		if(period%1000 != 0) {
			new IllegalArgumentException("Only second-based tasks allowed");
		}
		randomTasks.add(new RandomSimTask(task, period));
	}

	@Override
	public void scheduleThread(String id, SimRunnable runnable) {
		threads.put(id, runnable);
	}
	
	public void setMainThread(SimRunnable mainThread) {
		this.mainThread = mainThread;
	}

	@Override
	public void removeThread(String id) {
		SimRunnable thread = threads.remove(id);
		thread.stop();
	}

}

class SimTask {
	
	private final TimerTask task;
	private final long period;
	private final long begin;
	
	public SimTask(TimerTask task, long period, long begin) {
		this.task = task;
		this.period = period;
		this.begin = (long) (Math.floor(begin/1000)*1000);
	}

	public TimerTask getTask() {
		return task;
	}

	public long getPeriod() {
		return period;
	}
	
	public long getBegin() {
		return begin;
	}
}

class RandomSimTask {
	
	private final TimerTask task;
	private final long period;
	private long executionTime;
	
	public RandomSimTask(TimerTask task, long period) {
		this.task = task;
		this.period = period;
		this.executionTime = 0;
	}
	
	public boolean isRandom() {
		return true;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public TimerTask getTask() {
		return task;
	}

	public long getPeriod() {
		return period;
	}

}
