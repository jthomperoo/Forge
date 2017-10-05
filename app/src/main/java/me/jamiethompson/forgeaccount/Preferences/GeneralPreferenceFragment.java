package me.jamiethompson.forgeaccount.Preferences;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.UI.Notifications;

/**
 * Created by jamie on 03/10/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
		setHasOptionsMenu(true);
		SwitchPreference helperPreference = ((SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.pref_helper_key)));
		helperPreference.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == android.R.id.home)
		{
			startActivity(new Intent(getActivity(), Preferences.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public boolean onPreferenceChange(Preference preference, Object o)
	{
		if (preference.getKey() == getString(R.string.pref_helper_key))
		{
			if ((boolean) o)
			{
				Notifications.displayHelperNotification(getActivity(), true);
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				{
					if (Settings.canDrawOverlays(getContext()))
					{
						Notifications.displayHelperNotification(getActivity(), true);
						return true;
					}
					else
					{
						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(getString(R.string.draw_overlay_not_enabled));
						builder.setMessage(getString(R.string.error_draw_overlay_not_enabled));
						builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
								startActivityForResult(intent, 0);
							}
						});
						builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.dismiss();
							}
						});
						builder.show();
					}
				}
				else
				{
					return true;
				}
			}
			else
			{
				Notifications.removeHelperNotification(getActivity());
			}
		}
		return true;
	}


}
