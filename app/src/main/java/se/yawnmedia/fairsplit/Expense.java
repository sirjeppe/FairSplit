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

    public Expense(JSONObject transaction) {
        try {
            this.expenseID = transaction.getLong("expenseID");
            this.groupID = transaction.getLong("groupID");
            this.userID = transaction.getLong("userID");
            this.amount = transaction.getDouble("amount");
            this.title = transaction.getString("title");
            this.comment = transaction.getString("comment");
            this.datetime = transaction.getLong("datetime");
        } catch (Exception ex) {
            Log.e("Expense(JSONObject)", ex.getMessage());
        }
    }

    @Override
    public String toString() {
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
            Log.e("Expense.toString()", ex.getMessage());
        }
        return user.toString();
    }
}
