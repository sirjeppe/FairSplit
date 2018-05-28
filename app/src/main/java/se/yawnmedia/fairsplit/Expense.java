package se.yawnmedia.fairsplit;

import android.util.Log;

import org.json.JSONObject;

public class Expense {
    public long expenseID;
    public long groupID;
    public long userID;
    public double amount;
    public String title;
    public String comment;
    public long datetime;
    public boolean deleteMe = false;

    public Expense() {}

    public Expense(JSONObject expense) {
        try {
            this.expenseID = expense.getLong("expenseID");
            this.groupID = expense.getLong("groupID");
            this.userID = expense.getLong("userID");
            this.amount = expense.getDouble("amount");
            this.title = expense.getString("title");
            this.comment = expense.getString("comment");
            this.datetime = expense.getLong("datetime");
        } catch (Exception ex) {
            Log.e("Expense(JSONObject)", ex.getMessage());
        }
    }

    @Override
    public String toString() {
        JSONObject user = this.toJSONObject();
        return user.toString();
    }

    public JSONObject toJSONObject() {
        JSONObject user = new JSONObject();
        try {
            user.put("expenseID", this.expenseID);
            user.put("groupID", this.groupID);
            user.put("userID", this.userID);
            user.put("amount", this.amount);
            user.put("title", this.title);
            user.put("comment", this.comment);
            user.put("datetime", this.datetime);
        } catch (Exception ex) {
            Log.e("Expense.toJSONObject()", ex.getMessage());
        }
        return user;
    }
}
