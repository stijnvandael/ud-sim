package electricvehicle.agent.controllers.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;
import clock.Time;

import communication.messages.DefectMessage;
import communication.messages.DefectRequestMessage;
import communication.messages.FlexMessage;
import communication.messages.Message;
import communication.messages.ScheduleMessage;
import communication.network.MessageListener;

import electricvehicle.agent.controllers.regulation.POPCalculatorInterface;
import electricvehicle.agent.controllers.regulation.RegulationInterface;
import electricvehicle.modules.EVModuleManager;

/**
 * 
 * Class for scheduling POP.
 * v1: basic functionality
 * v2: changes to imitate C code more accurately:
 * 		- validity flags instead of removal of data structures
 * 		- no timers any more
 * v3: bins
 * 
 * @author stijn
 *
 */
public class Scheduler implements MessageListener, POPCalculatorInterface {
	
	private final Logger logger;
	
	//static information
	private final EVModuleManager ev;
	private final RegulationInterface regulationController;
	private final Duration optimizationInterval;
	private final PowerValue binPower;

	//dynamic information
	//0. POP
	private boolean pop_requested;
	private long pop_requested_time;
	private long pop_requested_power;
	//1a. flexgraph
	private boolean valid_flexGraph;
	private long syncTime_flexGraph;
	private EnergyValue syncEnergy = new EnergyValue(0);
	private ArrayList<EnergyValue> flexEnergyMin;
	private ArrayList<EnergyValue> flexEnergyMax;
	//1b. bin-flexgraph
	private boolean useBins;
	private ArrayList<EnergyValue> binFlexEnergyMin;
	private ArrayList<EnergyValue> binFlexEnergyMax;
	//2. percentage schedule
	private boolean valid_percentageSchedule;
	private long syncTime_percentageSchedule;
	private long[] percentageSchedule;
	
	public Scheduler(EVModuleManager ev, RegulationInterface regulationController, Duration optimizationInterval, PowerValue binPower, boolean useBins) {
		this.logger = Logger.getLogger("ev.scheduler");
		this.ev = ev;
		this.regulationController = regulationController;
		this.optimizationInterval = optimizationInterval;
		this.binPower = binPower;
		this.useBins = useBins;
		//clear all data structures
		this.flexEnergyMin = new ArrayList<EnergyValue>();
		this.flexEnergyMax = new ArrayList<EnergyValue>();
		this.binFlexEnergyMin = new ArrayList<EnergyValue>();
		this.binFlexEnergyMax = new ArrayList<EnergyValue>();
		valid_flexGraph = false;
		valid_percentageSchedule = false;
		//register this class as POP calculator
		regulationController.registerPOPCalculator(this);
	}
	
	public void init() {
		//initialize timers
		initSchedulerTimer();
	}
	
	/**
	 * Timer to inform aggregator about current scheduling information
	 * TODO make random
	 */
	private void initSchedulerTimer() {
        TimerTask task = new TimerTask() {
        	  @Override
        	  public void run() {
        		  calculateFlexibility();
        	  }
        	};
        long scheduleRate = 30*1000;
        //Time.getInstance().scheduleAtFixedRate(ev.getId(), task, scheduleRate , new Random().nextInt((int)scheduleRate));
        Time.getInstance().scheduleRandom(ev.getId(), task, scheduleRate);
	}
	
	/**
	 * Dispatch messages
	 */
	@Override
	public synchronized void receive(Message message) {
		if(message instanceof ScheduleMessage) {
			receiveScheduleMessage((ScheduleMessage) message);
		}
	}
	
	/*****************************
	 * 
	 * FLEXGRAPH METHODS
	 * 
	 *****************************/
	
