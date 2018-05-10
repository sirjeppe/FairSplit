package se.yawnmedia.fairsplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private FairSplit app;
//    private static final int RC_OCR_CAPTURE = 9003;
    private TransactionAdapter transactionAdapter;
    private GroupsAdapter groupsAdapter;
    private UsersAdapter usersAdapter;
    private ListView transactionListView;
    private ListView groupsListView;
    private ListView usersListView;
    private FloatingActionButton actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(this, R.style.Logo);

        // Find some stuff
        actionButton = findViewById(R.id.add_action_button);
        app = ((FairSplit) this.getApplication());

        // Default to show logged in user after logging in
        if (app.getSelectedUser() == null) {
            app.setSelectedUser(app.getCurrentUser());
        }

        // Transactions part
        if (transactionAdapter == null) {
            transactionAdapter = new TransactionAdapter(MainActivity.this, R.layout.transaction_item);
            transactionListView = findViewById(R.id.transaction_list_view);
            transactionListView.setAdapter(transactionAdapter);
            transactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView transactionTitle = view.findViewById(R.id.transactionTitle);
                    Transaction transaction = (Transaction) transactionTitle.getTag();
                    showTransactionAlert(transaction);
                }
            });
            transactionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView transactionTitle = view.findViewById(R.id.transactionTitle);
                    final Transaction transaction = (Transaction) transactionTitle.getTag();

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("Remove " + transaction.title + "?");
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            transaction.deleteMe = true;
                            new PostTransactionTask().execute(transaction);
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {}
                    });
                    alert.show();
                    return true;
                }
            });
            for (Transaction transaction : app.getSelectedUser().transactions) {
                transactionAdapter.add(transaction);
            }
            transactionAdapter.notifyDataSetChanged();
        }
        // End transactions part

        // Groups part
        if (groupsAdapter == null) {
            groupsAdapter = new GroupsAdapter(MainActivity.this, R.layout.group_item);
            groupsListView = findViewById(R.id.groups_list_view);
            groupsListView.setAdapter(groupsAdapter);
            groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RadioButton groupRadioButton = view.findViewById(R.id.groupRadioButton);
                    Group group = (Group) groupRadioButton.getTag();
                    app.setCurrentGroup(group);
                    ViewFlipper vf = findViewById(R.id.vf);
                    vf.setDisplayedChild(0);
                }
            });
