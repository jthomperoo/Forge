package me.jamiethompson.forgeaccount.UI;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import me.jamiethompson.forgeaccount.Constants;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.TabActivity.Forge;

/**
 * Created by jamie on 02/10/17.
 */

public class Notifications
{
	public static void setUpChannels(Activity activity)
	{
		NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{

			NotificationChannel mChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL, activity.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_MIN);
			mNotificationManager.createNotificationChannel(mChannel);
		}
	}

	public static void displayHelperNotification(Activity activity)
	{
		NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder mBuilder;
		String title = activity.getString(R.string.helper_notification_title);
		String text = activity.getString(R.string.helper_notification_text);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			mBuilder = new Notification.Builder(activity)
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
		}
		else
		{
			mBuilder = new Notification.Builder(activity)
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

		Intent generateIntent = new Intent(activity, Forge.class);
		generateIntent.putExtra(Constants.NOTIFICATION_NAVIGATION, Constants.GENERATE_TAB);
		PendingIntent generatePendingIntent =
				PendingIntent.getActivity(
						activity,
						0,
						generateIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

		mBuilder.addAction(new Notification.Action(R.drawable.preferences_icon, activity.getString(R.string.helper_notification_generate), generatePendingIntent));

		Intent storeIntent = new Intent(activity, Forge.class);
		generateIntent.putExtra(Constants.NOTIFICATION_NAVIGATION, Constants.STORE_TAB);
		PendingIntent storePendingIntent =
				PendingIntent.getActivity(
						activity,
						0,
						generateIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

		mBuilder.addAction(new Notification.Action(R.drawable.preferences_icon, activity.getString(R.string.helper_notification_store), storePendingIntent));

		mNotificationManager.notify(Constants.HELPER_NOTIFICATION_ID, mBuilder.build());
	}
}
