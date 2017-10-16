package me.jamiethompson.forgeaccount.Preferences;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import me.jamiethompson.forgeaccount.R;

/**
 * Created by jamie on 03/10/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PasswordPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    final int ABSOLUTE_MAX_PASSWORD_LENGTH = 128;
    final int ABSOLUTE_MIN_PASSWORD_LENGTH = 1;
    int minPassLen;
    int maxPassLen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_password);
        setHasOptionsMenu(true);
        EditTextPreference minPassLenPref = (EditTextPreference) getPreferenceScreen().findPreference(getString(R.string.pref_password_min_key));
        EditTextPreference maxPassLenPref = (EditTextPreference) getPreferenceScreen().findPreference(getString(R.string.pref_password_max_key));
        minPassLen = Integer.valueOf(minPassLenPref.getText());
        maxPassLen = Integer.valueOf(maxPassLenPref.getText());
        minPassLenPref.setOnPreferenceChangeListener(this);
        maxPassLenPref.setOnPreferenceChangeListener(this);
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
        String text = String.valueOf(o);
        if (!text.isEmpty()) {
            if (preference.getKey() == getString(R.string.pref_password_max_key)) {
                int value = Integer.valueOf(text);
                if (value > ABSOLUTE_MAX_PASSWORD_LENGTH) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_pass_long) + ABSOLUTE_MAX_PASSWORD_LENGTH);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
                if (value < ABSOLUTE_MIN_PASSWORD_LENGTH) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_pass_short) + ABSOLUTE_MIN_PASSWORD_LENGTH);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
                if (value <= minPassLen) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_min_pass_greater_max));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
                maxPassLen = value;
                return true;
            }
            if (preference.getKey() == getString(R.string.pref_password_min_key)) {
                int value = Integer.valueOf(text);
                if (value > ABSOLUTE_MAX_PASSWORD_LENGTH) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_pass_long) + ABSOLUTE_MAX_PASSWORD_LENGTH);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
                if (value < ABSOLUTE_MIN_PASSWORD_LENGTH) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_pass_short) + ABSOLUTE_MIN_PASSWORD_LENGTH);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
                if (value >= maxPassLen) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(R.string.error_min_pass_greater_max);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
                minPassLen = value;
                return true;
            }

            return true;
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.invalid_preference));
            builder.setMessage(getString(R.string.error_value_required));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
            return false;
        }
    }
}
