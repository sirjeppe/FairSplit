package se.yawnmedia.fairsplit;

import java.util.ArrayList;

public class Group {
    public int groupID;
    public String groupName;
    public ArrayList<User> members;

    public Group(int groupID) {
        this.groupID = groupID;
        this.groupName = "Dummy group";
        this.members = new ArrayList<User>();
    }
}
