package electricvehicle.agent.controllers.regulation;

import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;
import clock.Time;

import communication.messages.Message;
import communication.messages.RegulationMessage;
import communication.messages.StatusMessage;
import communication.network.MessageListener;

import electricvehicle.modules.EVModuleManager;
import exceptions.BatteryException;
import exceptions.POPoutOfRangeException;

public class RegulationController implements MessageListener, RegulationInterface {

	private final Logger logger;
	
	//static information
	private final EVModuleManager ev;
	private final Duration deltaT;

	//dynamic information
	private POPCalculatorInterface popCalculator;
	private int state;
	private PowerValue pop;
	private PowerValue downRegulation;
	private PowerValue upRegulation;
	private PowerValue reqRegulation;
	
	//pop change variables
	private Duration optimizationInterval;
	private DateTime nextPopChangeTime;

	public RegulationController(EVModuleManager ev, Duration deltaT, Duration optimizationInterval) {
		this.logger = Logger.getLogger("ev.regulationcontroller");
		this.ev = ev;
		this.deltaT = deltaT;
		this.optimizationInterval = optimizationInterval;
		this.pop = new PowerValue(0);
		this.downRegulation = new PowerValue(0);
		this.upRegulation = new PowerValue(0);
		this.reqRegulation = new PowerValue(0);
		this.state = -1;
	}
	
	public void init() {
		initRegulationTimer();
	}
	
	private void initRegulationTimer() {
		//create timer to inform aggregator about current regulation
		TimerTask task = new TimerTask() {
        	  @Override
        	  public void run() {
        		  StatusMessage statusMessage = new StatusMessage(ev.getId(), ev.getBatteryModule().getCurrentPower().inWatt(), pop.inWatt(), upRegulation.inWatt(), downRegulation.inWatt());
        		  ev.getCommunicationModule().send(statusMessage);
        	  }
        	};
        	Time.getInstance().scheduleAtFixedRate(ev.getId(), task, 20000, 0);
	}

	public void step() {
		
		//check if EV is not undercharged
		if(Time.getInstance().getDateTime().isAfter(ev.getDepartureTime()) && ev.getBatteryModule().getCurrentEnergy().inWH() < ev.getRequiredEnergy().inWH()) {
			logger.log(Level.ERROR, "Error! EV wasn't charged enough - state is " + this.state);
		}
		
		//record previous state
		int prevState = this.state;
		//CASE 5: after departure
		if(Time.getInstance().getDateTime().getMillis() >= ev.getDepartureTime().getMillis()) {
			this.state = Math.max(5, this.state);
		}else
		//CASE 4: emergency charging
		if(ev.getBatteryModule().getCurrentEnergy().inWH() <= ev.getMinRequiredEnergy(Time.getInstance().getDateTime()).inWH()) {
			this.state = Math.max(4, this.state);	
		}else
		//CASE 1: lower SoC charging
		if(ev.getBatteryModule().getCurrentEnergy().inWH() <= ev.getBatteryModule().getMinEnergy().inWH()) {
			this.state = Math.max(1, this.state);
		}else
		//CASE 2: high SoC charging
		if(ev.getBatteryModule().getCurrentEnergy().inWH() >= ev.getBatteryModule().getMaxEnergy().inWH() && (Time.getInstance()).getDateTime().isBefore(this.ev.getDepartureTime().minus(3600*1000))) {
			this.state = Math.max(2, this.state);
		}else{
			//CASE 3 POP charging
			this.state = Math.max(3, this.state);
		}

		switch (this.state) {
			case 1:
				//this.setPOP(ev.getBatteryModule().getPmin(), false);
				//break;
				//no POP check necessary, direct charging
				this.pop = this.ev.getBatteryModule().getPmin();
				try {
					this.ev.getBatteryModule().setPower(this.pop);
				} catch (BatteryException e) {};
				break;
			case 2:
				//this.setPOP(ev.getBatteryModule().getPmax(), false);
				//break;
				//no POP check necessary, direct charging
				this.pop = this.ev.getBatteryModule().getPmax();
				try {
					this.ev.getBatteryModule().setPower(this.pop);
				} catch (BatteryException e) {};
				break;
			case 3:
				//only change POP every 15 minutes OR when state changed			
				if(prevState != state || Time.getInstance().getDateTime().isAfter(nextPopChangeTime)) {
					if(this.popCalculator != null) {
						this.setPOP(this.popCalculator.calculatePOP(), false);
						logger.log(Level.INFO, "New POP: " + this.pop);
					}else {
						logger.log(Level.INFO, "Warning! No POP calculator registered. POP set to zero.");
						this.setPOP(new PowerValue(0), false);
					}
					this.nextPopChangeTime = getSynchronizationTime(Time.getInstance().getDateTime());
				}
				break;
			case 4:
				//no POP check necessary, direct charging
				this.pop = this.ev.getBatteryModule().getPmin();
				try {
					this.ev.getBatteryModule().setPower(this.pop);
				} catch (BatteryException e) {};
				break;
			case 5:
				this.setPOP(new PowerValue(0), true); //popsetpoint is ignored
				break;
			default:
				break;
		}
	}
	
