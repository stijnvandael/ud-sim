package communication.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DefectMessage extends Message {

    private long syncTime;
    private final long[] defects; //energy in Wh
    
    public DefectMessage(String id, long syncTime, long[] defects) {
        super(id, "defects");
        this.syncTime = syncTime;
        this.defects = defects;
    }

    public long getSyncTime() {
        return syncTime;
    }

    public long[] getDefects() {
        return defects;
    }

    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("synctime",this.syncTime);
        JSONArray defectList = new JSONArray();
        for(long l: this.defects) {
            defectList.add(new Long(l));
        }
        obj.put("defects", defectList);        
        return obj;
    }

    public static DefectMessage fromJSON(JSONObject object) {
        String id = (String)object.get("id");
        long syncTime = (Long)object.get("synctime");
        JSONArray defectList = (JSONArray)object.get("defects");
        long[] defects = new long[defectList.size()];
        for(int i = 0; i < defectList.size(); i++) {
            defects[i] = (Long) defectList.get(i);
        }
        return new DefectMessage(id, syncTime, defects);
    }
    
}