	/**
	 * Calculate and send flex information to aggregator
	 */
	public synchronized void calculateFlexibility() {
		
		logger.log(Level.INFO, "FLEX1: Calculating EV flexibility...");
		
		//check if POP of previous flexibility was requested
		if(valid_flexGraph == true && pop_requested == false && getSynchronizationTime(Time.getInstance().getDateTime()).getMillis() == syncTime_flexGraph + optimizationInterval.getMillis()) {
			logger.log(Level.INFO, "FLEX1: WARNING! new flexibility calculation postponed. POP not requested yet.");
			return;
		}else {
			pop_requested = false;
		}
		
		//clear old flexibility information
		this.valid_flexGraph = false;
		
		//current time
		DateTime time = Time.getInstance().getDateTime();	
		
		//calculate sync time/energy of flexibility information
		DateTime syncTime = getSynchronizationTime(time);
		setSyncEnergy(getSynchronizationEnergy(time));
		
		//check if there is still flexibility available
		if(syncTime.plus(getIntervalDuration()).isAfter(ev.getDepartureTime()) || ev.getMinEnergy(syncTime.plus(getIntervalDuration())).plus(getDeltaE()).inWH() > ev.getMaxEnergy().minus(getDeltaE()).inWH()) {
			logger.log(Level.INFO, "FLEX1: WARNING! No flexibility available any more.");
			return;
		}
		
		//validate flexgraph
		valid_flexGraph = true;
				
		//flexgraph
		flexEnergyMin.clear();
		flexEnergyMin.add(syncEnergy);
		DateTime flexTime = new DateTime(syncTime).plus(getIntervalDuration());
		EnergyValue flexEnergy = syncEnergy;
		while(flexTime.getMillis() <= ev.getDepartureTime().getMillis() && ev.getMinEnergy(flexTime).plus(getDeltaE()).inWH() <= ev.getMaxEnergy().minus(getDeltaE()).inWH()) {
			flexEnergy = flexEnergy.copy();
			//limit by Emin
			if(flexEnergy.inWH() < ev.getMinEnergy(flexTime).plus(getDeltaE()).inWH()) {
				flexEnergy = ev.getMinEnergy(flexTime).plus(getDeltaE()).copy();
			}
			//limit by Emax
			if(flexEnergy.inWH() > ev.getMaxEnergy().minus(getDeltaE()).inWH()) {
				flexEnergy = ev.getMaxEnergy().minus(getDeltaE()).copy();
			}
			//limit by Pmin
			EnergyValue Emin = ev.getBatteryModule().getPmin().multiply(optimizationInterval);
			if(flexEnergyMin.get(flexEnergyMin.size()-1).minus(flexEnergy).inWH() < Emin.inWH()) {
				flexEnergy = flexEnergyMin.get(flexEnergyMin.size()-1).minus(Emin);
			}
			//limit by Pmax
			EnergyValue Emax = ev.getBatteryModule().getPmax().multiply(optimizationInterval);
			if(flexEnergyMin.get(flexEnergyMin.size()-1).minus(flexEnergy).inWH() > Emax.inWH()) {
				flexEnergy = flexEnergyMin.get(flexEnergyMin.size()-1).minus(Emax);
			}
			//add  energy to flexGraph
			flexEnergyMin.add(flexEnergy);
			flexTime = flexTime.plus(this.optimizationInterval);
		}
		//make relative
		for(int i = 0; i < flexEnergyMin.size(); i++) {
			flexEnergyMin.set(i, new EnergyValue(flexEnergyMin.get(i).inWH() - syncEnergy.inWH()));
		}
		//flexgraph max
		flexEnergyMax.clear();
		flexEnergyMax.add(new EnergyValue(0));
		for(int i = 1; i < flexEnergyMin.size(); i++) {
			
			EnergyValue newE = flexEnergyMin.get(flexEnergyMin.size()-1).copy();
			
			//check if flexEnergyMin is decreasing
			//if so, flexEnergyMin = flexEnergyMax
			if(flexEnergyMin.get(i).inWH() < 0) {
				newE = flexEnergyMin.get(i).copy();
			}else{
				//limit by Pmin
				EnergyValue Emin = ev.getBatteryModule().getPmin().multiply(optimizationInterval);
				if(flexEnergyMax.get(flexEnergyMax.size()-1).minus(newE).inWH() < Emin.inWH()) {
					newE = flexEnergyMax.get(flexEnergyMax.size()-1).minus(Emin);
				}
				//limit by Pmax
				EnergyValue Emax = ev.getBatteryModule().getPmax().multiply(optimizationInterval);
				if(flexEnergyMax.get(flexEnergyMax.size()-1).minus(newE).inWH() > Emax.inWH()) {
					newE = flexEnergyMax.get(flexEnergyMax.size()-1).minus(Emax);
				}
			}

			flexEnergyMax.add(newE);
		}
		
		//calculate bin flexgraph
		calculateBinFlexGraph(flexEnergyMin.size()-1);

		//if flexgraph is successfully received, store
		this.syncTime_flexGraph = syncTime.getMillis();
		this.syncEnergy = syncEnergy;

		//2. SEND flexibility information
		FlexMessage flexMessage = null;
		//construct flexgraph and flexmax array if they are available
		if(flexEnergyMin.size() > 0 && flexEnergyMax.size() > 0) {
			if(useBins) {
				// flexgraph
				long[] flexGraphArray = toArray(binFlexEnergyMin);
				// flexgraph max
				long[] flexGraphMaxArray = toArray(binFlexEnergyMax);
				// flexmax
				long[] flexMaxArray = new long[binFlexEnergyMin.size()-1];
				for(int i = 0; i < flexMaxArray.length; i++) {
					flexMaxArray[i] = this.binPower.inWatt();
				}
				//reg powermax
				long[] regPowerMaxArray = new long[binFlexEnergyMin.size()-1];
				for(int i = 0; i < regPowerMaxArray.length; i++) {
					regPowerMaxArray[i] = -ev.getBatteryModule().getPmin().inWatt();
				}
				flexMessage = new FlexMessage(ev.getId(), (long)(syncTime_flexGraph/1000), flexGraphArray, flexGraphMaxArray, flexMaxArray, regPowerMaxArray);
				ev.getCommunicationModule().send(flexMessage);	
			}else{
				// flexgraph
				long[] flexGraphArray = toArray(flexEnergyMin);
				// flexgraph max
				long[] flexGraphMaxArray = toArray(flexEnergyMax);
				// flexmax
				long[] flexMaxArray = new long[flexEnergyMin.size()-1];
				for(int i = 0; i < flexMaxArray.length; i++) {
					flexMaxArray[i] = -ev.getBatteryModule().getPmin().inWatt();
				}
				flexMessage = new FlexMessage(ev.getId(), (long)(syncTime_flexGraph/1000), flexGraphArray, flexGraphMaxArray, flexMaxArray, flexMaxArray);
				ev.getCommunicationModule().send(flexMessage);	
			}
		}
	}
	
