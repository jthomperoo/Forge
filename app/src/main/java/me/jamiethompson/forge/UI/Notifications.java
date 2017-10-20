package me.jamiethompson.forge.UI;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import me.jamiethompson.forge.Constants;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.Services.NotificationClickReceiver;
import me.jamiethompson.forge.Services.OverlayService;
import me.jamiethompson.forge.TabActivity.Forge;

/**
 * Created by jamie on 02/10/17.
 */

public class Notifications {
    public static void setUpChannels(Activity activity) {
        NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL, activity.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    public static void displayHelperNotification(Activity activity, Boolean override, Boolean overrideOverlay) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        if (override) {
            createHelperNotification(activity, overrideOverlay);
        } else {
            if (sharedPref.getBoolean(activity.getString(R.string.pref_helper_key), true)) {
                createHelperNotification(activity, overrideOverlay);
            }
        }
    }

    public static void displayHelperNotification(Activity activity, Boolean override) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        if (override) {
            createHelperNotification(activity, sharedPref.getBoolean(activity.getString(R.string.pref_overlay_key), false));
        } else {
            if (sharedPref.getBoolean(activity.getString(R.string.pref_helper_key), true)) {
                createHelperNotification(activity, sharedPref.getBoolean(activity.getString(R.string.pref_overlay_key), false));
            }
        }
    }

    private static void createHelperNotification(Context context, boolean showOverlay) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder;
        String title = context.getString(R.string.helper_notification_title);
        String text = context.getString(R.string.helper_notification_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.forge_logo_small)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setChannelId(Constants.NOTIFICATION_CHANNEL)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setTicker(null)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setVibrate(null)
                    .setSound(null);
        } else {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.forge_logo_small)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setTicker(null)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setVibrate(null)
                    .setSound(null);
        }


        Intent notificationReciever = new Intent(context, NotificationClickReceiver.class);
        PendingIntent pendingIntentAutoFill = PendingIntent.getBroadcast(context, 0, notificationReciever, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntentAutoFill);

        Intent generateIntent = new Intent(context, Forge.class);
        generateIntent.putExtra(Constants.NOTIFICATION_NAVIGATION, Constants.GENERATE_TAB);
        PendingIntent generatePendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        generateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.addAction(new Notification.Action(R.drawable.icon_generate, context.getString(R.string.helper_notification_generate), generatePendingIntent));

        Intent storeIntent = new Intent(context, Forge.class);
        generateIntent.putExtra(Constants.NOTIFICATION_NAVIGATION, Constants.STORE_TAB);
        PendingIntent storePendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        storeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        notificationBuilder.addAction(new Notification.Action(R.drawable.icon_store, context.getString(R.string.helper_notification_store), storePendingIntent));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (showOverlay) {
            Intent overlayIntent = new Intent(context, OverlayService.class);
            PendingIntent overlayPendingIntent =
                    PendingIntent.getService(
                            context,
                            0,
                            overlayIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            notificationBuilder.addAction(new Notification.Action(R.drawable.icon_helper, context.getString(R.string.helper_notification_overlay), overlayPendingIntent));
        }

        notificationManager.notify(Constants.HELPER_NOTIFICATION_TAG, Constants.HELPER_NOTIFICATION_ID, notificationBuilder.build());
    }

    public static void removeHelperNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.HELPER_NOTIFICATION_TAG, Constants.HELPER_NOTIFICATION_ID);
    }
}
