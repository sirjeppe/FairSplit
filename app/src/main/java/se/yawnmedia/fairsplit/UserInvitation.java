package se.yawnmedia.fairsplit;

import android.util.Log;

import org.json.JSONObject;

public class UserInvitation {
    public int groupID;
    public String userName;
    public String sender;

    public UserInvitation(Integer groupID, String userName, String sender) {
        this.groupID = groupID;
        this.userName = userName;
        this.sender = sender;
    }

    @Override
    public String toString() {
        JSONObject userInvitation = this.toJSONObject();
        return userInvitation.toString();
    }

    public JSONObject toJSONObject() {
        JSONObject userInvitation = new JSONObject();
        try {
            userInvitation.put("groupID", this.groupID);
            userInvitation.put("userName", this.userName);
            userInvitation.put("sender", this.sender);
        } catch (Exception ex) {
            Log.e("UserInvitation.toString()", ex.getMessage());
        }
        return userInvitation;
    }
}
