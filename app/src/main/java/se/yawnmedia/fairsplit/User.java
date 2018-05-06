package se.yawnmedia.fairsplit;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class User {
    public int userID;
    public String userName;
    public int salary;
    public ArrayList<Integer> groups;
    public ArrayList<Transaction> transactions;
    public String apiKey;
    public long keyValidTo;

    public User(JSONObject user) {
        try {
            this.userID = user.getInt("userID");
            this.userName = user.getString("userName");
            this.salary = user.getInt("salary");
            this.apiKey = user.getString("apiKey");
            this.keyValidTo = user.getLong("keyValidTo");
            this.groups = new ArrayList<>();
            JSONArray groups = user.getJSONArray("groups");
            if (groups.length() > 0) {
                for (int g = 0; g < groups.length(); g++) {
                    this.groups.add(groups.getInt(g));
                }
            }
        } catch (Exception ex) {
            Log.e("User(JSONObject)", ex.getMessage());
        }
    }

    public User(String user) {
        try {
            JSONObject userObject = new JSONObject(user);
            this.userID = userObject.getInt("userID");
            this.userName = userObject.getString("userName");
            this.salary = userObject.getInt("salary");
            this.apiKey = userObject.getString("apiKey");
            this.keyValidTo = userObject.getLong("keyValidTo");
            this.groups = new ArrayList<>();
            JSONArray groups = userObject.getJSONArray("groups");
            if (groups.length() > 0) {
                for (int g = 0; g < groups.length(); g++) {
                    this.groups.add(groups.getInt(g));
                }
            }
        } catch (Exception ex) {
            Log.e("User(String)", ex.getMessage());
        }
    }

    @Override
    public String toString() {
        JSONObject user = new JSONObject();
        try {
            user.put("userID", this.userID);
            user.put("userName", this.userName);
            user.put("salary", this.salary);
            user.put("groups", new JSONArray(this.groups));
            user.put("transactions", new JSONArray(this.transactions));
            user.put("apiKey", this.apiKey);
            user.put("keyValidTo", this.keyValidTo);
        } catch (Exception ex) {
            Log.e("User.toString()", ex.getMessage());
        }
        return user.toString();
    }

    public static User findUserByID(int userID, ArrayList<User> users) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).userID == userID) {
                return users.get(i);
            }
        }
        return null;
    }
}
