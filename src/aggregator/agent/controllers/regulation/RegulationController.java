package aggregator.agent.controllers.regulation;


import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import units.PowerValue;
import aggregator.modules.AggregatorModuleManager;
import aggregator.modules.pjm.PJMListener;
import clock.Time;

import communication.messages.RegulationMessage;

public class RegulationController implements PJMListener {

	private final AggregatorModuleManager aggregator;
	private final Duration bidDuration = new Duration(3600*1000);
	
	private int currentRegSignal;

	public RegulationController(AggregatorModuleManager agg) {
		this.aggregator = agg;
		//create timer to hourly change scale for PJM regulation signal
		TimerTask t = new TimerTask() {
			public void run() {
				long[] bids = aggregator.getBidModule().getBids(Time.getInstance().getDateTime(), 1);
				aggregator.getPjmModule().setMaxRegulation(new PowerValue(bids[0]));
			};
		};
		Time.getInstance().scheduleHourlyTask("aggregator_controller", t);
	}

	@Override
	public void receiveRegulationSignal(int regSignal) {
		this.currentRegSignal = regSignal;
		// always send regulation signal to EVs
		int alphaMax = 100; //parameter
		int alpha;
		if(regSignal > 0) {
			alpha = (int)Math.round((regSignal*((double)alphaMax))/(double)aggregator.getEVSessionManager().getTotalUpReg().inWatt());
			if(aggregator.getEVSessionManager().getTotalUpReg().inWatt() == 0)
				alpha = 0;
		}else{
			alpha = -(int)Math.round((regSignal*((double)alphaMax))/(double)aggregator.getEVSessionManager().getTotalDownReg().inWatt());
			if(aggregator.getEVSessionManager().getTotalDownReg().inWatt() == 0)
				alpha = 0;
		}
		
		RegulationMessage message = new RegulationMessage(aggregator.getId(), alpha, alphaMax);
		this.aggregator.getEVSessionManager().sendMessage(message);
	}

	public int getCurrentRegSignal() {
		return currentRegSignal;
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
