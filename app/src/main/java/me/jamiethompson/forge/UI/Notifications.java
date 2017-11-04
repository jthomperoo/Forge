package me.jamiethompson.forge.UI;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import me.jamiethompson.forge.R;
import me.jamiethompson.forge.Services.Autofill.NotificationClickReceiver;
import me.jamiethompson.forge.Services.OverlayService;
import me.jamiethompson.forge.TabActivity.Forge;

/**
 * Created by jamie on 02/10/17.
 * Handles all Android notifications
 */

public class Notifications {

    final public static String NOTIFICATION_NAVIGATION = "notification_nav";
    final public static String NOTIFICATION_CHANNEL = "forge_channel";
    final public static int HELPER_NOTIFICATION_ID = 1;
    final public static String HELPER_NOTIFICATION_TAG = "helper_notification";

    /**
     * Handles setting up a notification channel for the app
     *
     * @param context calling context
     */
    public static void setUpChannels(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If the Android version is Oreo or greater
            // Create a notification channel
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL, context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    /**
     * Creates the helper notification and displays it
     *
     * @param context calling context
     */
    public static void displayHelperNotification(Context context) {
        // Get notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Set up notification builder
        Notification.Builder notificationBuilder;
        // Set notification title and text
        String title = context.getString(R.string.helper_notification_title);
        String text = context.getString(R.string.helper_notification_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If the Android version is Oreo or greater
            // Create notification assigned to notification channel
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.forge_logo_small)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setChannelId(NOTIFICATION_CHANNEL)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setTicker(null)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setVibrate(null)
                    .setSound(null);
        } else {
            // If the Android version is lower than Oreo
            // Create notification without notification channel
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.forge_logo_small)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setTicker(null)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setVibrate(null)
                    .setSound(null);
        }

        // Set up the pending intent for auto fill triggering
        Intent notificationReciever = new Intent(context, NotificationClickReceiver.class);
        PendingIntent pendingIntentAutoFill = PendingIntent.getBroadcast(context, 0, notificationReciever, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set on notification click to trigger auto fill
        notificationBuilder.setContentIntent(pendingIntentAutoFill);

        // Set up the pending intent for loading the Generate tab in the Forge activity
        Intent generateIntent = new Intent(context, Forge.class);
        generateIntent.putExtra(NOTIFICATION_NAVIGATION, Forge.GENERATE_TAB);
        PendingIntent generatePendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        generateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Add the generate pending intent action to trigger when Generate button is pressed
        notificationBuilder.addAction(new Notification.Action(R.drawable.icon_generate, context.getString(R.string.helper_notification_generate), generatePendingIntent));

        // Set up the pending intent for loading the Storage tab in the Forge activity
        Intent storeIntent = new Intent(context, Forge.class);
        generateIntent.putExtra(NOTIFICATION_NAVIGATION, Forge.STORE_TAB);
        PendingIntent storePendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        storeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Add the storage pending intent action to trigger when Storage button is pressed
        notificationBuilder.addAction(new Notification.Action(R.drawable.icon_store, context.getString(R.string.helper_notification_store), storePendingIntent));

        // Open shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPref.getBoolean(context.getString(R.string.pref_overlay_key), false)) {
            // If the overlay is enabled in the preferences
            // Set up the pending intent for opening up the out of app overlay
            Intent overlayIntent = new Intent(context, OverlayService.class);
            PendingIntent overlayPendingIntent =
                    PendingIntent.getService(
                            context,
                            0,
                            overlayIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            // Add the overlay pending intent action to trigger when the Overlay button is pressed
            notificationBuilder.addAction(new Notification.Action(R.drawable.icon_helper, context.getString(R.string.helper_notification_overlay), overlayPendingIntent));
        }

        // Display the notification
        notificationManager.notify(HELPER_NOTIFICATION_TAG,
                HELPER_NOTIFICATION_ID,
                notificationBuilder.build());
    }

    /**
     * Removes any active auto fill helper notification
     *
     * @param context calling context
     */
    public static void removeHelperNotification(Context context) {
        // Get notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Cancel the notification
        notificationManager.cancel(HELPER_NOTIFICATION_TAG, HELPER_NOTIFICATION_ID);
    }
}
