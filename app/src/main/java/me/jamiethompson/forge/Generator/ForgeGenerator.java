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
 */

public class ForgeGenerator {
    private List<String> names;
    private MailCommunicator mailComs;
    private Context context;

    public ForgeGenerator(EmailInterface callback, Context context) {
        this.context = context;
        mailComs = new MailCommunicator(callback, context);
        loadNames();
    }

    public ForgeAccount refreshItem(ForgeAccount account, String item, boolean networkAvailable) {
        Random rand = new Random();
        switch (item) {
            case UI.FIRSTNAME: {
                account.setFirstName(names.get(rand.nextInt(names.size())));
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.MIDDLENAME: {
                account.setMiddleName(names.get(rand.nextInt(names.size())));
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.LASTNAME: {
                account.setLastName(names.get(rand.nextInt(names.size())));
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.USERNAME: {
                account.setUsername(stripSpecialCharacters(generateUsername(account)));
                break;
            }
            case UI.EMAIL: {
                if (networkAvailable) {
                    mailComs.getAddress();
                } else {
                    account.setEmail(generateEmail());
                }
                break;
            }
            case UI.PASSWORD: {
                account.setPassword(generatePassword());
                break;
            }
            case UI.DATE: {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                long minAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_min_key), "")) * DateUtils.YEAR_IN_MILLIS;
                long maxAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_max_key), "")) * DateUtils.YEAR_IN_MILLIS;
                long currentMillis = System.currentTimeMillis();
                Calendar dob = Calendar.getInstance();
                dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(0 - maxAge, currentMillis - minAge));
                account.setDateOfBirth(dob);
                break;
            }
        }
        CurrentManager.updateCurrentAccount(account, context);
        return account;
    }


    public ForgeAccount forgeAccount(boolean networkAvailable) {
        Random rand = new Random();
        ForgeAccount account = new ForgeAccount();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        long minAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_min_key), "")) * DateUtils.YEAR_IN_MILLIS;
        long maxAge = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_dob_max_key), "")) * DateUtils.YEAR_IN_MILLIS;
        if (networkAvailable) {
            mailComs.getAddress();
        } else {
            account.setEmail(generateEmail());
        }
        long currentMillis = System.currentTimeMillis();
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(0 - maxAge, currentMillis - minAge));
        account.setDateOfBirth(dob);
        account.setFirstName(names.get(rand.nextInt(names.size())));
        account.setMiddleName(names.get(rand.nextInt(names.size())));
        account.setLastName(names.get(rand.nextInt(names.size())));
        account.setUsername(stripSpecialCharacters(generateUsername(account)));
        account.setPassword(generatePassword());
        CurrentManager.updateCurrentAccount(account, context);
        return account;
    }

    private void loadNames() {
        try {
            InputStreamReader is = new InputStreamReader(context.getResources().openRawResource(R.raw.most_common_names));
            BufferedReader reader = new BufferedReader(is);
            names = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                names.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshEmails(EmailAddress emailAddress, EmailMessage latestMessage) {
        mailComs.getEmails(emailAddress, latestMessage);
    }

    public void setEmailAddress(EmailAddress emailAddress) {
        mailComs.setEmail(emailAddress.getAddress());
    }

    private String generateUsername(ForgeAccount account) {
        return account.getFirstName() + account.getMiddleName().substring(0, 1) + account.getLastName() + RandomStringUtils.randomNumeric(3);
    }

    private String generatePassword() {
        Random rand = new Random();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int maxLen = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_password_max_key), ""));
        int minLen = Integer.valueOf(sharedPref.getString(context.getString(R.string.pref_password_min_key), ""));
        boolean specialChars = sharedPref.getBoolean(context.getString(R.string.pref_password_special_key), true);
        boolean uppercaseChars = sharedPref.getBoolean(context.getString(R.string.pref_password_uppercase_key), true);
        boolean numberChars = sharedPref.getBoolean(context.getString(R.string.pref_password_number_key), true);
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
        return RandomStringUtils.random(rand.nextInt((maxLen - minLen) + 1) + minLen, characterSet);
    }

    private EmailAddress generateEmail() {
        return new EmailAddress(String.format("%s@%s", RandomStringUtils.randomAlphanumeric(8), General.MAIL_DOMAIN),
                null);
    }

    private String stripSpecialCharacters(String input) {
        Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match = pt.matcher(input);
        while (match.find()) {
            String s = match.group();
            input = input.replaceAll("\\" + s, "");
        }
        return input;
    }
}
