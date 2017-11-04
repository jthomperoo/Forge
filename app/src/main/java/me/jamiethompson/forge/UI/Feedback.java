package me.jamiethompson.forge.UI;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

/**
 * Created by jamie on 27/09/17.
 * Handles snackbar feedback to the user
 */

public class Feedback {
    /**
     * Displays a snackbar message to the user
     *
     * @param message the message to display
     * @param view    the view to display it in
     */
    public static void displayMessage(String message, View view) {
        Snackbar snackbarMessage = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbarMessage.show();
    }

    /**
     * Displays a toast message to the user
     *
     * @param context calling context
     * @param message the message to display
     */
    public static void displayToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showDialog(final Activity activity, String title, String message, final Intent action) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (action != null) {
                    activity.startActivityForResult(action, 0);
                } else {
                    dialog.dismiss();
                }
            }
        });
        if (action != null) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        }
        builder.show();
    }
}