	private void calculateBinFlexGraph(int timeslots) {
		long Ereq = this.flexEnergyMin.get(this.flexEnergyMin.size()-1).inWH();
		long Pmax = this.binPower.inWatt();
		long EPmax = this.binPower.multiply(new Duration(900*1000)).inWH();
		long Pline = this.ev.getBatteryModule().getPmax().inWatt();
		long EPline = this.ev.getBatteryModule().getPmax().multiply(new Duration(900*1000)).inWH();
		
		long N = (1000*Ereq)/EPmax; // bv. 1.129 = 1129
		long N_round;
		if(N%1000 < 500){
		    N_round = N/1000;
		}else{
		    N_round = (N/1000) + 1;
		}
		
		long EPmax_bin;
		long Ereq_bin;
		//one timeslot left
		if(Ereq <= EPmax) {
			EPmax_bin = Ereq;
		    Ereq_bin = Ereq;
		}else {
		    long x = (100000*(Ereq - N_round*EPmax))/(N_round*EPmax + Ereq);
		    EPmax_bin = ((100000+x)*EPmax)/100000;
		    if(EPmax_bin > EPline) {
		        EPmax_bin = EPline;
		        x = (100000*(Ereq - N_round*EPline))/Ereq;
			}
		    Ereq_bin = ((100000-x)*Ereq)/100000;
		}
		
		//create bin flexgraph
		binFlexEnergyMin = new ArrayList<EnergyValue>();
		binFlexEnergyMax = new ArrayList<EnergyValue>();
		for(int n = 0; n <= timeslots; n++) {
			binFlexEnergyMin.add(new EnergyValue(Math.max(EPmax_bin*(n - (timeslots - N_round)), 0)));
			binFlexEnergyMax.add(new EnergyValue(Math.min(EPmax_bin*(n), EPmax_bin*N_round)));
		}
		System.out.println();
	}

