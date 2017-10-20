package me.jamiethompson.forge.Preferences;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Notifications;
import me.jamiethompson.forge.Util;

/**
 * Created by jamie on 03/10/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);
        SwitchPreference helperPreference = ((SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.pref_helper_key)));
        SwitchPreference overlayPreference = ((SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.pref_overlay_key)));
        overlayPreference.setOnPreferenceChangeListener(this);
        helperPreference.setOnPreferenceChangeListener(this);
        if (!Util.isAccessibilitySettingsOn(getActivity().getApplicationContext())) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getActivity().getString(R.string.pref_overlay_key), false);
            editor.putBoolean(getActivity().getString(R.string.pref_helper_key), false);
            helperPreference.setChecked(false);
            overlayPreference.setChecked(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), Preferences.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey() == getString(R.string.pref_helper_key)) {
            if ((boolean) o) {
                if (Util.isAccessibilitySettingsOn(getActivity().getApplicationContext())) {
                    Notifications.displayHelperNotification(getActivity(), true);
                    return true;
                } else {
                    showDialog(getString(R.string.accessibility_not_enabled), getString(R.string.error_accessibility_not_enabled), new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    return false;
                }
            } else {
                Notifications.removeHelperNotification(getActivity());
            }
        }

        if (preference.getKey() == getString(R.string.pref_overlay_key)) {
            if ((boolean) o) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                if (sharedPref.getBoolean(getActivity().getString(R.string.pref_helper_key), false)) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(getContext())) {
                            Notifications.displayHelperNotification(getActivity(), false, true);
                            return true;
                        } else {
                            showDialog(getString(R.string.draw_overlay_not_enabled), getString(R.string.error_draw_overlay_not_enabled), new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                            return false;
                        }
                    } else {
                        Notifications.displayHelperNotification(getActivity(), false, true);
                        return true;
                    }

                } else {
                    showDialog(getString(R.string.helper_not_enabled), getString(R.string.error_helper_not_enabled), null);
                    return false;
                }
            } else {
                Notifications.displayHelperNotification(getActivity(), false, false);
            }
        }
        return true;
    }


    private void showDialog(String title, String message, final Intent action) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (action != null) {
                    startActivityForResult(action, 0);
                } else {
                    dialog.dismiss();
                }
            }
        });
        if (action != null) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        }
        builder.show();
    }


}
