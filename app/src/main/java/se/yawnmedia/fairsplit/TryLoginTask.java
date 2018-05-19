package se.yawnmedia.fairsplit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class TryLoginTask extends AsyncTask<Void, Void, Void> {

    private FairSplit app;
    private Context context;

    public TryLoginTask(FairSplit app, Context context) {
        this.app = app;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String errorMessage = null;
        try {
            errorMessage = RESTHelper.loginUserByAPIKey(app, context);
        } catch (Exception ex) {
            Log.e("loginUserByAPIKey", ex.getMessage());
        }
        if (errorMessage == null) {
            app.setSelectedUser(app.getCurrentUser());
        } else {
            if (!context.getClass().equals(LoginActivity.class)) {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.getApplicationContext().startActivity(intent);
            }
        }
        return null;
    }
}
