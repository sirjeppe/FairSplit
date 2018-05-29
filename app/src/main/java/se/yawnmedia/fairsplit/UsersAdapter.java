package se.yawnmedia.fairsplit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class UsersAdapter extends ArrayAdapter<User> {
    private FairSplit app;

    private static class ViewHolder {
        RadioButton userRadioButton;
        TextView userName;
    }

    public UsersAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        app = ((FairSplit) context.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.user_item, parent, false);
            viewHolder.userRadioButton = convertView.findViewById(R.id.user_radio_button);
            viewHolder.userName = convertView.findViewById(R.id.user_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.userName.setText(user.userName);

        if (user == app.getSelectedUser()) {
            viewHolder.userRadioButton.setChecked(true);
        } else {
            viewHolder.userRadioButton.setChecked(false);
        }

        viewHolder.userRadioButton.setTag(user);

        return convertView;
    }
}
