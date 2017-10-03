package me.jamiethompson.forgeaccount.Preferences;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import me.jamiethompson.forgeaccount.R;

/**
 * Created by jamie on 03/10/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PasswordPreferenceFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_password);
		setHasOptionsMenu(true);
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
}
