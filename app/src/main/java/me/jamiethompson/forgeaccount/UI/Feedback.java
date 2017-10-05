package me.jamiethompson.forgeaccount.UI;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by jamie on 27/09/17.
 */

public class Feedback
{
	public static void displayMessage(String message, View view)
	{
		Snackbar snackbarMessage = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
		snackbarMessage.show();
	}
}
