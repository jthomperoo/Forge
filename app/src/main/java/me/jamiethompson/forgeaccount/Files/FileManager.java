package me.jamiethompson.forgeaccount.Files;

import android.app.Activity;
import android.content.Context;

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

import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.R;

/**
 * Created by jamie on 29/09/17.
 */

public class FileManager
{
	public static HashMap<UUID, ForgeAccount> load(Activity activity)
	{
		HashMap<UUID, ForgeAccount> accounts = new HashMap<>();

		if (activity.getFileStreamPath(activity.getString(R.string.filename_forge_accounts)).exists())
		{
			FileInputStream inputStream;
			try
			{
				inputStream = activity.openFileInput(activity.getString(R.string.filename_forge_accounts));

				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null)
				{
					stringBuilder.append(line);
				}

				Gson gson = new Gson();
				List<ForgeAccount> accountList =
						gson.fromJson(stringBuilder.toString(), new TypeToken<List<ForgeAccount>>()
				{
				}.getType());

				for(ForgeAccount account : accountList)
				{
					accounts.put(account.getId(), account);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return accounts;
	}

	public static ForgeAccount add(Activity activity, ForgeAccount account)
	{
		Gson gson = new Gson();
		HashMap<UUID, ForgeAccount> accounts = FileManager.load(activity);
		account.setId(java.util.UUID.randomUUID());
		accounts.put(account.getId(),account);
		String jsonString = gson.toJson(new ArrayList<>(accounts.values()));
		// Internal save
		FileOutputStream outputStream;
		try
		{
			outputStream = activity.openFileOutput(activity.getString(R.string.filename_forge_accounts), Context.MODE_PRIVATE);
			outputStream.write(jsonString.getBytes());
			outputStream.close();
			return account;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static ForgeAccount replace(Activity activity, ForgeAccount account)
	{
		Gson gson = new Gson();
		HashMap<UUID, ForgeAccount> accounts = FileManager.load(activity);
		accounts.put(account.getId(), account);
		String jsonString = gson.toJson(new ArrayList<>(accounts.values()));
		// Internal save
		FileOutputStream outputStream;
		try
		{
			outputStream = activity.openFileOutput(activity.getString(R.string.filename_forge_accounts), Context.MODE_PRIVATE);
			outputStream.write(jsonString.getBytes());
			outputStream.close();
			return account;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static boolean delete(Activity activity, ForgeAccount account)
	{
		Gson gson = new Gson();
		HashMap<UUID, ForgeAccount> accounts = FileManager.load(activity);
		accounts.remove(account.getId());
		String jsonString = gson.toJson(new ArrayList<>(accounts.values()));
		// Internal save
		FileOutputStream outputStream;
		try
		{
			outputStream = activity.openFileOutput(activity.getString(R.string.filename_forge_accounts), Context.MODE_PRIVATE);
			outputStream.write(jsonString.getBytes());
			outputStream.close();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
