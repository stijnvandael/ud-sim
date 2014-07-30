package aggregator.modules.communication;

import utils.ArrayMath;
import communication.messages.DefectMessage;
import communication.messages.FlexMessage;
import communication.messages.Message;
import communication.messages.StatusMessage;
import communication.network.MessageListener;
import communication.network.Session;

/**
 * Session which maintains all the information from a connected EV
 * Note: all methods are synchronized as fields are accessed by both aggregator and EV
 * 
 * @author Stijn
 *
 */
public class EVInformation implements MessageListener {

	private String id;
	
	private Session session;
	
	//status information
	private long power; //Watt
	private long pop; //Watt
	private long upRegulation; //Watt
	private long downRegulation; //Watt
	
	//scheduling information
	private long scheduleSyncTime; //s
	private long scheduleSyncEnergy; //Wh
	private long[] flexMin; //Watt
	private long[] flexMax; //Watt
	private long[] flexPower; //Watt
	private long[] regMaxPower;

	//repair/defect information
	private long defectSyncTime; //s
	private long[] defects;
	
	public EVInformation(Session session) {
		this.session = session;
		this.session.register(this);
		power = 0;
		pop = 0;
		upRegulation = 0;
		downRegulation = 0;
		scheduleSyncTime = 0;
		scheduleSyncEnergy = 0;
		flexMin = new long[0];
		flexMax = new long[0];
		flexPower = new long[0];
		regMaxPower = new long[0];
		defects = new long[0];
		defects = new long[0]; 
	}

	@Override
	public synchronized void receive(Message message) {
		if(message.getType().equals("status")) {
			StatusMessage statusMessage = (StatusMessage) message;
			this.id = statusMessage.getId();
			this.power = statusMessage.getPower();
			this.upRegulation = statusMessage.getUpReg();
			this.downRegulation = statusMessage.getDownReg();
			this.pop = statusMessage.getPop();
		}else
		if(message.getType().equals("flex")) {
			FlexMessage flexMessage = (FlexMessage) message;
			this.scheduleSyncTime = flexMessage.getSyncTime();
			this.flexMin = flexMessage.getFlexGraph();
			this.flexMax = flexMessage.getFlexGraphMax();
			this.flexPower = flexMessage.getFlexMax();
			this.regMaxPower = flexMessage.getRegMaxPower();
		}
	}
	
	public synchronized FlexGraph getFlexGraph() {
		//if V2G flexgraph, subtract flexibility graph change from power array
		//TODO do this post-processing at the EV agent
		if(hasV2GFlexGraph()) {
			//ArrayMath.show(evSession.getFlexGraphMax());
			long[] arr = ArrayMath.diff(flexMax);
			//ArrayMath.show(arr);
			arr = ArrayMath.multiply(arr, -4);
			//ArrayMath.show(arr);
			flexMax = ArrayMath.sumArray(flexMax, arr);
			//ArrayMath.show(totalFlexMax);
		}
		return new FlexGraph(	hasV2GFlexGraph()?new long[flexMin.length]:flexMin, 
								hasV2GFlexGraph()?new long[flexMax.length]:flexMax,
								flexPower,
								regMaxPower,this);
	}
	
	public synchronized boolean hasV2GFlexGraph() {
		for(int i = 0; i < flexMin.length-1; i++) {
			if(flexMin[i+1] - flexMin[i] < 0) {
				return true;
			}
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public synchronized long getPower() {
		return power;
	}

	public synchronized long getPop() {
		return pop;
	}

	public synchronized long getUpRegulation() {
		return upRegulation;
	}

	public synchronized long getDownRegulation() {
		return downRegulation;
	}

	public synchronized long getScheduleSyncTime() {
		return scheduleSyncTime;
	}
	
	public synchronized long getScheduleSyncEnergy() {
		return scheduleSyncEnergy;
	}
	
	public Session getSession() {
		return session;
	}
	
}
