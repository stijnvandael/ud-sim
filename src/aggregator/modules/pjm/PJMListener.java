package aggregator.modules.pjm;

/**
 * Interface from PJM module to agent
 * 
 * @author Stijn
 *
 */
public interface PJMListener {

	/**
	 * PJM regulation signal in kW
	 * 
	 * @param regSignal
	 */
	public void receiveRegulationSignal(int regSignal);
	
}
