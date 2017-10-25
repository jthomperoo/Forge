package me.jamiethompson.forge;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import me.jamiethompson.forge.Constants.General;
import me.jamiethompson.forge.Services.Autofill.AccessibilityAutofillService;

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
            Log.e(General.ERROR_LOG, e.getMessage());
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
}
