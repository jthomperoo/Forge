package me.jamiethompson.forge;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import me.jamiethompson.forge.Services.Autofill.AccessibilityAutofillService;
import me.jamiethompson.forge.TabActivity.Forge;

/**
 * Created by Jamie on 17/10/2017.
 * Handles all global utility methods and functions
 */

public class Util {

    /**
     * Checks if the Forge Auto fill accessibility service is enabled
     *
     * @param context application context
     * @return true = service enabled, false = service disabled
     */
    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + AccessibilityAutofillService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            // If there is an error, log it
            Log.e(Forge.ERROR_LOG, e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Places a string into the device's clipboard to paste
     *
     * @param context calling context
     * @param label   clip label
     * @param content string to be pasted
     */
    public static void addToClipboard(Context context, String label, String content) {
        // Get the clipboard manager
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // Create the new Clip Data with the label and content
        ClipData clip = ClipData.newPlainText(label, content);
        // Put the clip data into the clipboard manager
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Taken from Stack Overflow - https://stackoverflow.com/a/4239019
     * Checks if there is an available network connection
     *
     * @return true = connection available, false = no connection
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (NullPointerException e) {
            Log.e(Forge.ERROR_LOG, e.getMessage());
            return false;
        }
    }
}
