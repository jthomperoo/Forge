package me.jamiethompson.forge.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Calendar;

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.CurrentManager;
import me.jamiethompson.forge.Files.FileManager;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Feedback;
import me.jamiethompson.forge.Util;

/**
 * Created by jamie on 05/10/17.
 * Handles displaying and all interactions with the Forge Overlay
 */

public class OverlayService extends Service implements View.OnClickListener {

    // Window manager for drawing over other apps
    private WindowManager windowManager;
    // Overlay view
    private View view;
    // Current Forge Account
    private ForgeAccount account;
    // Application context
    private Context appContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        boolean canDraw = true;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // If the version requires a permission to draw over apps,
            canDraw = Settings.canDrawOverlays(appContext);
        }
        if (canDraw) {
            // If Forge is allowed to draw over other apps
            // Close the notifications status bar
            Intent closeStatusBarIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            appContext.sendBroadcast(closeStatusBarIntent);
            // Load the current account
            account = CurrentManager.loadCurrentAccount(appContext);
            // Get the window manager
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            // Get the layout inflater
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            // Create the view from the forge overlay layout
            view = layoutInflater.inflate(R.layout.forge_overlay, null);

            setUpEditTexts();
            setUpButtons();

            // Set up window manager parameters
            WindowManager.LayoutParams params;

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // If the device is version Oreo or greater
                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSPARENT);
            } else {
                // If the device is pre-Oreo
                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                        PixelFormat.TRANSPARENT);
            }
            params.gravity = Gravity.CENTER;

            // Display the view over the app
            windowManager.addView(view, params);
        } else {
            this.stopSelf();
        }
    }

    /**
     * Sets up the overlay buttons
     */
    private void setUpButtons() {
        // Set up copy buttons
        ImageButton firstname = view.findViewById(R.id.copy_firstname);
        ImageButton middlename = view.findViewById(R.id.copy_middlename);
        ImageButton lastname = view.findViewById(R.id.copy_lastname);
        ImageButton username = view.findViewById(R.id.copy_username);
        ImageButton email = view.findViewById(R.id.copy_email);
        ImageButton password = view.findViewById(R.id.copy_password);
        ImageButton date = view.findViewById(R.id.copy_date);

        // Set listeners for copy buttons
        firstname.setOnClickListener(this);
        middlename.setOnClickListener(this);
        lastname.setOnClickListener(this);
        username.setOnClickListener(this);
        email.setOnClickListener(this);
        password.setOnClickListener(this);
        date.setOnClickListener(this);

        // Set listeners for general clicks, dismiss, save, background and card click
        view.findViewById(R.id.button_dismiss).setOnClickListener(this);
        view.findViewById(R.id.button_save).setOnClickListener(this);
        view.findViewById(R.id.background).setOnClickListener(this);
        view.findViewById(R.id.card_content).setOnClickListener(this);


        // Create copy icon drawable
        Drawable copyIcon = getDrawable(R.drawable.icon_copy);
        // Set each copy button to use the copy icon
        firstname.setImageDrawable(copyIcon);
        middlename.setImageDrawable(copyIcon);
        lastname.setImageDrawable(copyIcon);
        username.setImageDrawable(copyIcon);
        email.setImageDrawable(copyIcon);
        password.setImageDrawable(copyIcon);
        date.setImageDrawable(copyIcon);
    }

    /**
     * Sets up the overlay edit texts
     */
    private void setUpEditTexts() {
        // Set overlay edit texts to use the Forge account details
        ((EditText) view.findViewById(R.id.firstname)).setText(account.getFirstName());
        ((EditText) view.findViewById(R.id.middlename)).setText(account.getMiddleName());
        ((EditText) view.findViewById(R.id.lastname)).setText(account.getLastName());
        ((EditText) view.findViewById(R.id.username)).setText(account.getUsername());
        ((EditText) view.findViewById(R.id.email)).setText(account.getEmail().getAddress());
        ((EditText) view.findViewById(R.id.password)).setText(account.getPassword());
        ((EditText) view.findViewById(R.id.day)).setText(String.valueOf(account.getDateOfBirth().get(Calendar.DAY_OF_MONTH)));
        ((EditText) view.findViewById(R.id.month)).setText(String.valueOf(account.getDateOfBirth().get(Calendar.MONTH) + 1));
        ((EditText) view.findViewById(R.id.year)).setText(String.valueOf(account.getDateOfBirth().get(Calendar.YEAR)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) {
            // If there is an overlay view loaded, remove the overlay view
            windowManager.removeView(view);
        }
    }

    @Override
    public void onClick(View view) {
        boolean finish = false;
        switch (view.getId()) {
            case R.id.copy_firstname: {
                // When copy first name is pressed
                String nameTag = getString(R.string.firstname);
                // Add the first name to the device clipboard for pasting
                Util.addToClipboard(appContext, nameTag, account.getFirstName());
                // Display a message to the user informing them that the first name has been
                // copied to the clipboard
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                // Mark that the overlay should be closed
                finish = true;
                break;
            }
            case R.id.copy_middlename: {
                String nameTag = getString(R.string.middlename);
                Util.addToClipboard(appContext, nameTag, account.getMiddleName());
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                finish = true;
                break;
            }
            case R.id.copy_lastname: {
                String nameTag = getString(R.string.lastname);
                Util.addToClipboard(appContext, nameTag, account.getLastName());
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                finish = true;
                break;
            }
            case R.id.copy_username: {
                String nameTag = getString(R.string.username);
                Util.addToClipboard(appContext, nameTag, account.getUsername());
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                finish = true;
                break;
            }
            case R.id.copy_email: {
                String nameTag = getString(R.string.email);
                Util.addToClipboard(appContext, nameTag, account.getEmail().getAddress());
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                finish = true;
                break;
            }
            case R.id.copy_password: {
                String nameTag = getString(R.string.password);
                Util.addToClipboard(appContext, nameTag, account.getPassword());
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                finish = true;
                break;
            }
            case R.id.copy_date: {
                String nameTag = getString(R.string.date);
                Calendar dob = account.getDateOfBirth();
                // Add the date to the device clipboard in the format YYYY/MM/DD
                Util.addToClipboard(appContext, nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH) + 1, dob.get(Calendar.DAY_OF_MONTH)));
                Feedback.displayToast(appContext, String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
                finish = true;
                break;
            }
            case R.id.button_save: {
                // When the save button is pressed
                account.setAccountName(Calendar.getInstance().getTime().toString());
                // Add the currently loaded account to the storage
                FileManager.add(appContext, account);
                // Display a message to the user telling them the account has been saved
                Feedback.displayToast(appContext, getString(R.string.message_account_saved));
                break;
            }
            case R.id.button_dismiss: {
                // When the dismiss button is pressed, mark the overlay to be closed
                finish = true;
                break;
            }
            case R.id.background: {
                // When the background outside of the overlay is pressed, mark the overlay
                // to be closed
                finish = true;
                break;
            }
        }
        if (finish) {
            // If the overlay has been marked to be closed, stop this, calling onDestroy and
            // removing the overlay
            this.stopSelf();
        }
    }
}
