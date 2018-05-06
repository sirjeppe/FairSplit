package se.yawnmedia.fairsplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private User currentUser;
    private Group currentGroup;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<Group> allGroups = new ArrayList<>();

//    private static final int RC_OCR_CAPTURE = 9003;
    private TransactionAdapter transactionAdapter;
    private ListView transactionListView;

    private void updateTransaction(Transaction newTransaction) {
        if (!currentUser.transactions.contains(newTransaction)) {
            currentUser.transactions.add(newTransaction);
        }
        if (transactionAdapter.getPosition(newTransaction) < 0) {
            transactionAdapter.add(newTransaction);
        }
        transactionAdapter.notifyDataSetChanged();
    }

    private void switchUser(String user) {
        for (int memberID : currentGroup.members) {
            User groupUser = User.findUserByID(memberID, allUsers);
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
                    transaction.datetime = (int) (System.currentTimeMillis() / 1000);
                    toPost = transaction;
                }

                try {
                    toPost.userID = currentUser.userID;
                    toPost.groupID = currentGroup.groupID;
                    new PostTransactionTask().execute(new PostTransactionObject(currentUser.apiKey, toPost));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(this, R.style.Logo);

        if (currentUser == null) {
            currentUser = new User(getIntent().getExtras().getString("user"));
        }

        if (currentGroup == null) {
            try {
                JSONArray intentGroups = new JSONArray(getIntent().getExtras().getString("groups"));
                if (intentGroups != null) {
                    for (int g = 0; g < intentGroups.length(); g++) {
                        JSONObject addGroup = intentGroups.getJSONObject(g);
                        allGroups.add(new Group(addGroup));
                    }
                }
            } catch (Exception ex) {
                Log.e("currentGroup == null", ex.getMessage());
            }
        }

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
                            transactionAdapter.remove(transaction);
                            transactionAdapter.notifyDataSetChanged();
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {}
                    });
                    alert.show();
                    return true;
                }
            });
        }

        FloatingActionButton fabText = findViewById(R.id.add_transaction_by_text);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showTransactionAlert(null);
            }
        });

        /*FloatingActionButton fabCam = findViewById(R.id.add_transaction_by_camera);
        fabCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch Ocr capture activity.
                Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, false);

                startActivityForResult(intent, RC_OCR_CAPTURE);
            }
        });*/

        /*FloatingActionButton fabMenu = findViewById(R.id.add_transaction_menu);
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabMenu) {
                View byCam = findViewById(R.id.add_transaction_by_camera);
                View byText = findViewById(R.id.add_transaction_by_text);
                long animationDuration = 150;
                if (byCam.getAlpha() == 1) {
                    fabMenu.animate().rotationBy(-135).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(animationDuration).start();
                    byCam.animate().alpha(0).translationYBy(175).setInterpolator(new DecelerateInterpolator()).setDuration(animationDuration).start();
                    byText.animate().alpha(0).translationYBy(350).setInterpolator(new DecelerateInterpolator()).setDuration(animationDuration).start();
                } else if (byCam.getAlpha() == 0) {
                    fabMenu.animate().rotationBy(135).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(animationDuration).start();
                    byCam.animate().alpha(1).translationYBy(-175).setInterpolator(new DecelerateInterpolator()).setDuration(animationDuration).start();
                    byText.animate().alpha(1).translationYBy(-350).setInterpolator(new DecelerateInterpolator()).setDuration(animationDuration).start();
                }

            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getSubMenu() != null || item.getItemId() == R.id.action_settings) {
            SubMenu subMenu;
            switch (item.getItemId()) {
                case R.id.action_settings:
                    Snackbar.make(findViewById(R.id.logo), R.string.settings, Snackbar.LENGTH_LONG).show();
                    return true;

                case R.id.action_select_group:
                    String[] groups = new String[] {
                            "Test group 1", "Test group 2"
                    };
                    subMenu = item.getSubMenu();
                    subMenu.clear();
                    for (String user : groups) {
                        subMenu.add(user);
                    }
                    return true;

                case R.id.action_select_user:
                    subMenu = item.getSubMenu();
                    subMenu.clear();
                    for (int m : currentGroup.members) {
                        User u = User.findUserByID(m, allUsers);
                        subMenu.add(u.userName);
                    }
                    return true;

                default:
                    return super.onOptionsItemSelected(item);

            }
        } else {
            String user = item.getTitle().toString();
            switchUser(user);
        }
        return true;
    }

    private class PostTransactionTask extends AsyncTask<PostTransactionObject, Void, Transaction> {
        private Exception exception;

        protected Transaction doInBackground(PostTransactionObject... transactionObject) {
            try {
                String apiKey = transactionObject[0].apiKey;
                JSONObject transaction = transactionObject[0].transaction;
                return new Transaction(RESTHelper.DoRequest("POST", RESTHelper.transactionEndpoint, transaction, apiKey));
            } catch (Exception ex) {
                this.exception = ex;
            }
            return null;
        }

        protected void onPostExecute(Transaction transaction) {
            if (transaction != null) {
                updateTransaction(transaction);
            } else {
                Snackbar.make(findViewById(R.id.logo), R.string.transaction_store_failed, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
