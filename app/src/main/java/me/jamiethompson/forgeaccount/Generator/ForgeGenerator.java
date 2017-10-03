package me.jamiethompson.forgeaccount.Generator;

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

import me.jamiethompson.forgeaccount.Constants;
import me.jamiethompson.forgeaccount.Data.EmailAddress;
import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.EmailInterface;
import me.jamiethompson.forgeaccount.Files.CurrentManager;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.Web.MailCommunicator;

/**
 * Created by jamie on 02/10/17.
 */

public class ForgeGenerator
{
	private List<String> mNames;
	private MailCommunicator mMailComs;
	private Context mContext;

	public ForgeGenerator(EmailInterface callback, Context context)
	{
		mContext = context;
		mMailComs = new MailCommunicator(callback, context);
		loadNames();
	}

	public ForgeAccount refreshItem(ForgeAccount account, String item, boolean networkAvailable)
	{
		Random rand = new Random();
		switch (item)
		{
			case Constants.FIRSTNAME:
			{
				account.setFirstName(mNames.get(rand.nextInt(mNames.size())));
				account.setUsername(stripSpecialCharacters(generateUsername(account)));
				break;
			}
			case Constants.MIDDLENAME:
			{
				account.setMiddleName(mNames.get(rand.nextInt(mNames.size())));
				account.setUsername(stripSpecialCharacters(generateUsername(account)));
				break;
			}
			case Constants.LASTNAME:
			{
				account.setLastName(mNames.get(rand.nextInt(mNames.size())));
				account.setUsername(stripSpecialCharacters(generateUsername(account)));
				break;
			}
			case Constants.USERNAME:
			{
				account.setUsername(stripSpecialCharacters(generateUsername(account)));
				break;
			}
			case Constants.EMAIL:
			{
				if(networkAvailable)
				{
					mMailComs.getAddress();
				}
				else
				{
					account.setEmail(generateEmail());
				}
				break;
			}
			case Constants.PASSWORD:
			{
				account.setPassword(generatePassword());
				break;
			}
			case Constants.DATE:
			{
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
				long minAge = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.pref_dob_min_key), "")) * DateUtils.YEAR_IN_MILLIS;
				long maxAge = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.pref_dob_max_key), "")) * DateUtils.YEAR_IN_MILLIS;
				long currentMillis = System.currentTimeMillis();
				Calendar dob = Calendar.getInstance();
				dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(0 - maxAge, currentMillis - minAge));
				account.setDateOfBirth(dob);
				break;
			}
		}
		CurrentManager.updateCurrentAccount(account, mContext);
		return account;
	}


	public ForgeAccount forgeAccount(boolean networkAvailable)
	{
		Random rand = new Random();
		ForgeAccount account = new ForgeAccount();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
		long minAge = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.pref_dob_min_key), "")) * DateUtils.YEAR_IN_MILLIS;
		long maxAge = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.pref_dob_max_key), "")) * DateUtils.YEAR_IN_MILLIS;
		if(networkAvailable)
		{
			mMailComs.getAddress();
		}
		else
		{
			account.setEmail(generateEmail());
		}
		long currentMillis = System.currentTimeMillis();
		Calendar dob = Calendar.getInstance();
		dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(0 - maxAge, currentMillis - minAge));
		account.setDateOfBirth(dob);
		account.setFirstName(mNames.get(rand.nextInt(mNames.size())));
		account.setMiddleName(mNames.get(rand.nextInt(mNames.size())));
		account.setLastName(mNames.get(rand.nextInt(mNames.size())));
		account.setUsername(stripSpecialCharacters(generateUsername(account)));
		account.setPassword(generatePassword());
		CurrentManager.updateCurrentAccount(account, mContext);
		return account;
	}

	private void loadNames()
	{
		try
		{
			InputStreamReader is = new InputStreamReader(mContext.getResources().openRawResource(R.raw.most_common_names));
			BufferedReader reader = new BufferedReader(is);
			mNames = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null)
			{
				mNames.add(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void refreshEmails(EmailAddress emailAddress)
	{
		mMailComs.getEmails(emailAddress);
	}

	public void setEmailAddress(EmailAddress emailAddress)
	{
		mMailComs.setEmail(emailAddress.getAddress());
	}

	private String generateUsername(ForgeAccount account)
	{
		return account.getFirstName() + account.getMiddleName().substring(0, 1) + account.getLastName() + RandomStringUtils.randomNumeric(3);
	}

	private String generatePassword()
	{
		Random rand = new Random();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
		int maxLen = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.pref_password_max_key), ""));
		int minLen = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.pref_password_min_key), ""));
		boolean specialChars = sharedPref.getBoolean(mContext.getString(R.string.pref_password_special_key), true);
		boolean uppercaseChars = sharedPref.getBoolean(mContext.getString(R.string.pref_password_uppercase_key), true);
		boolean numberChars = sharedPref.getBoolean(mContext.getString(R.string.pref_password_number_key), true);
		String characterSet = Constants.LOWERCASE_CHARACTERS;
		if(specialChars)
		{
			characterSet += Constants.SPECIAL_CHARACTERS;
		}
		if(uppercaseChars)
		{
			characterSet += Constants.UPPERCASE_CHARACTERS;
		}
		if(numberChars)
		{
			characterSet += Constants.NUMBER_CHARACTERS;
		}
		return RandomStringUtils.random(rand.nextInt((maxLen - minLen) + 1) + minLen,characterSet);
	}

	private EmailAddress generateEmail()
	{
		return new EmailAddress(String.format("%s@%s", RandomStringUtils.randomAlphanumeric(8), Constants.MAIL_DOMAIN),
				null);
	}

	private String stripSpecialCharacters(String input)
	{
		Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
		Matcher match = pt.matcher(input);
		while (match.find())
		{
			String s = match.group();
			input = input.replaceAll("\\" + s, "");
		}
		return input;
	}
}
