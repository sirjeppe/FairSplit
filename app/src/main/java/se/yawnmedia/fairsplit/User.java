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
            this.groups = new ArrayList<Integer>();
            JSONArray groups = user.getJSONArray("groups");
            if (groups.length() > 0) {
                for (int g = 0; g < groups.length(); g++) {
                    this.groups.add(groups.getInt(g));
                }
            }
        } catch (Exception ex) {
            Log.e("tag", ex.getMessage());
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
            this.groups = new ArrayList<Integer>();
            JSONArray groups = userObject.getJSONArray("groups");
            if (groups.length() > 0) {
                for (int g = 0; g < groups.length(); g++) {
                    this.groups.add(groups.getInt(g));
                }
            }
        } catch (Exception ex) {
            Log.e("tag", ex.getMessage());
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
            Log.e("tag", ex.getMessage());
        }
        return user.toString();
    }
}
