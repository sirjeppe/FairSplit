package se.yawnmedia.fairsplit;

import android.app.AlertDialog;
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

public class PopupIncome {
    private FairSplit app;
    private MainActivity mainActivity;

    public PopupIncome(FairSplit app) {
        this.app = app;
        this.mainActivity = app.getMainActivityContext();
    }

    public void showIncomePopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.setting_income_popup, null);

        EditText incomeEdit = dialogView.findViewById(R.id.income);
        incomeEdit.setText(String.format(Locale.US, "%d", app.getCurrentUser().income));
        incomeEdit.setSelection(0, incomeEdit.getText().length());

        alert.setView(dialogView);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText incomeEdit = dialogView.findViewById(R.id.income);
                int income = Integer.parseInt(incomeEdit.getText().toString());
                app.getCurrentUser().income = income;
                try {
                    new PostUserTask().execute(app.getCurrentUser());
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

    public class PostUserTask extends AsyncTask<User, Void, Boolean> {
        protected Boolean doInBackground(User... user) {
            try {
                User toPost = user[0];
                JSONObject userResponse = RESTHelper.PUT(
                        RESTHelper.userEndpoint + "/" + app.getCurrentUser().userID,
                        toPost.toJSONObject(),
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
