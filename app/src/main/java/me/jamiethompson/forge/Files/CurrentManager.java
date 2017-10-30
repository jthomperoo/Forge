package me.jamiethompson.forge.Files;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.R;

/**
 * Created by jamie on 02/10/17.
 */

public class CurrentManager {
    public static void updateCurrentAccount(ForgeAccount account, Context context) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(account);
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.shared_pref_current), jsonString);
        editor.apply();
    }

    public static ForgeAccount loadCurrentAccount(Context context) {
        Gson gson = new Gson();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getString(R.string.shared_pref_current), null);
        if (jsonString != null) {
            return gson.fromJson(jsonString, new TypeToken<ForgeAccount>() {
            }.getType());
        } else {
            return null;
        }
    }
}
