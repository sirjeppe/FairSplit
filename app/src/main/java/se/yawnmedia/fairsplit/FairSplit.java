package se.yawnmedia.fairsplit;

import android.app.Application;

import java.util.ArrayList;

public class FairSplit extends Application {
    private User currentUser;
    private Group currentGroup;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<Group> allGroups = new ArrayList<>();

    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User user) {
        currentUser = user;
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
    public void addToAllUsers(User user) {
        allUsers.add(user);
    }

    public ArrayList<Group> getAllGroups() {
        return allGroups;
    }
    public void addToAllGroups(Group group) {
        allGroups.add(group);
    }
}
