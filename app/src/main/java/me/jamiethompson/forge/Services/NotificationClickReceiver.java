package me.jamiethompson.forge.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Notifications;
import me.jamiethompson.forge.Util;

/**
 * Created by jamie on 16/10/17.
 */

public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.isAccessibilitySettingsOn(context)) {
            AccessibilityAutofillService.instance.autofill();
        } else {
            Toast.makeText(context, context.getString(R.string.error_accessibility_not_enabled), Toast.LENGTH_LONG).show();
            Notifications.removeHelperNotification(context);
        }
    }
}
