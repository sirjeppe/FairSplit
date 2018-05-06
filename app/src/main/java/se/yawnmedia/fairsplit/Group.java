package se.yawnmedia.fairsplit;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Group {
    public int groupID;
    public String groupName;
    public ArrayList<Integer> members;

    public Group(JSONObject group) {
        try {
            this.groupID = group.getInt("groupID");
            this.groupName = group.getString("groupName");
            this.members = new ArrayList<>();
            JSONArray members = group.getJSONArray("members");
            if (members.length() > 0) {
                for (int m = 0; m < members.length(); m++) {
                    this.members.add(members.getInt(m));
                }
            }
        } catch (Exception ex) {
            Log.e("Group(JSONObject)", ex.getMessage());
        }
    }

    @Override
    public String toString() {
        JSONObject group = new JSONObject();
        try {
            group.put("groupID", this.groupID);
            group.put("groupName", this.groupName);
            group.put("members", this.members);
        } catch (Exception ex) {
            Log.e("Group.toString()", ex.getMessage());
        }
        return group.toString();
    }
}
