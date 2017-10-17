package me.jamiethompson.forge.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jamie on 16/10/17.
 */

public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AccessibilityAutofillService.instance.autofill();
    }
}