	/**
	 *  Calculate Flex path
	 */
	public synchronized void receiveScheduleMessage(ScheduleMessage schedMessage) {
		//preconditions
		//1. flexgraph should be valid
		//2. flexgraph should be shorter or equal as percentageplan
		//3. synctime of flexgraph should be the same
		if(this.valid_flexGraph == true) {
			if(this.flexEnergyMin.size() <= schedMessage.getPercentagePlan().length && this.flexEnergyMax.size() <= schedMessage.getPercentagePlan().length) {
				if(schedMessage.getSyncTime() == this.syncTime_flexGraph) {
					this.syncTime_percentageSchedule = schedMessage.getSyncTime();
					this.percentageSchedule = Arrays.copyOfRange(schedMessage.getPercentagePlan(), 0, this.flexEnergyMin.size());
					this.valid_percentageSchedule = true;
				}
			}	
		}
	}

	@Override
	public PowerValue calculatePOP() {
		logger.log(Level.INFO, "FLEX_POP: Start generating POP... (time: " + Time.getInstance().toString() + ")");
		
		pop_requested = true;
		
		long currentTime = Time.getInstance().getDateTime().getMillis();
		
		PowerValue popRequestedPower;
		//check if requested time is within first optimization interval
		if(currentTime < syncTime_flexGraph || currentTime > syncTime_flexGraph + optimizationInterval.getMillis()) {
			logger.log(Level.INFO, "FLEX_POP: WARNING! current time " + currentTime + " is not within first optimization interval " + syncTime_flexGraph + " - " + (syncTime_flexGraph + optimizationInterval.getMillis()) + " (validity=" + valid_flexGraph + ").");
			popRequestedPower = this.regulationController.getCurrentPOP();
		}
	    else{
			//generate solution path
	    	ArrayList<EnergyValue> solutionPath = generateSolutionPath();

	    	if(solutionPath.isEmpty()) {
	    		logger.log(Level.INFO, "FLEX_POP: WARNING! No solution path.");
	    		popRequestedPower = new PowerValue(0);
	    	}else{
	    		//extract pop from first step in solution path
	    		popRequestedPower = new PowerValue(0);
	    		try{
		    		popRequestedPower = new PowerValue(-solutionPath.get(1).divide(getIntervalDuration()).inWatt());
	    		}catch(java.lang.IndexOutOfBoundsException e) {
	    		}
	    	}
		}

		logger.log(Level.INFO, "FLEX_POP: New POP is " + popRequestedPower + ".");
		logger.log(Level.INFO, "FLEX_POP: End generating POP.");

		return popRequestedPower;
	}

	/*****************************
	 * 
	 * FLEXPATH METHODS
	 * 
	 *****************************/
	public ArrayList<EnergyValue> generateSolutionPath() {
		//preconditions for adding percentages
		//1. synctimes should be the same
		//2. length should be the same
		ArrayList<EnergyValue> flexPath = new ArrayList<EnergyValue>();
		if(this.valid_flexGraph == false || this.valid_percentageSchedule == false || syncTime_flexGraph != syncTime_percentageSchedule || percentageSchedule.length != flexEnergyMin.size()) {
			return flexPath;
		}
		
		//add percentage path
		for(int i = 0; i < this.flexEnergyMin.size(); i++) {
			//relative position in flex graph
			long newE;
			if(useBins) {
				newE = binFlexEnergyMin.get(i).inWH() + (percentageSchedule[i] * binFlexEnergyMax.get(i).minus(binFlexEnergyMin.get(i)).inWH())/1000;
			}else{
				newE = flexEnergyMin.get(i).inWH() + (percentageSchedule[i] * flexEnergyMax.get(i).minus(flexEnergyMin.get(i)).inWH())/1000;	
			}
			//add new energy to flexpath
			flexPath.add(new EnergyValue(newE));
		}
		return flexPath;
	}

