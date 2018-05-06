package se.yawnmedia.fairsplit;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Jeppe on 2018-03-26.
 */

public class Transaction {
    public long transactionID;
    public long groupID;
    public long userID;
    public double amount;
    public String title;
    public String comment;
    public long datetime;

    public Transaction() {}

    public Transaction(JSONObject transaction) {
        try {
            this.transactionID = transaction.getLong("transactionID");
            this.groupID = transaction.getLong("groupID");
            this.userID = transaction.getLong("userID");
            this.amount = transaction.getDouble("amount");
            this.title = transaction.getString("title");
            this.comment = transaction.getString("comment");
            this.datetime = transaction.getLong("datetime");
        } catch (Exception ex) {
            Log.e("Transaction", ex.getMessage());
        }
    }

    @Override
    public String toString() {
        JSONObject user = new JSONObject();
        try {
            user.put("transactionID", this.transactionID);
            user.put("groupID", this.groupID);
            user.put("userID", this.userID);
            user.put("amount", this.amount);
            user.put("title", this.title);
            user.put("comment", this.comment);
            user.put("datetime", this.datetime);
        } catch (Exception ex) {
            Log.e("Transaction.toString()", ex.getMessage());
        }
        return user.toString();
    }
}
