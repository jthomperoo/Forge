package me.jamiethompson.forgeaccount.Services;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;

import me.jamiethompson.forgeaccount.Constants;
import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.Files.CurrentManager;
import me.jamiethompson.forgeaccount.Files.FileManager;
import me.jamiethompson.forgeaccount.Generator.ForgeGenerator;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.TabActivity.Forge;

/**
 * Created by jamie on 05/10/17.
 */

public class OverlayService extends Service implements View.OnClickListener
{

	private WindowManager windowManager;
	private View overlayView;
	private ForgeAccount account;

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		account = CurrentManager.loadCurrentAccount(getApplicationContext());
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		overlayView = layoutInflater.inflate(R.layout.dialog_manual_copy, null);

		setUpEditTexts();
		setUpButtons();

		WindowManager.LayoutParams params;

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					PixelFormat.TRANSPARENT);
		}
		else
		{
			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.TYPE_PHONE,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					PixelFormat.TRANSPARENT);
		}

		params.gravity = Gravity.CENTER;

		windowManager.addView(overlayView, params);
	}

	private void setUpButtons()
	{
		// assign to variables
		ImageButton firstname = overlayView.findViewById(R.id.copy_firstname);
		ImageButton middlename = overlayView.findViewById(R.id.copy_middlename);
		ImageButton lastname = overlayView.findViewById(R.id.copy_lastname);
		ImageButton username = overlayView.findViewById(R.id.copy_username);
		ImageButton email = overlayView.findViewById(R.id.copy_email);
		ImageButton password = overlayView.findViewById(R.id.copy_password);
		ImageButton date = overlayView.findViewById(R.id.copy_date);

		// listeners
		overlayView.findViewById(R.id.button_dismiss).setOnClickListener(this);
		overlayView.findViewById(R.id.button_generate).setOnClickListener(this);
		overlayView.findViewById(R.id.button_save).setOnClickListener(this);
		overlayView.findViewById(R.id.background).setOnClickListener(this);
		firstname.setOnClickListener(this);
		middlename.setOnClickListener(this);
		lastname.setOnClickListener(this);
		username.setOnClickListener(this);
		email.setOnClickListener(this);
		password.setOnClickListener(this);
		date.setOnClickListener(this);

		// set up images
		Drawable copyIcon = getDrawable(R.drawable.icon_copy);
		firstname.setImageDrawable(copyIcon);
		middlename.setImageDrawable(copyIcon);
		lastname.setImageDrawable(copyIcon);
		username.setImageDrawable(copyIcon);
		email.setImageDrawable(copyIcon);
		password.setImageDrawable(copyIcon);
		date.setImageDrawable(copyIcon);
	}

	private void setUpEditTexts()
	{
		((EditText) overlayView.findViewById(R.id.firstname)).setText(account.getFirstName());
		((EditText) overlayView.findViewById(R.id.middlename)).setText(account.getMiddleName());
		((EditText) overlayView.findViewById(R.id.lastname)).setText(account.getLastName());
		((EditText) overlayView.findViewById(R.id.username)).setText(account.getUsername());
		((EditText) overlayView.findViewById(R.id.email)).setText(account.getEmail().getAddress());
		((EditText) overlayView.findViewById(R.id.password)).setText(account.getPassword());
		((EditText) overlayView.findViewById(R.id.day)).setText(String.valueOf(account.getDateOfBirth().get(Calendar.DAY_OF_MONTH)));
		((EditText) overlayView.findViewById(R.id.month)).setText(String.valueOf(account.getDateOfBirth().get(Calendar.MONTH) + 1));
		((EditText) overlayView.findViewById(R.id.year)).setText(String.valueOf(account.getDateOfBirth().get(Calendar.YEAR)));
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (overlayView != null)
		{
			windowManager.removeView(overlayView);
		}
	}

	@Override
	public void onClick(View view)
	{
		boolean finish = false;
		switch (view.getId())
		{
			case R.id.copy_firstname:
			{
				String nameTag = getString(R.string.firstname);
				addToClipboard(nameTag, account.getFirstName());
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.copy_middlename:
			{
				String nameTag = getString(R.string.middlename);
				addToClipboard(nameTag, account.getMiddleName());
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.copy_lastname:
			{
				String nameTag = getString(R.string.lastname);
				addToClipboard(nameTag, account.getLastName());
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.copy_username:
			{
				String nameTag = getString(R.string.username);
				addToClipboard(nameTag, account.getUsername());
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.copy_email:
			{
				String nameTag = getString(R.string.email);
				addToClipboard(nameTag, account.getEmail().getAddress());
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.copy_password:
			{
				String nameTag = getString(R.string.password);
				addToClipboard(nameTag, account.getPassword());
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.copy_date:
			{
				String nameTag = getString(R.string.date);
				Calendar dob = account.getDateOfBirth();
				addToClipboard(nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH) + 1, dob.get(Calendar.DAY_OF_MONTH)));
				displayToast(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)));
				finish = true;
				break;
			}
			case R.id.button_save:
			{
				account.setAccountName(Calendar.getInstance().getTime().toString());
				FileManager.add(getApplicationContext(), account);
				displayToast(getString(R.string.message_account_saved));
				break;
			}
			case R.id.button_generate:
			{
				Intent generateIntent = new Intent(this, Forge.class);
				generateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				generateIntent.putExtra(Constants.NOTIFICATION_NAVIGATION, Constants.GENERATE_TAB);
				startActivity(generateIntent);
				finish = true;
				break;
			}
			case R.id.background:
			{
				finish = true;
				break;
			}
		}
		if(finish)
		{
			this.stopSelf();
		}
	}

	private void displayToast(String message)
	{
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	private void addToClipboard(String label, String content)
	{
		ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, content);
		clipboard.setPrimaryClip(clip);
	}
}
