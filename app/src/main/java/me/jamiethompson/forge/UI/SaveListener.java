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
 * Handles user interaction once a user attempts to save an account, handling overwriting, saving
 * as new and cancelling saving
 */

public class SaveListener implements DialogInterface.OnClickListener {

    // Account being saved
    private ForgeAccount account;
    // Calling activity
    private Activity activity;
    // Activity view
    private View view;

    /**
     * @param account  the account being saved
     * @param activity the activity calling the save dialog
     * @param view     the activity view
     */
    public SaveListener(ForgeAccount account, Activity activity, View view) {
        this.account = account;
        this.activity = activity;
        this.view = view;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                // On overwrite
                // Replace any existing account in the store with the new version
                ForgeAccount saveAccount = FileManager.replace(activity, account);
                if (saveAccount != null) {
                    // If the account save is successful, display a message
                    account = saveAccount;
                    Feedback.displayMessage(activity.getString(R.string.message_account_saved), view);
                }
                // Reload the storage list and close the dialog
                ((Forge) activity).reloadSaveList();
                dialog.dismiss();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE: {
                // On save as new
                // Add a new account to the store
                ForgeAccount saveAccount = FileManager.add(activity, account);
                if (saveAccount != null) {
                    // If the account save is successful, display a message
                    account = saveAccount;
                    Feedback.displayMessage(activity.getString(R.string.message_account_saved), view);
                }
                // Reload the storage list and close the dialog
                ((Forge) activity).reloadSaveList();
                dialog.dismiss();
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                // On cancel
                dialog.dismiss();
                break;
            }
        }
    }
}
