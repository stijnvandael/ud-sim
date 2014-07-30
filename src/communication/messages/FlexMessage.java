package communication.messages;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FlexMessage extends Message {

	private final long syncTime;
	private final long[] flexGraph; //energy in Wh
	private final long[] flexGraphMax;
	private final long[] flexMax; //power in W
	private final long[] regPowerMax; //power in W

	public FlexMessage(String id, long syncTime, long[] flexGraph, long[] flexGraphMax, long[] flexMax, long[] regPowerMax) {
		super(id, "flex");
		this.syncTime = syncTime;
		this.flexGraph = flexGraph;
		this.flexGraphMax = flexGraphMax;
		this.flexMax = flexMax;
		this.regPowerMax = regPowerMax;
		if(flexGraph.length != flexGraphMax.length || flexGraph.length != flexMax.length + 1) {
			Logger.getLogger(this.getClass()).log(Level.ERROR, "Invalid flexmessage");
		}
	}

	public long getSyncTime() {
		return syncTime;
	}
	
	public long[] getFlexGraph() {
		return flexGraph;
	}
	
	public long[] getFlexGraphMax() {
		return flexGraphMax;
	}
	
	public long[] getFlexMax() {
		return flexMax;
	}
	
	public long[] getRegMaxPower() {
		return regPowerMax;
	}
	
	public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("synctime",this.syncTime);
        JSONArray flexGraphList = new JSONArray();
        for(long l: this.flexGraph) {
        	flexGraphList.add(new Long(l));
        }
        obj.put("flexgraph", flexGraphList);
        JSONArray flexMaxList = new JSONArray();
        for(long l: this.flexMax) {
        	flexMaxList.add(new Long(l));
        }
        obj.put("flexmax", flexMaxList);
        JSONArray flexGraphMaxList = new JSONArray();
        for(long l: this.flexGraphMax) {
        	flexGraphMaxList.add(new Long(l));
        }
        obj.put("flexgraphmax", flexGraphMaxList);     
        JSONArray flexPowerMaxList = new JSONArray();
        for(long l: this.regPowerMax) {
        	flexPowerMaxList.add(new Long(l));
        }
        obj.put("regpowermax", flexPowerMaxList);
        return obj;
    }
	
	public static FlexMessage fromJSON(JSONObject object) {
		String id = (String)object.get("id");
		long syncTime = (Long)object.get("synctime");
		JSONArray flexGraphList = (JSONArray)object.get("flexgraph");
		long[] flexGraph = new long[flexGraphList.size()];
        for(int i = 0; i < flexGraphList.size(); i++) {
        	flexGraph[i] = (Long) flexGraphList.get(i);
        }
        JSONArray flexGraphMaxList = (JSONArray)object.get("flexgraphmax");
		long[] flexGraphMax = new long[flexGraphMaxList.size()];
        for(int i = 0; i < flexGraphMaxList.size(); i++) {
        	flexGraphMax[i] = (Long) flexGraphMaxList.get(i);
        }
        JSONArray flexMaxList = (JSONArray)object.get("flexmax");
		long[] flexMax = new long[flexMaxList.size()];
        for(int i = 0; i < flexMaxList.size(); i++) {
        	flexMax[i] = (Long) flexMaxList.get(i);
        }
        JSONArray regPowerMaxList = (JSONArray)object.get("regpowermax");
		long[] regPowerMax = new long[regPowerMaxList.size()];
        for(int i = 0; i < regPowerMaxList.size(); i++) {
        	regPowerMax[i] = (Long) regPowerMaxList.get(i);
        }
		return new FlexMessage(id, syncTime, flexGraph, flexGraphMax, flexMax, regPowerMax);
	}

}
