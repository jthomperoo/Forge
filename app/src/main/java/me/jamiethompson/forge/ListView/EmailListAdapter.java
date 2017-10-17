package me.jamiethompson.forge.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import me.jamiethompson.forge.Data.EmailMessage;
import me.jamiethompson.forge.R;

/**
 * Created by jamie on 27/09/17.
 */

public class EmailListAdapter extends ArrayAdapter<EmailMessage> {

    public EmailListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public EmailListAdapter(Context context, int resource, List<EmailMessage> emails) {
        super(context, resource, emails);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_email, null);
        }

        EmailMessage email = getItem(position);

        if (email != null) {
            TextView subject = v.findViewById(R.id.subject);
            ((CheckBox) v.findViewById(R.id.read)).setChecked(!email.isRead());
            ((TextView) v.findViewById(R.id.from)).setText(email.getFrom());
            ((TextView) v.findViewById(R.id.time)).setText(email.getTime());
            if (subject != null) {
                subject.setText(email.getSubject());
            }
        }

        return v;
    }

}
