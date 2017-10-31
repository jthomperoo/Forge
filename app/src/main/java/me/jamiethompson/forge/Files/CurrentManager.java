package me.jamiethompson.forge.Files;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.R;

/**
 * Created by jamie on 02/10/17.
 * Handles loading and storing the temporary current Forge account
 */

public class CurrentManager {
    /**
     * Sets the currently loaded account
     *
     * @param account the Forge account to set as currently loaded
     * @param context calling context
     */
    public static void updateCurrentAccount(ForgeAccount account, Context context) {
        Gson gson = new Gson();
        // Convert the account to a JSON string
        String jsonString = gson.toJson(account);
        // Save the JSON account string to local shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.shared_pref_current), jsonString);
        editor.apply();
    }

    /**
     * Gets the currently loaded account
     *
     * @param context calling context
     * @return the currently loaded Forge account
     */
    public static ForgeAccount loadCurrentAccount(Context context) {
        Gson gson = new Gson();
        // Get the JSON string representing the current Forge account
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getString(R.string.shared_pref_current), null);
        if (jsonString != null) {
            // If the JSON string isn't null, convert and return the JSON into a Forge Account object
            return gson.fromJson(jsonString, new TypeToken<ForgeAccount>() {
            }.getType());
        } else {
            return null;
        }
    }
}
