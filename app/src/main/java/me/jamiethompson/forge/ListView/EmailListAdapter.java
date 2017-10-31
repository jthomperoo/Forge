package me.jamiethompson.forge.ListView;

import android.content.Context;
import android.support.annotation.NonNull;
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
 * Adapter for the Forge email inbox list
 */

public class EmailListAdapter extends ArrayAdapter<EmailMessage> {

    public EmailListAdapter(Context context, int resource, List<EmailMessage> emails) {
        super(context, resource, emails);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_email, null);
        }
        // Get the email that has been created
        EmailMessage email = getItem(position);

        if (email != null) {
            TextView subject = v.findViewById(R.id.subject);
            // If email has been read, set read checkbox to checked, unchecked if not read
            ((CheckBox) v.findViewById(R.id.read)).setChecked(!email.isRead());
            // Set from textbox to the sender email address
            ((TextView) v.findViewById(R.id.from)).setText(email.getFrom());
            // Set the time received textbox to the time received
            ((TextView) v.findViewById(R.id.time)).setText(email.getTime());
            if (subject != null) {
                // If there is a subject, set the subject to the email subject in the object
                subject.setText(email.getSubject());
            }
        }

        return v;
    }


}
