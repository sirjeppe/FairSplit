package se.yawnmedia.fairsplit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class GroupsAdapter extends ArrayAdapter<Group> {
    private FairSplit app;

    private static class ViewHolder {
        RadioButton groupRadioButton;
    }

    public GroupsAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        app = ((FairSplit) context.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Group group = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.group_item, parent, false);
            viewHolder.groupRadioButton = convertView.findViewById(R.id.groupRadioButton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.groupRadioButton.setText(group.groupName);

        if (group == app.getCurrentGroup()) {
            viewHolder.groupRadioButton.setChecked(true);
        } else {
            viewHolder.groupRadioButton.setChecked(false);
        }

        viewHolder.groupRadioButton.setTag(group);

        return convertView;
    }
}
