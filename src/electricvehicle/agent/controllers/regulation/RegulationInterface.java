package electricvehicle.agent.controllers.regulation;

import org.joda.time.Duration;

import units.PowerValue;

public interface RegulationInterface {

	public Duration getDeltaT();
	
	public int getState();
	
	public PowerValue getCurrentPOP();
	
	public void registerPOPCalculator(POPCalculatorInterface popCalculator);
	
}
