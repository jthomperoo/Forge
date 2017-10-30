package me.jamiethompson.forge.Generator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jamiethompson.forge.Constants.General;
import me.jamiethompson.forge.Constants.UI;
import me.jamiethompson.forge.Data.EmailAddress;
import me.jamiethompson.forge.Data.EmailMessage;
import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.CurrentManager;
import me.jamiethompson.forge.Interfaces.EmailInterface;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.Web.MailCommunicator;

/**
 * Created by jamie on 02/10/17.
 * Handles generation of a Forge Account
 */

public class ForgeGenerator {
    // List of names available to generate from
    private List<String> names;
    // Mail API communicator
    private MailCommunicator mailComs;
    // Calling source context
    private Context context;

    /**
     * @param callback for any Mail API communicators
     * @param context source context
     */
    public ForgeGenerator(EmailInterface callback, Context context) {
        this.context = context;
        mailComs = new MailCommunicator(callback, context);
        // Load the names from a list
        loadNames();
    }

    /**
     * Refreshes a specific item in the Forge Account
     * @param account the account to refresh the item in
     * @param item the item to refresh
     * @param networkAvailable if the network is available
     * @return the edited Forge Account
     */
    public ForgeAccount refreshItem(ForgeAccount account, String item, boolean networkAvailable) {
        // Set up random number generator
        Random rand = new Random();
        switch (item) {
            case UI.FIRSTNAME: {
                // Set the first name to a random value in the list and update the username
                account.setFirstName(names.get(rand.nextInt(names.size())));
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.MIDDLENAME: {
                // Set the middle name to a random value in the list and update the username
                account.setMiddleName(names.get(rand.nextInt(names.size())));
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.LASTNAME: {
                // Set the last name to a random value in the list and update the username
                account.setLastName(names.get(rand.nextInt(names.size())));
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.USERNAME: {
                // Set the username to use a different random number at the end
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.EMAIL: {
                if (networkAvailable) {
                    // If there is internet access, get the email from the API
                    mailComs.getAddress();
                } else {
                    // Otherwise, generate it locally
                    account.setEmail(generateEmail());
                }
                break;
            }
            case UI.PASSWORD: {
                // Set the password to a randomly generated value
                account.setPassword(generatePassword());
                break;
            }
            case UI.DATE: {
                // Load the users Date of Birth preferences
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                long minAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_min_key), "")) * DateUtils.YEAR_IN_MILLIS;
                long maxAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_max_key), "")) * DateUtils.YEAR_IN_MILLIS;
                // Get current time in milliseconds
                long currentMillis = System.currentTimeMillis();
                Calendar dob = Calendar.getInstance();
                // Set the date of birth to be random within the age bounds
                dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(currentMillis - maxAge, currentMillis - minAge));
                // Update the Forge Account
                account.setDateOfBirth(dob);
                break;
            }
        }
        // Update the current stored account with the new values and return the account
        CurrentManager.updateCurrentAccount(account, context);
        return account;
    }

    /**
     * Generates a completely new Forge Account
     * @param networkAvailable if the network is available
     * @return the newly generated Forge Account
     */
    public ForgeAccount forgeAccount(boolean networkAvailable) {
        Random rand = new Random();
        // Create a blank account
        ForgeAccount account = new ForgeAccount();
        // Load user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        long minAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_min_key), "")) * DateUtils.YEAR_IN_MILLIS;
        long maxAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_max_key), "")) * DateUtils.YEAR_IN_MILLIS;
        if (networkAvailable) {
            // If there is internet access, load an email from the Mail API
            mailComs.getAddress();
        } else {
            // Otherwise, generate an email locally
            account.setEmail(generateEmail());
        }
        // Get current time
        long currentMillis = System.currentTimeMillis();
        Calendar dob = Calendar.getInstance();
        // Set the date of birth to be random within the age bounds
        dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(currentMillis - maxAge, currentMillis - minAge));
        account.setDateOfBirth(dob);
        // Set the first name to be a random value in the list
        account.setFirstName(names.get(rand.nextInt(names.size())));
        // Set the middle name to be a random value in the list
        account.setMiddleName(names.get(rand.nextInt(names.size())));
        // Set the last name to be a random value in the list
        account.setLastName(names.get(rand.nextInt(names.size())));
        // Set the user name, removing any special characters from the first to last names
        account.setUsername(stripSpecialCharacters(generateUsername(account)));
        // Set the password to be a random string of characters
        account.setPassword(generatePassword());
        // Update the currently loaded account and return the newly generated account
        CurrentManager.updateCurrentAccount(account, context);
        return account;
    }

    /**
     * Gets more information about a specific email
     *
     * @param email the email to load more information about
     */
    public void fetchEmail(EmailMessage email) {
        mailComs.getEmail(email);
    }

    /**
     * Loads the name list to choose from, from a CSV file to RAM
     */
    private void loadNames() {
        try {
            // Open an input stream to the CSV file
            InputStreamReader is = new InputStreamReader(context.getResources().openRawResource(R.raw.most_common_names));
            BufferedReader reader = new BufferedReader(is);
            names = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                // Iterate through each line in the CSV and add the name to the names list
                names.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses Mail Communicator to refresh the emails received in the Mail API
     * @param emailAddress the email address
     * @param latestMessage the last email received by the app from the API
     */
    public void refreshEmails(EmailAddress emailAddress, EmailMessage latestMessage) {
        mailComs.getEmails(emailAddress, latestMessage);
    }

    /**
     * Uses Mail Communicator to set an email to the one provided
     * @param emailAddress
     */
    public void setEmailAddress(EmailAddress emailAddress) {
        mailComs.setEmail(emailAddress.getAddress());
    }

    /**
     * Generates a user name from the first, middle and last names alongside a random number
     * @param account the account to generate the userame for
     * @return the generated username
     */
    private String generateUsername(ForgeAccount account) {
        return account.getFirstName() + account.getMiddleName().substring(0, 1) + account.getLastName() + RandomStringUtils.randomNumeric(3);
    }

    /**
     * Generates a random password, adhering to the user's password preferences
     * @return the random password
     */
    private String generatePassword() {
        Random rand = new Random();
        // Get shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int maxLen = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_password_max_key), ""));
        int minLen = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_password_min_key), ""));
        // If special characters can be used
        boolean specialChars = sharedPref.getBoolean(context.getString(R.string.pref_password_special_key), true);
        // If uppercase characters can be used
        boolean uppercaseChars = sharedPref.getBoolean(context.getString(R.string.pref_password_uppercase_key), true);
        // If numbers can be used
        boolean numberChars = sharedPref.getBoolean(context.getString(R.string.pref_password_number_key), true);
        // Set character set to include lower case characters
        String characterSet = General.LOWERCASE_CHARACTERS;
        if (specialChars) {
            characterSet += General.SPECIAL_CHARACTERS;
        }
        if (uppercaseChars) {
            characterSet += General.UPPERCASE_CHARACTERS;
        }
        if (numberChars) {
            characterSet += General.NUMBER_CHARACTERS;
        }
        // Return the randomly generated password string
        return RandomStringUtils.random(rand.nextInt((maxLen - minLen) + 1) + minLen, characterSet);
    }

    /**
     * Locally generate a random email
     * @return the locally generated email
     */
    private EmailAddress generateEmail() {
        return new EmailAddress(String.format("%s@%s", RandomStringUtils.randomAlphanumeric(8), General.MAIL_DOMAIN),
                null);
    }

    /**
     * Removes any special characters from a string
     * @param input the original string to remove the special characters from
     * @return the new string with special characters removed
     */
    private String stripSpecialCharacters(String input) {
        // Set up regex pattern for alphanumeric numbers
        Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match = pt.matcher(input);
        while (match.find()) {
            // For each special character match, remove its
            String s = match.group();
            input = input.replaceAll("\\\\" + s, "");
        }
        return input;
    }
}
