package me.jamiethompson.forge.UI;

import android.support.design.widget.Snackbar;
import android.view.View;

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
}
