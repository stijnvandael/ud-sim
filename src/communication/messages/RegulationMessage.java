package communication.messages;

import org.json.simple.JSONObject;


public class RegulationMessage extends Message {

    private final long alpha;
    private final long alphaMax;
    
    public RegulationMessage(String id, long alpha2, long alphaMax2) {
        super(id, "reg");
        this.alpha = alpha2;
        this.alphaMax = alphaMax2;
    }
    
    public long getAlpha() {
		return alpha;
	}

	public long getAlphaMax() {
		return alphaMax;
	}

	public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("alpha",this.alpha);
        obj.put("alphamax",this.alphaMax);
        return obj;
    }

	public static RegulationMessage fromJSON(JSONObject object) {
		String id = (String)object.get("id");
		long alpha = (Long)object.get("alpha");
		long alphaMax = (Long)object.get("alphamax");
		return new RegulationMessage(id, alpha, alphaMax);
	}
    
}