	/*****************************
	 * 
	 * SYNCHRONIZATION METHODS
	 * 
	 *****************************/

	/**
	 * calculate next synchronization time
	 */
	private DateTime getSynchronizationTime(DateTime time) {
		return time.plusMillis((int) (optimizationInterval.getMillis()-(time.getMillisOfDay()%optimizationInterval.getMillis())));
	}
	
	/**
	 * calculate next energy value
	 */
	private EnergyValue getSynchronizationEnergy(DateTime time) {
		Duration timeUntilNextSync = new Duration(time, getSynchronizationTime(time));
		return this.ev.getBatteryModule().getCurrentEnergy().minus(this.regulationController.getCurrentPOP().multiply(timeUntilNextSync));
	}
	
	public EnergyValue getDeltaE() {
		return ev.getBatteryModule().getPmax().multiply(regulationController.getDeltaT());
	}

	/*****************************
	 * 
	 * GETTERS AND SETTERS
	 * 
	 *****************************/
	
	public Duration getIntervalDuration() {
		return optimizationInterval;
	}

	public long getSyncTime() {
		return syncTime_flexGraph;
	}
	
	public synchronized void setSyncEnergy(EnergyValue v) {
		this.syncEnergy = v;
	}

	public synchronized EnergyValue getSyncEnergy() {
		return syncEnergy;
	}
	
	public synchronized ArrayList<EnergyValue> getFlexMin() {
		if(!this.valid_flexGraph) {
			return new ArrayList<EnergyValue>();
		}
		//deep copy of flexgraph
		ArrayList<EnergyValue> arrayCopy = new ArrayList<EnergyValue>();
		for(EnergyValue e: this.flexEnergyMin) {
			arrayCopy.add(e.copy());
		}
		return arrayCopy;
	}
	
	public synchronized ArrayList<EnergyValue> getFlexMax() {
		if(!this.valid_flexGraph) {
			return new ArrayList<EnergyValue>();
		}
		//deep copy of flexgraphmax
		ArrayList<EnergyValue> arrayCopy = new ArrayList<EnergyValue>();
		for(EnergyValue e: this.flexEnergyMax) {
			arrayCopy.add(e.copy());
		}
		return arrayCopy;
	}

	public synchronized ArrayList<EnergyValue> getBinFlexMin() {
		if(!this.valid_flexGraph) {
			return new ArrayList<EnergyValue>();
		}
		//deep copy of flexgraph
		ArrayList<EnergyValue> arrayCopy = new ArrayList<EnergyValue>();
		for(EnergyValue e: this.binFlexEnergyMin) {
			arrayCopy.add(e.copy());
		}
		return arrayCopy;
	}
	
	public synchronized ArrayList<EnergyValue> getBinFlexMax() {
		if(!this.valid_flexGraph) {
			return new ArrayList<EnergyValue>();
		}
		//deep copy of flexgraphmax
		ArrayList<EnergyValue> arrayCopy = new ArrayList<EnergyValue>();
		for(EnergyValue e: this.binFlexEnergyMax) {
			arrayCopy.add(e.copy());
		}
		return arrayCopy;
	}
	
	/**
	 * 
	 * compare two arrays
	 * 
	 */
	public boolean isSame(ArrayList<EnergyValue> list1, ArrayList<EnergyValue> list2) {
		if(list1.size() != list2.size()) {
			return false;
		}
		for(int i = 0; i < list1.size(); i++) {
			if(!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Helper method to convert arraylist of energyvalues to long[]
	 * 
	 * @param list
	 * @return
	 */
	public synchronized long[] toArray(ArrayList<EnergyValue> list) {
		long[] result = new long[list.size()];
		for(int i = 0; i < list.size(); i++) {
			result[i] = list.get(i).inWH();
		}
		return result;
	}
	
}
