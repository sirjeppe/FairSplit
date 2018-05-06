package se.yawnmedia.fairsplit;

import android.util.Log;

import org.json.JSONObject;

public class PostTransactionObject {
    public String apiKey;
    public JSONObject transaction;

    public PostTransactionObject(String apiKey, Transaction transaction) {
        this.apiKey = apiKey;
        try {
            this.transaction = new JSONObject(transaction.toString());
        } catch (Exception ex) {
            Log.e("PostTransactionObject", ex.getMessage());
        }
    }
}
