package clock;

public abstract class SimRunnable implements Runnable {
	
	private volatile boolean running = true;
	
	@Override
	public void run() {
		//start agent simulation
		while (running) {
			try {
				Thread.sleep(10000/Time.speedUpFactor);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			step();
		}
	}
	
	/**
	 * Performs one step.
	 */
	protected abstract void step();
	
	/**
	 * This method has to return the step method.
	 */
	protected abstract void stop();

	public void stopRunning() {
		this.running = false;
		stop();
	}

}
