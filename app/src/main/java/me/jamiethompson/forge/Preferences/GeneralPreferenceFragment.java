package me.jamiethompson.forge.Preferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.MenuItem;

import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Feedback;
import me.jamiethompson.forge.UI.Notifications;
import me.jamiethompson.forge.Util;

/**
 * Created by jamie on 03/10/17.
 * Handles the general preferences
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    // Preferences activity
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        // Get the preferences from XML
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);
        SwitchPreference helperPreference = ((SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.pref_helper_key)));
        SwitchPreference overlayPreference = ((SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.pref_overlay_key)));
        overlayPreference.setOnPreferenceChangeListener(this);
        helperPreference.setOnPreferenceChangeListener(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        if (!Util.isAccessibilitySettingsOn(activity.getApplicationContext())) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getActivity().getString(R.string.pref_overlay_key), false);
            editor.putBoolean(getActivity().getString(R.string.pref_helper_key), false);
            editor.apply();
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
        if (preference.getKey().equals(getString(R.string.pref_helper_key))) {
//            REMOVED IN 1.4 DUE TO CHANGE IN ANDROID GUIDELINES
//            if ((boolean) o) {
//                if (Util.isAccessibilitySettingsOn(getActivity().getApplicationContext())) {
//                    return true;
//                } else {
//                    Feedback.showDialog(activity, getString(R.string.accessibility_not_enabled), getString(R.string.error_accessibility_not_enabled), new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
//                    return false;
//                }
//            } else {
//                Notifications.removeHelperNotification(getActivity());
//            }
            if ((boolean) o) {
                Notifications.displayHelperNotification(getActivity());
            } else {
                Notifications.removeHelperNotification(getActivity());
            }
            return true;
        }

        if (preference.getKey().equals(getString(R.string.pref_overlay_key))) {
            if ((boolean) o) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                if (sharedPref.getBoolean(getActivity().getString(R.string.pref_helper_key), false)) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(getContext())) {
                            return true;
                        } else {
                            Feedback.showDialog(activity, getString(R.string.draw_overlay_not_enabled), getString(R.string.error_draw_overlay_not_enabled), new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                            return false;
                        }
                    } else {
                        return true;
                    }

                } else {
                    Feedback.showDialog(activity, getString(R.string.helper_not_enabled), getString(R.string.error_helper_not_enabled), null);
                    return false;
                }
            }
        }
        return true;
    }


}
