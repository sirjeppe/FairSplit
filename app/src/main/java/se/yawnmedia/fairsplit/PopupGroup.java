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
import android.widget.EditText;

import org.json.JSONObject;

public class PopupGroup {
    private FairSplit app;
    private MainActivity mainActivity;
    private PostGroupTask postGroupTask;

    public PopupGroup(FairSplit app) {
        this.app = app;
        this.mainActivity = app.getMainActivityContext();
        postGroupTask = new PostGroupTask();
    }

    public void showGroupPopup(final Group group) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.popup_group, null);

        final EditText groupNameEdit = dialogView.findViewById(R.id.user_name);
        if (group != null) {
            groupNameEdit.setText(group.groupName);
            groupNameEdit.setSelection(groupNameEdit.getText().length());
        }

        alert.setView(dialogView);
        alert.setTitle(String.format("Edit/remove %s?", group.groupName));

        // OK button
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Group toPost = (group != null) ? group : new Group();
                toPost.groupName = groupNameEdit.getText().toString();
                postGroupTask.execute(toPost);
            }
        });

        // Delete button, only show for existing groups and as long as it's not the last group
        if (group != null && app.getGroupsOwnedByUserID(app.getCurrentUser().userID).size() > 1) {
            alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    group.deleteMe = true;
                    postGroupTask.execute(group);
                }
            });
        }

        // Cancel button
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        final AlertDialog alertDialog = alert.create();

        groupNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (groupNameEdit.getText().toString().isEmpty()) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
    }

    private class PostGroupTask extends AsyncTask<Group, Void, Group> {
        protected Group doInBackground(Group... group) {
            try {
                // Add new group
                if (group[0].groupID == 0) {
                    JSONObject groupResponse = RESTHelper.POST(
                        RESTHelper.groupEndpoint,
                        group[0].toJSONObject(),
                        app.getCurrentUser().apiKey,
                        mainActivity
                    );
                    if (groupResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    JSONObject newGroup = groupResponse.getJSONArray("data").getJSONObject(0);
                    return new Group(newGroup);

                // Delete existing group
                } else if (group[0].deleteMe) {
                    JSONObject groupResponse = RESTHelper.DELETE(
                        RESTHelper.groupEndpoint + "/" + group[0].groupID,
                        group[0].toJSONObject(),
                        app.getCurrentUser().apiKey,
                        mainActivity
                    );
                    if (groupResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return group[0];

                // Update existing group
                } else {
                    JSONObject groupResponse = RESTHelper.PUT(
                        RESTHelper.groupEndpoint + "/" + group[0].groupID,
                        group[0].toJSONObject(),
                        app.getCurrentUser().apiKey,
                        mainActivity
                    );
                    if (groupResponse.getInt("errorCode") > 0) {
                        return null;
                    }
                    return group[0];
                }
            } catch (Exception ex) {
                Log.e("PostGroupTask", ex.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Group group) {
            if (group != null) {
                updateGroup(group);
            } else {
                Snackbar.make(
                    mainActivity.findViewById(R.id.logo),
                    R.string.group_modification_failed,
                    Snackbar.LENGTH_LONG
                ).show();
            }
        }
    }

    private void updateGroup(Group group) {
        // Safety net
        if (app.getSelectedUser() == app.getCurrentUser()) {
            if (!app.getAllGroups().contains(group)) {
                app.getAllGroups().add(group);
            }
            if (mainActivity.groupsAdapter.getPosition(group) < 0) {
                mainActivity.groupsAdapter.add(group);
            }
            if (group.deleteMe) {
                mainActivity.groupsAdapter.remove(group);
                app.getAllGroups().remove(group);
                // Select the first group and select current user, since we don't
                // know what users are available in the group2
                app.setCurrentGroup(app.getAllGroups().get(0));
                app.setSelectedUser(app.getCurrentUser());
            } else {
                app.setCurrentGroup(group);
            }
            mainActivity.groupsAdapter.notifyDataSetChanged();
        }
    }
}
