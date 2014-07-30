package communication.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ScheduleMessage extends Message {

    private final long syncTime;
    private final long[] percentagePlan;
    
    public ScheduleMessage(String id, long syncTime, long[] percentagePlan) {
        super(id, "schedule");
        this.syncTime = syncTime;
        this.percentagePlan = percentagePlan;
    }
    
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("synctime", this.syncTime);
        JSONArray percentagePlanList = new JSONArray();
        for(long l: this.percentagePlan) {
        	percentagePlanList.add(new Long(l));
        }
        obj.put("percentageplan", percentagePlanList);
        return obj;
    }

    public static ScheduleMessage fromJSON(JSONObject object) {
        String id = (String)object.get("id");
        long syncTime = (Long)object.get("synctime");
        JSONArray percentagePlanArray = (JSONArray)object.get("percentageplan");
        long[] percentagePlan = new long[percentagePlanArray.size()];
        for(int t = 0; t < percentagePlan.length; t++) {
            percentagePlan[t] = (Long) percentagePlanArray.get(t);
        }
        return new ScheduleMessage(id, syncTime, percentagePlan);
    }

	public long getSyncTime() {
		return syncTime;
	}

	public long[] getPercentagePlan() {
		return percentagePlan;
	}
    
}