	/**
	 * calculate next synchronization time
	 */
	private DateTime getSynchronizationTime(DateTime time) {
		return time.plusMillis((int) (optimizationInterval.getMillis()-(time.getMillisOfDay()%optimizationInterval.getMillis())));
	}
	
//	public PowerValue calculatePOP(EnergyValue e) {
//		DateTime t = Time.getInstance().getDateTime();
//		PowerValue EPMax = getEPMax(e, t);
//		PowerValue EPMin = getEPMax(e, t);
//		PowerValue pop = new PowerValue(EPMax.plus(EPMin).inWatt()/2);
//		return pop;
//	}
	
	public PowerValue getEPMax(EnergyValue e, DateTime t) {
		return new PowerValue(Math.min(e.minus(ev.getMinEnergy(t.plus(deltaT))).divide(getDeltaT()).inWatt(), ev.getBatteryModule().getPmax().inWatt()));
	}
	
	public PowerValue getEPMin(EnergyValue e) {
		return new PowerValue(Math.max(e.minus(ev.getMaxEnergy()).divide(getDeltaT()).inWatt(), ev.getBatteryModule().getPmin().inWatt()));

	}

	/**
	 * Set the POP of the EV
	 * 
	 * @param 	power
	 * 			pop - power in W
	 * @throws POPoutOfRangeException 
	 * @throws BatteryException 
	 */
	public void setPOP(PowerValue power, boolean maximizeRegulation) {
		logger.log(Level.INFO, "set POP to " + power.inKWatt());
		//Check POP boundaries
		EnergyValue energy = this.getEv().getBatteryModule().getCurrentEnergy();
		PowerValue EPMax = getEPMax(energy, Time.getInstance().getDateTime());
		PowerValue EPMin = getEPMin(energy);
		logger.log(Level.INFO, "EPMax = " + EPMax.inWatt());
		logger.log(Level.INFO, "EPMin = " + EPMin.inWatt());
		//limit by EPMax and EPMin
		if(EPMax.inWatt() < EPMin.inWatt()) {
			EPMin = EPMax.copy();
		}
		if(power.inWatt() > EPMax.inWatt()) {
			power = EPMax.copy();
		}else
		if(power.inWatt() < EPMin.inWatt()) {
			power = EPMin.copy();
		}
		logger.log(Level.INFO, "EPMax = " + EPMax.inWatt());
		logger.log(Level.INFO, "EPMin = " + EPMin.inWatt());
		//limit by POP
		PowerValue refPop = new PowerValue(EPMax.plus(EPMin).inWatt()/2);
		if(maximizeRegulation || (refPop.inWatt() < 0 && power.inWatt() > refPop.inWatt())) {
			power = refPop.copy();
		}else
		if(refPop.inWatt() > 0 && power.inWatt() < refPop.inWatt()){
			power = refPop.copy();
		}
		//set POP, UPregulation and DOWNregulation
		this.pop = power;
		this.upRegulation = new PowerValue(Math.min(EPMax.minus(power).inWatt(), -EPMin.minus(power).inWatt()));
		this.downRegulation = new PowerValue(-upRegulation.inWatt());
		//start charging accordingly, if EV is not connected to aggregator.
		//otherwise, the regulation signal will control the charging
		try {
			if(!this.ev.getCommunicationModule().isConnectedToServer()) {// || maximizeRegulation==true) {
				ev.getBatteryModule().setPower(this.pop);
			}
		} catch (BatteryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(Message message) {
		if(message.getType().equals("reg")) {
			RegulationMessage regMessage = (RegulationMessage) message;
			//TODO make difference up and down!
			PowerValue power = new PowerValue((int) (-getDownRegulation().inWatt()*(((double)regMessage.getAlpha()/regMessage.getAlphaMax()))));
			reqRegulation = power.copy();
			try {
				PowerValue newPower = getCurrentPOP().plus(power);
				if(newPower.inWatt() > ev.getBatteryModule().getPmax().inWatt()) {
					newPower = new PowerValue(ev.getBatteryModule().getPmax().inWatt());
				}
				if(newPower.inWatt() < ev.getBatteryModule().getPmin().inWatt()) {
					newPower = new PowerValue(ev.getBatteryModule().getPmin().inWatt());
				}
				ev.getBatteryModule().setPower(newPower);
			} catch (BatteryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Duration getDeltaT() {
		return deltaT;
	}
	
	public EnergyValue getDeltaE() {
		return ev.getBatteryModule().getPmax().multiply(deltaT);
	}
	
	public void setState(DateTime time, int state) {
		this.state = state;
	}

	public EVModuleManager getEv() {
		return ev;
	}

	public PowerValue getCurrentPOP() {
		return pop;
	}

	public PowerValue getDownRegulation() {
		return downRegulation;
	}

	public PowerValue getUpRegulation() {
		return upRegulation;
	}
	
	public int getState() {
		return this.state;
	}

	public PowerValue getReqRegulation() {
		return reqRegulation;
	}

	@Override
	public void registerPOPCalculator(POPCalculatorInterface popCalculator) {
		this.popCalculator = popCalculator;
	}

}
