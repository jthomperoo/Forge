package me.jamiethompson.forge.ListView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.FileManager;
import me.jamiethompson.forge.Interfaces.ReloadInterface;
import me.jamiethompson.forge.R;

/**
 * Created by jamie on 29/09/17.
 * Adapter for the storage Forge account list
 */

public class AccountListAdapter extends ArrayAdapter<ForgeAccount> {
    // The activity the list is in
    private Activity activity;
    // Interface for reloading the listview
    private ReloadInterface listFragment;

    public AccountListAdapter(Context context, int resource, List<ForgeAccount> accounts, Activity activity, ReloadInterface listFragment) {
        super(context, resource, accounts);
        this.activity = activity;
        this.listFragment = listFragment;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_account, null);
        }
        // Get the account that has been created
        final ForgeAccount account = getItem(position);

        if (account != null) {
            TextView name = v.findViewById(R.id.name);
            // Set on delete button click listener
            v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dialog on click listener
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // If the user hits the delete button, delete the account,
                                    // reload the listview and dismiss this dialog
                                    FileManager.delete(activity, account);
                                    listFragment.reload();
                                    dialog.dismiss();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    // If the user hits the cancel button, dismiss this dialog
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };
                    // Build and show the dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(activity.getString(R.string.dialog_delete))
                            .setPositiveButton(activity.getString(R.string.option_delete), dialogClickListener)
                            .setNegativeButton(activity.getString(R.string.option_cancel), dialogClickListener).show();
                }
            });

            if (name != null) {
                // Set account title to the account name provided if it exists
                String accountTitle = account.getAccountName();
                if (accountTitle.isEmpty()) {
                    // If no account name provided, use the email address connected with the account
                    accountTitle = account.getEmail().getAddress();
                }
                // Set the list item title to the account title
                name.setText(accountTitle);
            }
        }

        return v;
    }

}
