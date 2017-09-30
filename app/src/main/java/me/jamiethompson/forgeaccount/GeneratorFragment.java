package me.jamiethompson.forgeaccount;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import android.os.Handler;

import com.google.gson.Gson;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jamie on 27/09/17.
 */

public class GeneratorFragment extends Fragment implements View.OnClickListener, EmailInterface, ListView.OnItemClickListener, LoadInterface
{
	final private Handler mailPollHandler = new Handler();
	private ForgeAccount mAccount;
	private List<String> mNames;
	private View mView;
	private MailCommunicator mMailComs;
	private ProgressBar mAddressProgress;
	private ProgressBar mMailProgress;
	private TextInputLayout mEmailWrapper;
	private TextView mEmailEntry;
	private ListView mEmailList;
	private EditText mAccountNameEntry;
	private List<EmailMessage> emailMessages;
	private boolean mLoaded = false;

	public GeneratorFragment()
	{
	}

	public static GeneratorFragment newInstance()
	{
		GeneratorFragment fragment = new GeneratorFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_generator, container, false);
		this.mView = view;
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		emailMessages = new ArrayList<>();
		setUpInputs();
		loadNames();
		refresh();
	}

	@Override
	public void onDetach()
	{
		mView = null;
		super.onDetach();
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.save:
			{
				save();
				break;
			}
			case R.id.refresh:
			{
				refresh();
				break;
			}
			case R.id.refresh_firstname:
			{
				refreshItem(Constants.FIRSTNAME);
				break;
			}
			case R.id.refresh_middlename:
			{
				refreshItem(Constants.MIDDLENAME);
				break;
			}
			case R.id.refresh_lastname:
			{
				refreshItem(Constants.LASTNAME);
				break;
			}
			case R.id.refresh_username:
			{
				refreshItem(Constants.USERNAME);
				break;
			}
			case R.id.refresh_email:
			{
				refreshItem(Constants.EMAIL);
				break;
			}
			case R.id.refresh_password:
			{
				refreshItem(Constants.PASSWORD);
				break;
			}
			case R.id.refresh_date:
			{
				refreshItem(Constants.DATE);
				break;
			}
			case R.id.copy_firstname:
			{
				String nameTag = getString(R.string.firstname);
				addToClipboard(nameTag, mAccount.getFirstName());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.copy_middlename:
			{
				String nameTag = getString(R.string.middlename);
				addToClipboard(nameTag, mAccount.getMiddleName());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.copy_lastname:
			{
				String nameTag = getString(R.string.lastname);
				addToClipboard(nameTag, mAccount.getLastName());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.copy_username:
			{
				String nameTag = getString(R.string.username);
				addToClipboard(nameTag, mAccount.getUsername());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.copy_email:
			{
				String nameTag = getString(R.string.email);
				addToClipboard(nameTag, mAccount.getEmail().getAddress());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.copy_password:
			{
				String nameTag = getString(R.string.password);
				addToClipboard(nameTag, mAccount.getPassword());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.copy_date:
			{
				String nameTag = getString(R.string.date);
				Calendar dob = mAccount.getDateOfBirth();
				addToClipboard(nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DAY_OF_MONTH)));
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	{
		((CheckBox) view.findViewById(R.id.read)).setChecked(false);
		emailMessages.get(i).setRead(true);
		EmailMessage email = emailMessages.get(i);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		builder.setView(inflater.inflate(R.layout.dialog_email, null))
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						dialogInterface.dismiss();
					}
				});
		Dialog dialog = builder.create();
		dialog.show();
		((TextView) dialog.findViewById(R.id.subject)).setText(email.getSubject());
		((TextView) dialog.findViewById(R.id.from)).setText(email.getFrom());
		((TextView) dialog.findViewById(R.id.time)).setText(email.getTime());
		((TextView) dialog.findViewById(R.id.body)).setText(fromHtml(fromHtml(email.getBody()).toString()));
		((TextView) dialog.findViewById(R.id.body)).setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void loadAddress(final EmailAddress emailAddress)
	{
		if (emailAddress != null)
		{
			Log.d("mega", emailAddress.getAddress());
			mailPollHandler.removeMessages(0);
			mAccount.setEmail(emailAddress);
			mEmailEntry.setText(emailAddress.getAddress());
			mEmailWrapper.setVisibility(View.VISIBLE);
			mAddressProgress.setVisibility(View.GONE);

			mMailProgress.setVisibility(View.VISIBLE);
			mMailComs.getEmails(emailAddress);

			final int delay = 10000; //milliseconds
			mailPollHandler.postDelayed(new Runnable()
			{
				public void run()
				{
					if (emailAddress != null)
					{
						mMailProgress.setVisibility(View.VISIBLE);
						mMailComs.getEmails(emailAddress);
						mailPollHandler.postDelayed(this, delay);
					}
				}
			}, delay);
		}
	}

	@Override
	public void loadEmails(List<EmailMessage> emails)
	{
		if (!emails.isEmpty())
		{
			mEmailList.setAdapter(null);
			this.emailMessages = emails;
		}
		mMailProgress.setVisibility(View.GONE);
		EmailListAdapter adapter = new EmailListAdapter(getActivity().getApplicationContext(), R.layout.item_email, this.emailMessages);
		mEmailList.setAdapter(adapter);
		setListViewHeightBasedOnChildren(mEmailList);
	}

	@Override
	public void load(ForgeAccount account)
	{
		mLoaded = true;
		mAccount = account;
		this.emailMessages = new ArrayList<>();
		mEmailList.setAdapter(null);
		setListViewHeightBasedOnChildren(mEmailList);
		mMailComs = new MailCommunicator(this, getContext());
		mMailComs.setEmail(account.getEmail().getAddress());
		mEmailWrapper.setVisibility(View.GONE);
		mAddressProgress.setVisibility(View.VISIBLE);
		loadAccount();
		Feedback.displayMessage(getString(R.string.message_account_loaded), mView);
	}

	private void setUpInputs()
	{
		((TextInputLayout) mView.findViewById(R.id.account_name_wrapper)).setHint(getString(R.string.account_name));
		((TextInputLayout) mView.findViewById(R.id.firstname_wrapper)).setHint(getString(R.string.firstname));
		((TextInputLayout) mView.findViewById(R.id.middlename_wrapper)).setHint(getString(R.string.middlename));
		((TextInputLayout) mView.findViewById(R.id.lastname_wrapper)).setHint(getString(R.string.account_name));
		((TextInputLayout) mView.findViewById(R.id.username_wrapper)).setHint(getString(R.string.username));
		mEmailWrapper = ((TextInputLayout) mView.findViewById(R.id.email_wrapper));
		mEmailWrapper.setHint(getString(R.string.email));
		((TextInputLayout) mView.findViewById(R.id.account_name_wrapper)).setHint(getString(R.string.account_name));
		((TextInputLayout) mView.findViewById(R.id.year_wrapper)).setHint(getString(R.string.year));
		((TextInputLayout) mView.findViewById(R.id.month_wrapper)).setHint(getString(R.string.month));
		((TextInputLayout) mView.findViewById(R.id.day_wrapper)).setHint(getString(R.string.day));

		// setting up click listeners
		// refresh
		mView.findViewById(R.id.refresh).setOnClickListener(this);
		mView.findViewById(R.id.refresh_firstname).setOnClickListener(this);
		mView.findViewById(R.id.refresh_middlename).setOnClickListener(this);
		mView.findViewById(R.id.refresh_lastname).setOnClickListener(this);
		mView.findViewById(R.id.refresh_username).setOnClickListener(this);
		mView.findViewById(R.id.refresh_email).setOnClickListener(this);
		mView.findViewById(R.id.refresh_password).setOnClickListener(this);
		mView.findViewById(R.id.refresh_date).setOnClickListener(this);
		// copy & paste
		mView.findViewById(R.id.copy_firstname).setOnClickListener(this);
		mView.findViewById(R.id.copy_middlename).setOnClickListener(this);
		mView.findViewById(R.id.copy_lastname).setOnClickListener(this);
		mView.findViewById(R.id.copy_username).setOnClickListener(this);
		mView.findViewById(R.id.copy_email).setOnClickListener(this);
		mView.findViewById(R.id.copy_password).setOnClickListener(this);
		mView.findViewById(R.id.copy_date).setOnClickListener(this);
		// save button
		mView.findViewById(R.id.save).setOnClickListener(this);
		// assign globals
		mAccountNameEntry = mView.findViewById(R.id.account_name);
		mEmailEntry = mView.findViewById(R.id.email);
		mAddressProgress = mView.findViewById(R.id.address_progress);
		mMailProgress = mView.findViewById(R.id.mail_progress);
		mEmailList = mView.findViewById(R.id.email_list);
		// listview
		mEmailList.setOnItemClickListener(this);
		mEmailList.setOnTouchListener(new View.OnTouchListener()
		{
			// Setting on Touch Listener for handling the touch inside ScrollView
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				// Disallow the touch request for parent scroll on touch of child view
				v.getParent().requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});
		setListViewHeightBasedOnChildren(mEmailList);
	}

	private void save()
	{
		mAccount.setAccountName(mAccountNameEntry.getText().toString());
		if(mLoaded)
		{
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
						case DialogInterface.BUTTON_POSITIVE:
						{
							ForgeAccount saveAccount = FileManager.replace(getActivity(), mAccount);
							if (saveAccount != null)
							{
								mAccount = saveAccount;
								mLoaded = true;
								Feedback.displayMessage(getString(R.string.message_account_saved), mView);
							}
							((Forge) getActivity()).reloadSaveList();
							dialog.dismiss();
							break;
						}
						case DialogInterface.BUTTON_NEGATIVE:
						{
							ForgeAccount saveAccount = FileManager.add(getActivity(), mAccount);
							if (saveAccount != null)
							{
								mAccount = saveAccount;
								mLoaded = true;
								Feedback.displayMessage(getString(R.string.message_account_saved), mView);
							}
							((Forge) getActivity()).reloadSaveList();
							dialog.dismiss();
							break;
						}
						case DialogInterface.BUTTON_NEUTRAL:
						{
							dialog.dismiss();
							break;
						}
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage(getString(R.string.dialog_overwrite))
					.setPositiveButton(getString(R.string.option_overwrite), dialogClickListener)
					.setNegativeButton(getString(R.string.option_save_new), dialogClickListener)
					.setNeutralButton(getString(R.string.option_cancel), dialogClickListener)
					.show();
		}
		else
		{
			ForgeAccount saveAccount = FileManager.add(getActivity(), mAccount);
			if (saveAccount != null)
			{
				Feedback.displayMessage(getString(R.string.message_account_saved), mView);
				mLoaded = true;
			}
		}
		((Forge) getActivity()).reloadSaveList();
		// External save
	}

	private void refresh()
	{
		mLoaded = false;
		this.emailMessages = new ArrayList<>();
		mEmailList.setAdapter(null);
		setListViewHeightBasedOnChildren(mEmailList);
		forgeAccount();
		mEmailWrapper.setVisibility(View.GONE);
		mAddressProgress.setVisibility(View.VISIBLE);
	}

	private void loadAccount()
	{
		((TextView) mView.findViewById(R.id.account_name)).setText(mAccount.getAccountName());
		((TextView) mView.findViewById(R.id.firstname)).setText(mAccount.getFirstName());
		((TextView) mView.findViewById(R.id.middlename)).setText(mAccount.getMiddleName());
		((TextView) mView.findViewById(R.id.lastname)).setText(mAccount.getLastName());
		((TextView) mView.findViewById(R.id.username)).setText(mAccount.getUsername());
		((TextView) mView.findViewById(R.id.password)).setText(mAccount.getPassword());
		Calendar dob = mAccount.getDateOfBirth();
		((TextView) mView.findViewById(R.id.day)).setText(String.valueOf(dob.get(Calendar.DAY_OF_MONTH)));
		((TextView) mView.findViewById(R.id.month)).setText(String.valueOf(dob.get(Calendar.MONTH)));
		((TextView) mView.findViewById(R.id.year)).setText(String.valueOf(dob.get(Calendar.YEAR)));
	}

	private void forgeAccount()
	{
		Random rand = new Random();
		mMailComs = new MailCommunicator(this, getContext());
		mMailComs.getAddress();
		long minAge = 18 * DateUtils.YEAR_IN_MILLIS;
		long currentMillis = System.currentTimeMillis();
		mAccount = new ForgeAccount();
		Calendar dob = Calendar.getInstance();
		dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(0, currentMillis - minAge));
		mAccount.setDateOfBirth(dob);
		mAccount.setFirstName(mNames.get(rand.nextInt(mNames.size())));
		mAccount.setMiddleName(mNames.get(rand.nextInt(mNames.size())));
		mAccount.setLastName(mNames.get(rand.nextInt(mNames.size())));
		mAccount.setUsername(stripSpecialCharacters(generateUsername()));
		mAccount.setPassword(generatePassword());
		loadAccount();
	}

	private void refreshItem(String item)
	{
		Random rand = new Random();
		switch (item)
		{
			case Constants.FIRSTNAME:
			{
				mAccount.setFirstName(mNames.get(rand.nextInt(mNames.size())));
				mAccount.setUsername(stripSpecialCharacters(generateUsername()));
				break;
			}
			case Constants.MIDDLENAME:
			{
				mAccount.setMiddleName(mNames.get(rand.nextInt(mNames.size())));
				mAccount.setUsername(stripSpecialCharacters(generateUsername()));
				break;
			}
			case Constants.LASTNAME:
			{
				mAccount.setLastName(mNames.get(rand.nextInt(mNames.size())));
				mAccount.setUsername(stripSpecialCharacters(generateUsername()));
				break;
			}
			case Constants.USERNAME:
			{
				mAccount.setUsername(stripSpecialCharacters(generateUsername()));
				break;
			}
			case Constants.EMAIL:
			{
				this.emailMessages = new ArrayList<>();
				mEmailList.setAdapter(null);
				setListViewHeightBasedOnChildren(mEmailList);
				mMailComs = new MailCommunicator(this, getContext());
				mMailComs.getAddress();
				mEmailWrapper.setVisibility(View.GONE);
				mAddressProgress.setVisibility(View.VISIBLE);
				break;
			}
			case Constants.PASSWORD:
			{
				mAccount.setPassword(generatePassword());
				break;
			}
			case Constants.DATE:
			{
				long minAge = 18 * DateUtils.YEAR_IN_MILLIS;
				long currentMillis = System.currentTimeMillis();
				Calendar dob = Calendar.getInstance();
				dob.setTimeInMillis(ThreadLocalRandom.current().nextLong(0, currentMillis - minAge));
				mAccount.setDateOfBirth(dob);
				break;
			}
		}
		loadAccount();
	}

	private void loadNames()
	{
		try
		{
			InputStreamReader is = new InputStreamReader(getActivity().getApplicationContext().getResources().openRawResource(R.raw.most_common_names));
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

	private String generateUsername()
	{
		return mAccount.getFirstName() + mAccount.getMiddleName().substring(0, 1) + mAccount.getLastName() + RandomStringUtils.randomNumeric(3);
	}

	private String generatePassword()
	{
		return RandomStringUtils.randomAlphanumeric(8) + RandomStringUtils.randomNumeric(2);
	}

	public static int randBetween(int start, int end)
	{
		return start + (int) Math.round(Math.random() * (end - start));
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

	private void addToClipboard(String label, String content)
	{
		ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, content);
		clipboard.setPrimaryClip(clip);
	}

	/**
	 * Taken from Stack Overflow - https://stackoverflow.com/a/26501296
	 *
	 * @param listView
	 */

	public static void setListViewHeightBasedOnChildren(ListView listView)
	{
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
		{
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++)
		{
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	/**
	 * Taken from Stack Overflow - https://stackoverflow.com/a/37905107
	 *
	 * @param html
	 * @return
	 */

	@SuppressWarnings("deprecation")
	public static Spanned fromHtml(String html)
	{
		Spanned result;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
		{
			result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
		}
		else
		{
			result = Html.fromHtml(html);
		}
		return result;
	}

}
