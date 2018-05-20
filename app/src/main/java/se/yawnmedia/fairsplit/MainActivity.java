package se.yawnmedia.fairsplit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private FairSplit app;
    private ExpenseAdapter expenseAdapter;
    private TextView expensesTitle;
    private TextView groupsTitle;
    private TextView usersTitle;
    private TextView settingsTitle;
    private FloatingActionButton actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar ab = this.getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayUseLogoEnabled(false);
        ab.setHomeButtonEnabled(false);
        View customView = getLayoutInflater().inflate(R.layout.action_bar, null);
        ab.setCustomView(customView);
        android.support.v7.widget.Toolbar parent = (android.support.v7.widget.Toolbar) customView.getParent();
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0,0);

        // Find some stuff
        actionButton = findViewById(R.id.add_action_button);
        expensesTitle = findViewById(R.id.expenses_title);
        groupsTitle = findViewById(R.id.groups_title);
        usersTitle = findViewById(R.id.users_title);
        settingsTitle = findViewById(R.id.settings_title);
        app = ((FairSplit) this.getApplication());
        app.setupAppPrefs(this);

        // Default to show logged in user after logging in
        if (app.getSelectedUser() == null) {
            if (app.getCurrentUser() == null) {
                try {
                    new TryLoginTask(app, this).execute().get();
                    if (app.getSelectedUser() != null) {
                        initiateViewPager();
                    }
                } catch (Exception ex) {
                    Log.e("TryLoginTask", ex.getMessage());
                }
            } else {
                app.setSelectedUser(app.getCurrentUser());
                initiateViewPager();
            }
        } else {
            initiateViewPager();
        }
    }

    private void initiateViewPager() {
        ViewPager viewPager = findViewById(R.id.vp);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        expensesTitle.setAlpha(1 - positionOffset);
                        groupsTitle.setAlpha(positionOffset);
                        usersTitle.setAlpha(0);
                        break;

                    case 1:
                        groupsTitle.setAlpha(1 - positionOffset);
                        usersTitle.setAlpha(positionOffset);
                        settingsTitle.setAlpha(0);
                        break;

                    case 2:
                        usersTitle.setAlpha(1 - positionOffset);
                        settingsTitle.setAlpha(positionOffset);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        actionButton.setOnClickListener(addTransactionListener);
                        actionButton.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        actionButton.setOnClickListener(createGroupListener);
                        actionButton.setVisibility(View.VISIBLE);
                        break;

                    case 2:
                        actionButton.setOnClickListener(inviteUserListener);
                        actionButton.setVisibility(View.VISIBLE);
                        break;

                    case 3:
                        actionButton.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(0);
    }

    public enum ModelObject {

        TRANSACTIONS(R.string.tabname_expenses, R.layout.content_expenses),
        GROUPS(R.string.tabname_groups, R.layout.content_group_manager),
        USERS(R.string.tabname_users, R.layout.content_user_selector),
        SETTINGS(R.string.tabname_settings, R.layout.content_settings);

        private int mTitleResId;
        private int mLayoutResId;

        ModelObject(int titleResId, int layoutResId) {
            mTitleResId = titleResId;
            mLayoutResId = layoutResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public int getLayoutResId() {
            return mLayoutResId;
        }

    }

    public class CustomPagerAdapter extends PagerAdapter {

        private Context mContext;

        public CustomPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            ModelObject modelObject = ModelObject.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(modelObject.getLayoutResId(), collection, false);
            collection.addView(layout);

            // Expenses part
            if (position == 0) {
                expenseAdapter = new ExpenseAdapter(MainActivity.this, R.layout.expense_item);
                ListView transactionListView = findViewById(R.id.expenses_list_view);
                transactionListView.setAdapter(expenseAdapter);
                transactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView transactionTitle = view.findViewById(R.id.expenseTitle);
                        Expense expense = (Expense) transactionTitle.getTag();
                        showTransactionAlert(expense);
                    }
                });
                transactionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView transactionTitle = view.findViewById(R.id.expenseTitle);
                        final Expense expense = (Expense) transactionTitle.getTag();

                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Remove " + expense.title + "?");
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                expense.deleteMe = true;
                                new PostTransactionTask().execute(expense);
                            }
                        });
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                        alert.show();
                        return true;
                    }
                });
                for (Expense expense : app.getSelectedUser().expenses) {
                    expenseAdapter.add(expense);
                }
                expenseAdapter.notifyDataSetChanged();
            }
            // End expenses part

            // Groups part
            else if (position == 1) {
                GroupsAdapter groupsAdapter = new GroupsAdapter(MainActivity.this, R.layout.group_item);
                ListView groupsListView = findViewById(R.id.groups_list_view);
                groupsListView.setAdapter(groupsAdapter);
                groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RadioButton groupRadioButton = view.findViewById(R.id.groupRadioButton);
                        Group group = (Group) groupRadioButton.getTag();
                        app.setCurrentGroup(group);
                        ViewPager vp = findViewById(R.id.vp);
                        vp.setCurrentItem(0);
                        actionButton.setOnClickListener(addTransactionListener);
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
            else if (position == 2) {
                UsersAdapter usersAdapter = new UsersAdapter(MainActivity.this, R.layout.user_item);
                ListView usersListView = findViewById(R.id.users_list_view);
                usersListView.setAdapter(usersAdapter);
                usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RadioButton userRadioButton = view.findViewById(R.id.userRadioButton);
                        User user = (User) userRadioButton.getTag();
                        app.setSelectedUser(user);
                        ViewPager vp = findViewById(R.id.vp);
                        vp.setCurrentItem(0);
                        actionButton.setOnClickListener(addTransactionListener);
                    }
                });
                for (int userID : app.getCurrentGroup().members) {
                    usersAdapter.add(app.getUserByID(userID));
                }
                usersAdapter.notifyDataSetChanged();
            }
            // End users part

            // Settings part
            else if (position == 3) {
                EditText salary = findViewById(R.id.salary_input);
                salary.setText("" + app.getCurrentUser().salary);
            }
            // End settings part

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return ModelObject.values().length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            ModelObject customPagerEnum = ModelObject.values()[position];
            return mContext.getString(customPagerEnum.getTitleResId());
        }

    }

    private void updateTransaction(Expense newExpense) {
        if (!app.getCurrentUser().expenses.contains(newExpense)) {
            app.getCurrentUser().expenses.add(newExpense);
        }
        if (expenseAdapter.getPosition(newExpense) < 0) {
            expenseAdapter.add(newExpense);
        }
        if (newExpense.deleteMe) {
            expenseAdapter.remove(newExpense);
        }
        expenseAdapter.notifyDataSetChanged();
    }

    private void switchUser(String user) {
        for (int memberID : app.getCurrentGroup().members) {
            User groupUser = User.findUserByID(memberID, app.getAllUsers());
            if (groupUser != null && groupUser.userName.equals(user)) {
                expenseAdapter.clear();
                for (Expense expense : groupUser.expenses) {
                    expenseAdapter.add(expense);
                }
                expenseAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    private void showTransactionAlert(final Expense expense) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        if (expense != null) {
            alert.setTitle("Edit expense");
        } else {
            alert.setTitle("Add expense");
        }
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_expense_popup, null);
        if (expense != null) {
            EditText popupAmount = dialogView.findViewById(R.id.popupAmount);
            popupAmount.setText("" + expense.amount);
            popupAmount.setSelection(popupAmount.getText().length());
            ((EditText) dialogView.findViewById(R.id.popupTitle)).setText(expense.title);
            ((EditText) dialogView.findViewById(R.id.popupComment)).setText(expense.comment);
        }
        alert.setView(dialogView);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText amount = dialogView.findViewById(R.id.popupAmount);
                EditText title = dialogView.findViewById(R.id.popupTitle);
                EditText comment = dialogView.findViewById(R.id.popupComment);

                Expense toPost;
                if (expense == null) {
                    Expense newExpense = new Expense();
                    newExpense.title = title.getText().toString();
                    newExpense.amount = Double.parseDouble(amount.getText().toString());
                    newExpense.comment = comment.getText().toString();
                    newExpense.datetime = (int) (System.currentTimeMillis() / 1000);
                    toPost = newExpense;
                } else {
                    expense.title = title.getText().toString();
                    expense.amount = Double.parseDouble(amount.getText().toString());
                    expense.comment = comment.getText().toString();
                    toPost = expense;
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

    private class PostTransactionTask extends AsyncTask<Expense, Void, Expense> {
        protected Expense doInBackground(Expense... expense) {
            try {
                JSONObject transactionJSON = new JSONObject(expense[0].toString());
                if (expense[0].transactionID == 0) {
                    JSONObject transactionResponse = RESTHelper.POST(RESTHelper.transactionEndpoint, transactionJSON, app.getCurrentUser().apiKey, MainActivity.this);
                    JSONObject newTransaction = transactionResponse.getJSONArray("data").getJSONObject(0);
                    return new Expense(newTransaction);
                } else if (expense[0].deleteMe == true) {
                    JSONObject transactionResponse = RESTHelper.DELETE(RESTHelper.transactionEndpoint + "/" + expense[0].transactionID, transactionJSON, app.getCurrentUser().apiKey, MainActivity.this);
                    if (transactionResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return expense[0];
                } else {
                    JSONObject transactionResponse = RESTHelper.PUT(RESTHelper.transactionEndpoint + "/" + expense[0].transactionID, transactionJSON, app.getCurrentUser().apiKey, MainActivity.this);
                    if (transactionResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return expense[0];
                }
            } catch (Exception ex) {
                Log.e("PostTransactionTask", ex.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Expense expense) {
            if (expense != null) {
                updateTransaction(expense);
            } else {
                Snackbar.make(findViewById(R.id.logo), R.string.transaction_modification_failed, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // Action button listeners
    private View.OnClickListener addTransactionListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            showTransactionAlert(null);
        }
    };

    private View.OnClickListener createGroupListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            //showCreateGroupAlert(null);
        }
    };

    private View.OnClickListener inviteUserListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            //showInviteUserAlert(null);
        }
    };
}
