package me.jamiethompson.forgeaccount;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamie on 29/09/17.
 */

public class FileManager
{
	public static List<ForgeAccount> load(Activity activity)
	{
		List<ForgeAccount> accounts = new ArrayList<>();

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
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}

				Gson gson = new Gson();
				accounts = gson.fromJson(stringBuilder.toString(), new TypeToken<List<ForgeAccount>>()
				{
				}.getType());
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
		List<ForgeAccount> accounts = FileManager.load(activity);
		account.setPosition(accounts.size());
		accounts.add(account);
		String jsonString = gson.toJson(accounts);
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
		List<ForgeAccount> accounts = FileManager.load(activity);
		accounts.set(account.getPosition(),account);
		String jsonString = gson.toJson(accounts);
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

	public static boolean delete(Activity activity, int position)
	{
		Gson gson = new Gson();
		List<ForgeAccount> accounts = FileManager.load(activity);
		accounts.remove(position);
		String jsonString = gson.toJson(accounts);
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
