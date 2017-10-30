package me.jamiethompson.forge.Files;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.jamiethompson.forge.Constants.General;
import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.R;

/**
 * Created by jamie on 29/09/17.
 * Handles permanent account storage
 */

public class FileManager {
    /**
     * Gets the stored Forge accounts from the storage
     *
     * @param context calling context
     * @return a hash map of all Forge accounts stored
     */
    public static HashMap<UUID, ForgeAccount> load(Context context) {
        // Create new hash map
        HashMap<UUID, ForgeAccount> accounts = new HashMap<>();
        // If storage file exists
        if (context.getFileStreamPath(context.getString(R.string.filename_forge_accounts)).exists()) {
            // Open file stream
            FileInputStream inputStream;
            try {
                inputStream = context.openFileInput(context.getString(R.string.filename_forge_accounts));
                // Open input stream
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                // Place the file contents into a string
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                // Initialise GSON
                Gson gson = new Gson();
                // Create a list of Forge Accounts from the JSON string
                List<ForgeAccount> accountList =
                        gson.fromJson(stringBuilder.toString(), new TypeToken<List<ForgeAccount>>() {
                        }.getType());
                // Iterate through each Forge account and add it to the hash map
                for (ForgeAccount account : accountList) {
                    accounts.put(account.getId(), account);
                }
            } catch (Exception e) {
                // If there is an error, log it
                Log.e(General.ERROR_LOG, e.getMessage());
            }
        }
        return accounts;
    }

    /**
     * Adds an account to the Forge account storage
     *
     * @param context calling context
     * @param account the Forge account to add to the storage
     * @return the account added to the storage, if it's a failure returns null
     */
    public static ForgeAccount add(Context context, ForgeAccount account) {
        // Initialise GSON
        Gson gson = new Gson();
        // Get the already saved Forge accounts
        HashMap<UUID, ForgeAccount> accounts = FileManager.load(context);
        // Give the account a random UID
        account.setId(java.util.UUID.randomUUID());
        // Add the new account to the hash map
        accounts.put(account.getId(), account);
        // Convert the list of Forge Accounts to a JSON string
        String jsonString = gson.toJson(new ArrayList<>(accounts.values()));
        // Internal save
        FileOutputStream outputStream;
        try {
            // Save the JSON to the file
            outputStream = context.openFileOutput(context.getString(R.string.filename_forge_accounts), Context.MODE_PRIVATE);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
            return account;
        } catch (Exception e) {
            // If there is an error, log it
            Log.e(General.ERROR_LOG, e.getMessage());
            return null;
        }
    }

    /**
     * Replaces an existing Forge account in the storage
     *
     * @param context calling context
     * @param account the Forge account to replace the original with
     * @return the account added to the storage, if it's a failure returns null
     */
    public static ForgeAccount replace(Context context, ForgeAccount account) {
        // Initialise GSON
        Gson gson = new Gson();
        // Get the already saved Forge accounts
        HashMap<UUID, ForgeAccount> accounts = FileManager.load(context);
        // Replace the account with the matching UUID with the new account details
        accounts.put(account.getId(), account);
        // Convert the list of Forge Accounts to a JSON string
        String jsonString = gson.toJson(new ArrayList<>(accounts.values()));
        // Internal save
        FileOutputStream outputStream;
        try {
            // Save the JSON to the file
            outputStream = context.openFileOutput(context.getString(R.string.filename_forge_accounts), Context.MODE_PRIVATE);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
            return account;
        } catch (Exception e) {
            // If there is an error, log it
            Log.e(General.ERROR_LOG, e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a Forge account from the storage
     *
     * @param context calling context
     * @param account the Forge account to delete
     * @return true = successfully deleted, false = failure in delete attempt
     */
    public static boolean delete(Context context, ForgeAccount account) {
        // Initialise GSON
        Gson gson = new Gson();
        // Get the already saved Forge accounts
        HashMap<UUID, ForgeAccount> accounts = FileManager.load(context);
        // Remove the account with the matching UUID
        accounts.remove(account.getId());
        // Convert the list of Forge Accounts to a JSON string
        String jsonString = gson.toJson(new ArrayList<>(accounts.values()));
        // Internal save
        FileOutputStream outputStream;
        try {
            // Save the JSON to the file
            outputStream = context.openFileOutput(context.getString(R.string.filename_forge_accounts), Context.MODE_PRIVATE);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            // If there is an error, log it
            Log.e(General.ERROR_LOG, e.getMessage());
            return false;
        }
    }
}
