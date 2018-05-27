package se.yawnmedia.fairsplit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

public class PopupInviteUser {
    private FairSplit app;
    private MainActivity mainActivity;

    public PopupInviteUser(FairSplit app) {
        this.app = app;
        this.mainActivity = app.getMainActivityContext();
    }

    public void showInviteUserPopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.popup_invite_user, null);

        alert.setView(dialogView);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText userNameEdit = dialogView.findViewById(R.id.income);
                try {
                    new PostInviteUserTask().execute(userNameEdit.getText().toString());
                } catch (Exception ex) {
                    Log.e("updateIncome", ex.getMessage());
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            //Put actions for CANCEL button here, or leave in blank
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
    }

    private class PostInviteUserTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... userName) {
            try {
                UserInvitation ui = new UserInvitation(
                    app.getCurrentGroup().groupID,
                    userName[0],
                    app.getCurrentUser().userName
                );
                JSONObject userResponse = RESTHelper.POST(
                    RESTHelper.userEndpoint + "/" + app.getCurrentUser().userID,
                    ui.toJSONObject(),
                    app.getCurrentUser().apiKey,
                        mainActivity
                );
                if (userResponse.getInt("errorCode") > 0) {
                    return false;
                }
                return true;
            } catch (Exception ex) {
                Log.e("PostUserTask", ex.getMessage());
            }
            return false;
        }

        protected void onPostExecute(Boolean success) {
            TextView incomeTextView = mainActivity.findViewById(R.id.setting_income_amount);
            if (success) {
                incomeTextView.setText(String.format(Locale.US, "%d", app.getCurrentUser().income));
                Snackbar.make(incomeTextView, R.string.settings_saved, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(incomeTextView, R.string.settings_save_failed, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
