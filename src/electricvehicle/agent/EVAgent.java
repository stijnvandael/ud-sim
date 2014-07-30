package electricvehicle.agent;

import org.joda.time.Duration;

import units.PowerValue;
import clock.SimRunnable;
import clock.Time;
import electricvehicle.agent.controllers.regulation.RegulationController;
import electricvehicle.agent.controllers.scheduler.Scheduler;
import electricvehicle.modules.EVModuleManager;

public class EVAgent extends SimRunnable {
	
	private boolean isAlive;
	
	private final EVModuleManager moduleManager;
	private final Scheduler scheduler;
	private final RegulationController regulationController;

	public EVAgent(EVModuleManager moduleManager, Duration deltaT, Duration optimizationInterval, PowerValue binPower, boolean useBins) {
		this.moduleManager = moduleManager;
		this.regulationController = new RegulationController(moduleManager, deltaT, optimizationInterval);
		this.scheduler = new Scheduler(moduleManager, regulationController, optimizationInterval, binPower, useBins);
		//communication with aggregator
		this.moduleManager.getCommunicationModule().addListener(regulationController);
		this.moduleManager.getCommunicationModule().addListener(scheduler);
	}
	
	public void start() {
		//init components
		init();
		//create EV thread
		Time.getInstance().scheduleThread(moduleManager.getId(), this);
		this.isAlive = true;
	}
	
	private void init() {
		this.moduleManager.init();
		this.regulationController.init();
		this.scheduler.init();
	}

	@Override
	public void step() {
		regulationController.step();
	}

	public RegulationController getRegulationController() {
		return regulationController;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public void stop() {
		this.isAlive = false;
		this.moduleManager.stop();
	}
	
	public String getId() {
		return moduleManager.getId();
	}

	public EVModuleManager getModuleManager() {
		return moduleManager;
	}

}
