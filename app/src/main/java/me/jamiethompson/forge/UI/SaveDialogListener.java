package me.jamiethompson.forge.UI;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.FileManager;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.TabActivity.Forge;

/**
 * Created by jamie on 02/10/17.
 */

public class SaveDialogListener implements DialogInterface.OnClickListener {

    private ForgeAccount account;
    private Activity activity;
    private View view;


    public SaveDialogListener(ForgeAccount account, Activity activity, View view) {
        this.account = account;
        this.activity = activity;
        this.view = view;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                ForgeAccount saveAccount = FileManager.replace(activity, account);
                if (saveAccount != null) {
                    account = saveAccount;
                    Feedback.displayMessage(activity.getString(R.string.message_account_saved), view);
                }
                ((Forge) activity).reloadSaveList();
                dialog.dismiss();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE: {
                ForgeAccount saveAccount = FileManager.add(activity, account);
                if (saveAccount != null) {
                    account = saveAccount;
                    Feedback.displayMessage(activity.getString(R.string.message_account_saved), view);
                }
                ((Forge) activity).reloadSaveList();
                dialog.dismiss();
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                dialog.dismiss();
                break;
            }
        }
    }
}
