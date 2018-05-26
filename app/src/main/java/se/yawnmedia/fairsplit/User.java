package se.yawnmedia.fairsplit;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class User {
    public int userID;
    public String userName;
    public int income;
    public ArrayList<Integer> groups = new ArrayList<>();
    public ArrayList<Expense> expenses = new ArrayList<>();
    public String apiKey;
    public long keyValidTo;

    public User(JSONObject user, Context context) {
        try {
            this.userID = user.getInt("userID");
            this.userName = user.getString("userName");
            this.income = user.getInt("income");
            this.apiKey = user.getString("apiKey");
            this.keyValidTo = user.getLong("keyValidTo");
            JSONArray groups = user.getJSONArray("groups");
            if (groups.length() > 0) {
                for (int g = 0; g < groups.length(); g++) {
                    this.groups.add(groups.getInt(g));
                }
            }
        } catch (Exception ex) {
            Log.e("User(JSONObject)", ex.getMessage());
        }
        this.fetchTransactionsForUser(context);
    }

    @Override
    public String toString() {
        JSONObject user = new JSONObject();
        try {
            user.put("userID", this.userID);
            user.put("userName", this.userName);
            user.put("income", this.income);
            user.put("groups", new JSONArray(this.groups));
            user.put("apiKey", this.apiKey);
            user.put("keyValidTo", this.keyValidTo);
        } catch (Exception ex) {
            Log.e("User.toString()", ex.getMessage());
        }
        return user.toString();
    }

    public JSONObject toJSONObject() {
        try {
            return new JSONObject(this.toString());
        } catch (Exception ex) {
            Log.e("User.toJSONObject()", ex.getMessage());
        }
        return null;
    }

    public static User findUserByID(int userID, ArrayList<User> users) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).userID == userID) {
                return users.get(i);
            }
        }
        return null;
    }

    public double sumExpenses() {
        double sum = 0;
        for (int i = 0; i < expenses.size(); i++) {
            sum += expenses.get(i).amount;
        }
        return sum;
    }

    private void fetchTransactionsForUser(Context context) {
        try {
            JSONObject response = RESTHelper.GET("/expense/byUserID/" + this.userID, this.apiKey, context);
            JSONArray t = response.getJSONArray("data");
            if (t.length() > 0) {
                for (int i = 0; i < t.length(); i++) {
                    this.expenses.add(new Expense(t.getJSONObject(i)));
                }
            }
        } catch (Exception ex) {
            Log.e("User.fetchTransactionsForUser", ex.getMessage());
        }
    }
}
