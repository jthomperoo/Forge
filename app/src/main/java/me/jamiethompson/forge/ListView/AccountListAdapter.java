package me.jamiethompson.forge.ListView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
 */

public class AccountListAdapter extends ArrayAdapter<ForgeAccount> {

    private Activity activity;
    private ReloadInterface listFragment;

    public AccountListAdapter(Context context, int resource, List<ForgeAccount> accounts, Activity activity, ReloadInterface listFragment) {
        super(context, resource, accounts);
        this.activity = activity;
        this.listFragment = listFragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_account, null);
        }

        final ForgeAccount account = getItem(position);

        if (account != null) {
            TextView subject = v.findViewById(R.id.name);
            v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    FileManager.delete(activity, account);
                                    listFragment.reload();
                                    dialog.dismiss();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(activity.getString(R.string.dialog_delete))
                            .setPositiveButton(activity.getString(R.string.option_delete), dialogClickListener)
                            .setNegativeButton(activity.getString(R.string.option_cancel), dialogClickListener).show();
                }
            });

            if (subject != null) {
                String accountTitle = account.getAccountName();
                if (accountTitle.isEmpty()) {
                    accountTitle = account.getEmail().getAddress();
                }
                subject.setText(accountTitle);
            }
        }

        return v;
    }

}
