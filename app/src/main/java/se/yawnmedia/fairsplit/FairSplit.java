package se.yawnmedia.fairsplit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class FairSplit extends Application {
    private User currentUser;
    private User selectedUser;
    private Group currentGroup;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<Group> allGroups = new ArrayList<>();
    private SharedPreferences prefs;

    public void setupAppPrefs(Context context) {
        prefs = context.getSharedPreferences("se.yawnmedia.fairsplit.app", Context.MODE_PRIVATE);
    }

    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public User getSelectedUser() {
        return selectedUser;
    }
    public void setSelectedUser(User user) {
        selectedUser = user;
    }

    public Group getCurrentGroup() {
        return currentGroup;
    }
    public void setCurrentGroup(Group group) {
        currentGroup = group;
    }

    public ArrayList<User> getAllUsers() {
        return allUsers;
    }
    public User getUserByID(int userID) {
        for (User user : allUsers) {
            if (user.userID == userID) {
                return user;
            }
        }
        return null;
    }
    public void addToAllUsers(User user) {
        if (!allUsers.contains(user)) {
            allUsers.add(user);
        }
    }

    public ArrayList<Group> getAllGroups() {
        return allGroups;
    }
    public Group getGroupByID(int groupID) {
        for (Group group : allGroups) {
            if (group.groupID == groupID) {
                return group;
            }
        }
        return null;
    }
    public void addToAllGroups(Group group) {
        if (!allGroups.contains(group)) {
            allGroups.add(group);
        }
    }

    public boolean userInList(int userID) {
        for (User user : allUsers) {
            if (user.userID == userID) {
                return true;
            }
        }
        return false;
    }

    public void setAPIKey(String apiKey) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("apiKey", apiKey);
        editor.commit();
    }
    public String getAPIKey() {
        return prefs.getString("apiKey", "");
    }

    public void setUserID(int userID) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("userID", userID);
        editor.commit();
    }
    public int getUserID() {
        return prefs.getInt("userID", 0);
    }

    public void setLoginName(String userName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("loginName", userName);
        editor.commit();
    }
    public String getLoginName() { return prefs.getString("loginName", ""); }
}
