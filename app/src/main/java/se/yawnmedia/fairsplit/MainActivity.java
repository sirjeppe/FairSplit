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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FairSplit app;
    public ExpenseAdapter expenseAdapter;
    public GroupsAdapter groupsAdapter;
    private TextView expensesTitle;
    private TextView groupsTitle;
    private TextView usersTitle;
    private TextView settingsTitle;
    private FloatingActionButton actionButton;
    private ViewPager viewPager;

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
        app.setMainActivityContext(this);
        viewPager = findViewById(R.id.vp);

        // Make top menu clickable
        // Expenses
        LinearLayout topExpensesGroup = findViewById(R.id.top_expenses_group);
        topExpensesGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        // Groups
        LinearLayout topGroupsGroup = findViewById(R.id.top_groups_group);
        topGroupsGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        // Users
        LinearLayout topUsersGroup = findViewById(R.id.top_users_group);
        topUsersGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
        // Settings
        LinearLayout topSettingsGroup = findViewById(R.id.top_settings_group);
        topSettingsGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(3);
            }
        });

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
        viewPager.setAdapter(new CustomPagerAdapter(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        expensesTitle.setAlpha(1 - positionOffset);
                        groupsTitle.setAlpha(positionOffset);
                        usersTitle.setAlpha(0);
                        settingsTitle.setAlpha(0);
                        break;

                    case 1:
                        groupsTitle.setAlpha(1 - positionOffset);
                        usersTitle.setAlpha(positionOffset);
                        settingsTitle.setAlpha(0);
                        break;

                    case 2:
                        usersTitle.setAlpha(1 - positionOffset);
                        settingsTitle.setAlpha(positionOffset);
                        groupsTitle.setAlpha(0);
                        expensesTitle.setAlpha(0);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        actionButton.setOnClickListener(addExpenseListener);
                        if (app.getSelectedUser() == app.getCurrentUser()) {
                            actionButton.setVisibility(View.VISIBLE);
                        } else {
                            actionButton.setVisibility(View.GONE);
                        }
                        break;

                    case 1:
                        actionButton.setOnClickListener(createGroupListener);
                        actionButton.setVisibility(View.VISIBLE);
                        break;

                    case 2:
                        actionButton.setOnClickListener(inviteUserListener);
                        if (app.getCurrentGroup().owner == app.getCurrentUser().userID) {
                            actionButton.setVisibility(View.VISIBLE);
                        } else {
                            actionButton.setVisibility(View.GONE);
                        }
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
        actionButton.setOnClickListener(addExpenseListener);
        actionButton.setVisibility(View.VISIBLE);
    }

    public enum ModelObject {

        EXPENSES(R.string.tabname_expenses, R.layout.content_expenses),
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
                // Show who's selected and total expenses
                updateSelectedUser();

                final PopupExpense pe = new PopupExpense(app);

                expenseAdapter = new ExpenseAdapter(MainActivity.this, R.layout.expense_item);
                ListView expenseListView = findViewById(R.id.expenses_list_view);
                expenseListView.setAdapter(expenseAdapter);
                expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView expenseTitle = view.findViewById(R.id.expense_title);
                        Expense expense = (Expense) expenseTitle.getTag();
                        pe.showExpensePopup(expense);
                    }
                });
                expenseListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView expenseTitle = view.findViewById(R.id.expense_title);
                        Expense expense = (Expense) expenseTitle.getTag();
                        pe.showExpenseDeletePopup(expense);
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
                groupsAdapter = new GroupsAdapter(MainActivity.this, R.layout.group_item);
                ListView groupsListView = findViewById(R.id.groups_list_view);
                groupsListView.setAdapter(groupsAdapter);
                groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RadioButton groupRadioButton = view.findViewById(R.id.groupRadioButton);
                    Group group = (Group) groupRadioButton.getTag();
                    app.setCurrentGroup(group);
                    viewPager.setCurrentItem(0);
                    actionButton.setOnClickListener(addExpenseListener);
                    }
                });
                groupsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        RadioButton groupRadioButton = view.findViewById(R.id.groupRadioButton);
                        final Group group = (Group) groupRadioButton.getTag();

                        // Only allow modifications by group owner
                        if (group.owner != app.getCurrentUser().userID) {
                            return false;
                        }

                        PopupGroup pg = new PopupGroup(app);
                        pg.showGroupPopup(group);
                        return true;
                    }
                });
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
                    switchUser(user);
                    viewPager.setCurrentItem(0);
                    actionButton.setOnClickListener(addExpenseListener);
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
                TextView income = findViewById(R.id.setting_income_amount);
                income.setText(String.format(Locale.US, "%d", app.getCurrentUser().income));
                RelativeLayout settingIncome = findViewById(R.id.setting_income);
                settingIncome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    PopupIncome pi = new PopupIncome(app);
                    pi.showIncomePopup();
                    }
                });
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

    public void updateSelectedUser() {
        TextView selectedUserName = findViewById(R.id.selected_user_name);
        TextView selectedUserExpensesTotal = findViewById(R.id.selected_user_expenses_total);
        selectedUserName.setText(app.getSelectedUser().userName);
        selectedUserExpensesTotal.setText(String.format("%.2f", app.getSelectedUser().sumExpenses()));
    }

    private void switchUser(User user) {
        expenseAdapter.clear();
        for (Expense expense : user.expenses) {
            expenseAdapter.add(expense);
        }
        expenseAdapter.notifyDataSetChanged();
        app.setSelectedUser(user);
    }

    // Action button listeners
    private View.OnClickListener addExpenseListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            PopupExpense pe = new PopupExpense(app);
            pe.showExpensePopup(null);
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
            PopupInviteUser piu = new PopupInviteUser(app);
            piu.showInviteUserPopup();
        }
    };
}
