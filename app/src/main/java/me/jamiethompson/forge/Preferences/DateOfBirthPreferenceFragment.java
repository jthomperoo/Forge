package me.jamiethompson.forge.Preferences;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import org.apache.commons.lang3.StringUtils;

import me.jamiethompson.forge.R;

/**
 * Created by jamie on 03/10/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DateOfBirthPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    final int ABSOLUTE_MAX_AGE = 100;
    final int ABSOLUTE_MIN_AGE = 0;
    int minAge;
    int maxAge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_date_of_birth);
        setHasOptionsMenu(true);
        EditTextPreference minAgePref = (EditTextPreference) getPreferenceScreen().findPreference(getString(R.string.pref_dob_min_key));
        EditTextPreference maxAgePref = (EditTextPreference) getPreferenceScreen().findPreference(getString(R.string.pref_dob_max_key));
        minAge = Integer.valueOf(minAgePref.getText());
        maxAge = Integer.valueOf(maxAgePref.getText());
        minAgePref.setOnPreferenceChangeListener(this);
        maxAgePref.setOnPreferenceChangeListener(this);
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String text = String.valueOf(o);
        if (!text.isEmpty()) {
            if (preference.getKey() == getString(R.string.pref_dob_max_key)) {
                if(StringUtils.isNumeric(text)) {
                    int value = Integer.valueOf(text);
                    if (value > ABSOLUTE_MAX_AGE) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.invalid_preference));
                        builder.setMessage(getString(R.string.error_age_too_high) + ABSOLUTE_MAX_AGE);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        return false;
                    }
                    if (value < ABSOLUTE_MIN_AGE) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.invalid_preference));
                        builder.setMessage(getString(R.string.error_age_too_low) + ABSOLUTE_MIN_AGE);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        return false;
                    }
                    if (value < minAge) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.invalid_preference));
                        builder.setMessage(getString(R.string.error_min_age_greater_max));
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        return false;
                    }
                    maxAge = value;
                    return true;
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_must_be_numeric));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
            }
            if (preference.getKey() == getString(R.string.pref_dob_min_key)) {
                if (StringUtils.isNumeric(text)) {
                    int value = Integer.valueOf(text);
                    if (value > ABSOLUTE_MAX_AGE) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.invalid_preference));
                        builder.setMessage(getString(R.string.error_age_too_high) + ABSOLUTE_MAX_AGE);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        return false;
                    }
                    if (value < ABSOLUTE_MIN_AGE) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.invalid_preference));
                        builder.setMessage(getString(R.string.error_age_too_low) + ABSOLUTE_MIN_AGE);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        return false;
                    }
                    if (value > maxAge) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.invalid_preference));
                        builder.setMessage(getString(R.string.error_min_age_greater_max));
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        return false;
                    }
                    minAge = value;
                    return true;
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.invalid_preference));
                    builder.setMessage(getString(R.string.error_must_be_numeric));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    return false;
                }
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
