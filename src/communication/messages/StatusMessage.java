package communication.messages;

import org.json.simple.JSONObject;

public class StatusMessage extends Message {

	private long power; //Watt
	private long pop; //Watt
	private long upReg; //Watt
	private long downReg; //Watt
		
	public StatusMessage(String id, long power, long pop, long upReg, long downReg) {
		super(id, "status");
		this.power = power;
		this.pop = pop;
		this.upReg = upReg;
		this.downReg = downReg;
	}

	public JSONObject toJSON() {
		JSONObject obj = super.toJSON();
		obj.put("power", this.power);
		obj.put("pop", this.pop);
		obj.put("upReg", this.upReg);
		obj.put("downReg", this.downReg);
		return obj;
	}
	
	public static StatusMessage fromJSON(JSONObject object) {
		String id = (String)object.get("id");
		long power = (Long)object.get("power");
		long pop = (Long)object.get("pop");
		long upReg = (Long)object.get("upReg");
		long downReg = (Long)object.get("downReg");
		return new StatusMessage(id, power, pop, upReg, downReg);
	}

	public long getPower() {
		return power;
	}
	
	public long getPop() {
		return pop;
	}

	public long getUpReg() {
		return upReg;
	}

	public long getDownReg() {
		return downReg;
	}

}
