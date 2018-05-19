package se.yawnmedia.fairsplit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jeppe on 2018-03-26.
 */

public class ExpenseAdapter extends ArrayAdapter<Expense> {
    private static class ViewHolder {
        TextView title;
        TextView amount;
        TextView comment;
        TextView datetime;
    }

    public ExpenseAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Expense expense = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.expense_item, parent, false);
            viewHolder.title = convertView.findViewById(R.id.expenseTitle);
            viewHolder.amount = convertView.findViewById(R.id.expenseAmount);
            viewHolder.comment = convertView.findViewById(R.id.expenseComment);
            viewHolder.datetime = convertView.findViewById(R.id.expenseDateTime);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Format DateTime
        long ms = expense.datetime * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateTime = sdf.format(new Date(ms));

        viewHolder.title.setText(expense.title);
        viewHolder.amount.setText("" + expense.amount);
        viewHolder.comment.setText(expense.comment);
        viewHolder.datetime.setText(dateTime);

        viewHolder.title.setTag(expense);

        return convertView;
    }
}
