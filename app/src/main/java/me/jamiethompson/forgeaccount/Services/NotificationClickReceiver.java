package me.jamiethompson.forgeaccount.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by jamie on 04/10/17.
 */

public class NotificationClickReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		boolean canDraw = true;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			if (Settings.canDrawOverlays(context))
			{
				canDraw = true;
			}
		}
		if (canDraw)
		{
			context.startService(new Intent(context, OverlayService.class));
		}
	}
}
