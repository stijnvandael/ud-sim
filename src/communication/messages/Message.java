package communication.messages;

import org.json.simple.JSONObject;

public abstract class Message {
	
	private String id;
	private String type;
	
	public Message(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("type",this.type);
		obj.put("id", id);
		return obj;
	}

	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}

}