//            groupsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                    TextView groupName = view.findViewById(R.id.groupName);
//                    final Group group = (Group) groupName.getTag();
//
//                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//                    alert.setTitle("Remove " + group.groupName + "?");
//                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            group.deleteMe = true;
//                            new PostGroupTask().execute(group);
//                        }
//                    });
//                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {}
//                    });
//                    alert.show();
//                    return true;
//                }
//            });
            for (int groupID : app.getCurrentUser().groups) {
                groupsAdapter.add(app.getGroupByID(groupID));
            }
            groupsAdapter.notifyDataSetChanged();
        }
        // End groups part

        // Users part
        if (usersAdapter == null) {
            usersAdapter = new UsersAdapter(MainActivity.this, R.layout.user_item);
            usersListView = findViewById(R.id.users_list_view);
            usersListView.setAdapter(usersAdapter);
            usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RadioButton userRadioButton = view.findViewById(R.id.userRadioButton);
                    User user = (User) userRadioButton.getTag();
                    app.setSelectedUser(user);
                    ViewFlipper vf = findViewById(R.id.vf);
                    vf.setDisplayedChild(0);
                    actionButton.setVisibility(View.VISIBLE);
                }
            });
            for (int userID : app.getCurrentGroup().members) {
                usersAdapter.add(app.getUserByID(userID));
            }
            usersAdapter.notifyDataSetChanged();
        }
        // End users part

        FloatingActionButton fabText = findViewById(R.id.add_action_button);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showTransactionAlert(null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ViewFlipper vf = findViewById(R.id.vf);
        switch (item.getItemId()) {
            case R.id.action_settings:
                Snackbar.make(findViewById(R.id.logo), R.string.settings, Snackbar.LENGTH_LONG).show();
                break;

            case R.id.action_select_group:
                vf.setDisplayedChild(1);
                break;

            case R.id.action_select_user:
                actionButton.setVisibility(View.GONE);
                vf.setDisplayedChild(2);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    private void updateTransaction(Transaction newTransaction) {
        if (!app.getCurrentUser().transactions.contains(newTransaction)) {
            app.getCurrentUser().transactions.add(newTransaction);
        }
        if (transactionAdapter.getPosition(newTransaction) < 0) {
            transactionAdapter.add(newTransaction);
        }
        if (newTransaction.deleteMe) {
            transactionAdapter.remove(newTransaction);
        }
        transactionAdapter.notifyDataSetChanged();
    }

    private void switchUser(String user) {
        for (int memberID : app.getCurrentGroup().members) {
            User groupUser = User.findUserByID(memberID, app.getAllUsers());
            if (groupUser != null && groupUser.userName.equals(user)) {
                transactionAdapter.clear();
                for (Transaction transaction : groupUser.transactions) {
                    transactionAdapter.add(transaction);
                }
                transactionAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    private void showTransactionAlert(final Transaction transaction) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        if (transaction != null) {
            alert.setTitle("Edit expense");
        } else {
            alert.setTitle("Add expense");
        }
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_transaction_popup, null);
        if (transaction != null) {
            EditText popupAmount = dialogView.findViewById(R.id.popupAmount);
            popupAmount.setText("" + transaction.amount);
            popupAmount.setSelection(popupAmount.getText().length());
            ((EditText) dialogView.findViewById(R.id.popupTitle)).setText(transaction.title);
            ((EditText) dialogView.findViewById(R.id.popupComment)).setText(transaction.comment);
        }
        alert.setView(dialogView);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText amount = dialogView.findViewById(R.id.popupAmount);
                EditText title = dialogView.findViewById(R.id.popupTitle);
                EditText comment = dialogView.findViewById(R.id.popupComment);

                Transaction toPost;
                if (transaction == null) {
                    Transaction newTransaction = new Transaction();
                    newTransaction.title = title.getText().toString();
                    newTransaction.amount = Double.parseDouble(amount.getText().toString());
                    newTransaction.comment = comment.getText().toString();
                    newTransaction.datetime = (int) (System.currentTimeMillis() / 1000);
                    toPost = newTransaction;
                } else {
                    transaction.title = title.getText().toString();
                    transaction.amount = Double.parseDouble(amount.getText().toString());
                    transaction.comment = comment.getText().toString();
                    toPost = transaction;
                }

                try {
                    toPost.userID = app.getCurrentUser().userID;
                    toPost.groupID = app.getCurrentGroup().groupID;
                    new PostTransactionTask().execute(toPost);
                } catch (Exception ex) {
                    Log.e("updateTransaction", ex.getMessage());
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

    private class PostTransactionTask extends AsyncTask<Transaction, Void, Transaction> {
        protected Transaction doInBackground(Transaction... transaction) {
            try {
                JSONObject transactionJSON = new JSONObject(transaction[0].toString());
                if (transaction[0].transactionID == 0) {
                    JSONObject transactionResponse = RESTHelper.POST(RESTHelper.transactionEndpoint, transactionJSON, app.getCurrentUser().apiKey);
                    JSONObject newTransaction = transactionResponse.getJSONArray("data").getJSONObject(0);
                    return new Transaction(newTransaction);
                } else if (transaction[0].deleteMe == true) {
                    JSONObject transactionResponse = RESTHelper.DELETE(RESTHelper.transactionEndpoint + "/" + transaction[0].transactionID, transactionJSON, app.getCurrentUser().apiKey);
                    if (transactionResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return transaction[0];
                } else {
                    JSONObject transactionResponse = RESTHelper.PUT(RESTHelper.transactionEndpoint + "/" + transaction[0].transactionID, transactionJSON, app.getCurrentUser().apiKey);
                    if (transactionResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return transaction[0];
                }
            } catch (Exception ex) {
                Log.e("PostTransactionTask", ex.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Transaction transaction) {
            if (transaction != null) {
                updateTransaction(transaction);
            } else {
                Snackbar.make(findViewById(R.id.logo), R.string.transaction_modification_failed, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
