package communication.messages;

import org.json.simple.JSONObject;


public class DefectRequestMessage extends Message {

	public DefectRequestMessage(String id) {
		super(id, "defectrequest");
	}
	
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        return obj;
    }
	
    public static DefectRequestMessage fromJSON(JSONObject object) {
        String id = (String)object.get("id");
        return new DefectRequestMessage(id);
    }
	
}
