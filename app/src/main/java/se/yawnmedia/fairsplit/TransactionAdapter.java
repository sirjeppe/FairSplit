package se.yawnmedia.fairsplit;

import android.content.Context;
import androidx.annotation.NonNull;

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

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    private static class ViewHolder {
        TextView title;
        TextView amount;
        TextView comment;
        TextView datetime;
    }

    public TransactionAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Transaction transaction = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.transaction_item, parent, false);
            viewHolder.title = convertView.findViewById(R.id.transactionTitle);
            viewHolder.amount = convertView.findViewById(R.id.transactionAmount);
            viewHolder.comment = convertView.findViewById(R.id.transactionComment);
            viewHolder.datetime = convertView.findViewById(R.id.transactionDateTime);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Format DateTime
        long ms = transaction.datetime * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateTime = sdf.format(new Date(ms));

        viewHolder.title.setText(transaction.title);
        viewHolder.amount.setText("" + transaction.amount);
        viewHolder.comment.setText(transaction.comment);
        viewHolder.datetime.setText(dateTime);

        viewHolder.title.setTag(transaction);

        return convertView;
    }
}
