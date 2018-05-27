package se.yawnmedia.fairsplit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.Locale;

public class PopupExpense {
    private FairSplit app;
    private MainActivity mainActivity;
    public PostExpenseTask postExpenseTask;

    public PopupExpense(FairSplit app) {
        this.app = app;
        this.mainActivity = app.getMainActivityContext();
        postExpenseTask = new PostExpenseTask();
    }

    public void showExpensePopup(final Expense expense) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        if (expense != null) {
            alert.setTitle("Edit expense");
        } else {
            alert.setTitle("Add expense");
        }

        LayoutInflater inflater = mainActivity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_expense_popup, null);
        final EditText popupAmount = dialogView.findViewById(R.id.popupAmount);
        final EditText popupTitle = dialogView.findViewById(R.id.popupTitle);
        final EditText popupComment = dialogView.findViewById(R.id.popupComment);

        if (expense != null) {
            popupAmount.setText(String.format(Locale.US, "%.2f", expense.amount));
            popupAmount.setSelection(popupAmount.getText().length());
            popupTitle.setText(expense.title);
            popupComment.setText(expense.comment);
        }

        alert.setView(dialogView);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Expense toPost;

                if (expense == null) {
                    Expense newExpense = new Expense();
                    newExpense.title = popupTitle.getText().toString();
                    newExpense.amount = Double.parseDouble(popupAmount.getText().toString());
                    newExpense.comment = popupComment.getText().toString();
                    newExpense.datetime = (int) (System.currentTimeMillis() / 1000);
                    toPost = newExpense;
                } else {
                    expense.title = popupTitle.getText().toString();
                    expense.amount = Double.parseDouble(popupAmount.getText().toString());
                    expense.comment = popupComment.getText().toString();
                    toPost = expense;
                }

                try {
                    toPost.userID = app.getCurrentUser().userID;
                    toPost.groupID = app.getCurrentGroup().groupID;
                    new PostExpenseTask().execute(toPost);
                } catch (Exception ex) {
                    Log.e("updateExpense", ex.getMessage());
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for CANCEL button here, or leave in blank
            }
        });
        final AlertDialog alertDialog = alert.create();

        // Make sure OK button gets enabled when popupAmount and popupTitle aren't empty
        popupAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (
                    popupAmount.getText().toString().isEmpty()
                    || popupTitle.getText().toString().isEmpty()
                ) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        popupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (
                    popupAmount.getText().toString().isEmpty()
                    || popupTitle.getText().toString().isEmpty()
                ) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();

        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (okButton != null) {
            okButton.setEnabled(false);
        }
    }

    public class PostExpenseTask extends AsyncTask<Expense, Void, Expense> {
        protected Expense doInBackground(Expense... expense) {
            try {
                JSONObject expenseJSON = new JSONObject(expense[0].toString());
                if (expense[0].expenseID == 0) {
                    JSONObject expenseResponse = RESTHelper.POST(RESTHelper.expenseEndpoint, expenseJSON, app.getCurrentUser().apiKey, mainActivity);
                    JSONObject newExpense = expenseResponse.getJSONArray("data").getJSONObject(0);
                    return new Expense(newExpense);
                } else if (expense[0].deleteMe) {
                    JSONObject expenseResponse = RESTHelper.DELETE(RESTHelper.expenseEndpoint + "/" + expense[0].expenseID, expenseJSON, app.getCurrentUser().apiKey, mainActivity);
                    if (expenseResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return expense[0];
                } else {
                    JSONObject expenseResponse = RESTHelper.PUT(RESTHelper.expenseEndpoint + "/" + expense[0].expenseID, expenseJSON, app.getCurrentUser().apiKey, mainActivity);
                    if (expenseResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return expense[0];
                }
            } catch (Exception ex) {
                Log.e("PostExpenseTask", ex.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Expense expense) {
            if (expense != null) {
                updateExpense(expense);
            } else {
                Snackbar.make(mainActivity.findViewById(R.id.logo), R.string.expense_modification_failed, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void updateExpense(Expense newExpense) {
        // Safety net
        if (app.getSelectedUser() == app.getCurrentUser()) {
            if (!app.getCurrentUser().expenses.contains(newExpense)) {
                app.getCurrentUser().expenses.add(newExpense);
            }
            if (mainActivity.expenseAdapter.getPosition(newExpense) < 0) {
                mainActivity.expenseAdapter.insert(newExpense, 0);
            }
            if (newExpense.deleteMe) {
                mainActivity.expenseAdapter.remove(newExpense);
                app.getCurrentUser().expenses.remove(newExpense);
            }
            mainActivity.updateSelectedUser();
            mainActivity.expenseAdapter.notifyDataSetChanged();
        }
    }
}
