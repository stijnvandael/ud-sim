package aggregator.agent;

import org.joda.time.Duration;

import aggregator.agent.controllers.regulation.RegulationController;
import aggregator.agent.controllers.scheduler.Scheduler;
import aggregator.modules.AggregatorModuleManager;

public class AggregatorAgent {

	private AggregatorModuleManager moduleManager;
	private RegulationController regulationController;
	private Scheduler scheduler;
	
	public AggregatorAgent(AggregatorModuleManager moduleManager, Duration optimizationInterval, boolean naieve, boolean useBins) {
		this.moduleManager = moduleManager;
		this.regulationController = new RegulationController(moduleManager);
		this.scheduler = new Scheduler(moduleManager, naieve, useBins);
		//interaction with PJM
		moduleManager.getPjmModule().addListener(regulationController);
	}
	
	public void start() {
		this.moduleManager.init();
	}

	public RegulationController getRegulationController() {
		return regulationController;
	}

	public AggregatorModuleManager getModuleManager() {
		return moduleManager;
	}
	
	public Scheduler getScheduler() {
		return scheduler;
	}

	public void stop() {
		this.regulationController.stop();
	}

}
