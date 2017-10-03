package me.jamiethompson.forgeaccount.TabActivity;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Handler;

import me.jamiethompson.forgeaccount.Constants;
import me.jamiethompson.forgeaccount.Data.EmailAddress;
import me.jamiethompson.forgeaccount.Data.EmailMessage;
import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.EmailInterface;
import me.jamiethompson.forgeaccount.Files.CurrentManager;
import me.jamiethompson.forgeaccount.Generator.ForgeGenerator;
import me.jamiethompson.forgeaccount.Preferences.DateOfBirthPreferenceFragment;
import me.jamiethompson.forgeaccount.Preferences.GeneralPreferenceFragment;
import me.jamiethompson.forgeaccount.Preferences.PasswordPreferenceFragment;
import me.jamiethompson.forgeaccount.Preferences.Preferences;
import me.jamiethompson.forgeaccount.UI.Feedback;
import me.jamiethompson.forgeaccount.Files.FileManager;
import me.jamiethompson.forgeaccount.ListView.EmailListAdapter;
import me.jamiethompson.forgeaccount.LoadInterface;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.UI.SaveDialogListener;

/**
 * Created by jamie on 27/09/17.
 */

public class GeneratorFragment extends Fragment implements View.OnClickListener, EmailInterface, ListView.OnItemClickListener, LoadInterface
{
	final private Handler mailPollHandler = new Handler();
	private Snackbar noInternetMessage;
	private Snackbar connectingMessage;
	private ForgeGenerator mGenerator;
	private ForgeAccount mAccount;
	private View mView;
	private ProgressBar mAddressProgress;
	private ProgressBar mMailProgress;
	private TextInputLayout mEmailWrapper;
	private TextView mEmailEntry;
	private ListView mEmailList;
	private EditText mAccountNameEntry;
	private List<EmailMessage> emailMessages;
	private boolean mLoaded = false;

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
		setUpGlobals();
		setUpUserInterface();
		displayAccount();
		ForgeAccount currentAccount = CurrentManager.loadCurrentAccount(getContext());
		if (currentAccount != null)
		{
			if (currentAccount.getEmail() != null)
			{
				load(currentAccount);
			}
		}
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
		if (isNetworkAvailable())
		{
			toggleNoInternetMessage(false);
		}
		else
		{
			toggleNoInternetMessage(true);
		}
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
				mAccount = mGenerator.refreshItem(mAccount, Constants.FIRSTNAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_middlename:
			{
				mAccount = mGenerator.refreshItem(mAccount, Constants.MIDDLENAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_lastname:
			{
				mAccount = mGenerator.refreshItem(mAccount, Constants.LASTNAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_username:
			{
				mAccount = mGenerator.refreshItem(mAccount, Constants.USERNAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_email:
			{
				mAccount = mGenerator.refreshItem(mAccount, Constants.EMAIL, isNetworkAvailable());
				this.emailMessages = new ArrayList<>();
				mEmailList.setAdapter(null);
				setListViewHeightBasedOnChildren(mEmailList);
				if (isNetworkAvailable())
				{
					showAddressProgress();
				}
				break;
			}
			case R.id.refresh_password:
			{
				mGenerator.refreshItem(mAccount, Constants.PASSWORD, isNetworkAvailable());
				break;
			}
			case R.id.refresh_date:
			{
				mGenerator.refreshItem(mAccount, Constants.DATE, isNetworkAvailable());
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
				addToClipboard(nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH) + 1, dob.get(Calendar.DAY_OF_MONTH)));
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), mView);
				break;
			}
			case R.id.preferences_email:
			{
				Intent intent = new Intent(getActivity(), Preferences.class );
				intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName() );
				intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
				startActivity(intent);
				break;
			}
			case R.id.preferences_password:
			{
				Intent intent = new Intent(getActivity(), Preferences.class );
				intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, PasswordPreferenceFragment.class.getName() );
				intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
				startActivity(intent);
				break;
			}
			case R.id.preferences_date:
			{
				Intent intent = new Intent(getActivity(), Preferences.class );
				intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, DateOfBirthPreferenceFragment.class.getName() );
				intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
				startActivity(intent);
				break;
			}
		}
		CurrentManager.updateCurrentAccount(mAccount, getContext());
		displayAccount();
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
				.setPositiveButton(getString(R.string.dialog_action_dismiss), new DialogInterface.OnClickListener()
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
		((TextView) dialog.findViewById(R.id.body)).setText(linkifyHtml(email.getBody(), Linkify.WEB_URLS));
		((TextView) dialog.findViewById(R.id.body)).setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void loadAddress(final EmailAddress emailAddress)
	{
		if (emailAddress != null)
		{
			mailPollHandler.removeMessages(0);
			mAccount.setEmail(emailAddress);
			mEmailEntry.setText(emailAddress.getAddress());

			hideAddressProgress();
			showEmailsProgress();

			if (isNetworkAvailable())
			{
				mGenerator.refreshEmails(emailAddress);
			}
			else
			{
				toggleNoInternetMessage(true);
			}
			CurrentManager.updateCurrentAccount(mAccount, getContext());

			mailPollHandler.postDelayed(new Runnable()
			{
				public void run()
				{
					if (emailAddress != null)
					{
						showEmailsProgress();
						if (isNetworkAvailable())
						{
							mGenerator.refreshEmails(emailAddress);
						}
						else
						{
							toggleNoInternetMessage(true);
						}
						mailPollHandler.postDelayed(this, Constants.EMAIL_REFRESH_DELAY);
					}
				}
			}, Constants.EMAIL_REFRESH_DELAY);
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
		hideEmailsProgress();
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

		if (isNetworkAvailable())
		{
			mGenerator.setEmailAddress(account.getEmail());
			showAddressProgress();
		}
		CurrentManager.updateCurrentAccount(mAccount, getContext());
		displayAccount();
		Feedback.displayMessage(getString(R.string.message_account_loaded), mView);
	}


	private void save()
	{
		mAccount.setAccountName(mAccountNameEntry.getText().toString());
		if (mLoaded)
		{
			DialogInterface.OnClickListener dialogClickListener = new SaveDialogListener(mAccount, mLoaded, getActivity(), mView);
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
		if (isNetworkAvailable())
		{
			toggleNoInternetMessage(false);
			showAddressProgress();
		}
		else
		{
			toggleNoInternetMessage(true);
		}
		mLoaded = false;
		this.emailMessages = new ArrayList<>();
		mEmailList.setAdapter(null);
		setListViewHeightBasedOnChildren(mEmailList);
		mAccount = mGenerator.forgeAccount(isNetworkAvailable());
		displayAccount();
	}

	private void reload()
	{
		if (isNetworkAvailable())
		{
			toggleNoInternetMessage(false);
			mGenerator.setEmailAddress(mAccount.getEmail());
			showAddressProgress();
		}
		else
		{
			toggleNoInternetMessage(true);
		}
	}

	private void setUpGlobals()
	{
		emailMessages = new ArrayList<>();
		mGenerator = new ForgeGenerator(this, getContext());
		mLoaded = false;
		emailMessages = new ArrayList<>();
		mAccount = mGenerator.forgeAccount(isNetworkAvailable());
	}

	private void setUpUserInterface()
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
		// settings
		mView.findViewById(R.id.preferences_email).setOnClickListener(this);
		mView.findViewById(R.id.preferences_password).setOnClickListener(this);
		mView.findViewById(R.id.preferences_date).setOnClickListener(this);
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
		mEmailList.setAdapter(null);
		setListViewHeightBasedOnChildren(mEmailList);
		noInternetMessage = Snackbar.make(mView, R.string.network_unavailable, Snackbar.LENGTH_INDEFINITE);
		noInternetMessage.setAction(R.string.reload, new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				reload();
			}
		}).addCallback(new Snackbar.Callback()
		{
			@Override
			public void onShown(Snackbar sb)
			{
				if (connectingMessage.isShown())
				{
					connectingMessage.dismiss();
				}
			}

			@Override
			public void onDismissed(Snackbar transientBottomBar, int event)
			{
				if (!isNetworkAvailable())
				{
					connectingMessage.show();
				}
			}
		});

		connectingMessage = Snackbar.make(mView, R.string.network_connecting, Snackbar.LENGTH_INDEFINITE);
		Snackbar.SnackbarLayout connectingView = (Snackbar.SnackbarLayout) connectingMessage.getView();
		connectingView.addView(new ProgressBar(getContext()));
		connectingMessage.addCallback(new Snackbar.Callback()
		{
			@Override
			public void onShown(Snackbar sb)
			{
				if (!isNetworkAvailable())
				{
					noInternetMessage.show();
				}
			}
		});

		if(isNetworkAvailable())
		{
			showAddressProgress();
		}
		else
		{
			hideAddressProgress();
		}

	}

	private void displayAccount()
	{
		((TextView) mView.findViewById(R.id.account_name)).setText(mAccount.getAccountName());
		((TextView) mView.findViewById(R.id.firstname)).setText(mAccount.getFirstName());
		((TextView) mView.findViewById(R.id.middlename)).setText(mAccount.getMiddleName());
		((TextView) mView.findViewById(R.id.lastname)).setText(mAccount.getLastName());
		((TextView) mView.findViewById(R.id.username)).setText(mAccount.getUsername());
		if (mAccount.getEmail() != null)
		{
			((TextView) mView.findViewById(R.id.email)).setText(mAccount.getEmail().getAddress());
		}
		((TextView) mView.findViewById(R.id.password)).setText(mAccount.getPassword());
		Calendar dob = mAccount.getDateOfBirth();
		((TextView) mView.findViewById(R.id.day)).setText(String.valueOf(dob.get(Calendar.DAY_OF_MONTH)));
		((TextView) mView.findViewById(R.id.month)).setText(String.valueOf(dob.get(Calendar.MONTH) + 1));
		((TextView) mView.findViewById(R.id.year)).setText(String.valueOf(dob.get(Calendar.YEAR)));
	}

	private void toggleNoInternetMessage(boolean show)
	{
		if (show)
		{
			if (!noInternetMessage.isShown())
			{
				noInternetMessage.show();
				connectingMessage.dismiss();
			}
		}
		else
		{
			if (noInternetMessage.isShown())
			{
				noInternetMessage.dismiss();
				connectingMessage.dismiss();
			}
		}
	}

	private void hideAddressProgress()
	{
		mEmailWrapper.setVisibility(View.VISIBLE);
		mAddressProgress.setVisibility(View.GONE);
	}

	private void showAddressProgress()
	{
		mEmailWrapper.setVisibility(View.GONE);
		mAddressProgress.setVisibility(View.VISIBLE);
	}

	private void hideEmailsProgress()
	{
		mMailProgress.setVisibility(View.GONE);
	}

	private void showEmailsProgress()
	{
		mMailProgress.setVisibility(View.VISIBLE);
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

	/**
	 * Taken from Stack Overflow - https://stackoverflow.com/a/17201376/6052295
	 *
	 * @param html
	 * @param linkifyMask
	 * @return
	 */

	public static Spannable linkifyHtml(String html, int linkifyMask)
	{
		Spanned text = fromHtml(fromHtml(html).toString());
		URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

		SpannableString buffer = new SpannableString(text);
		Linkify.addLinks(buffer, linkifyMask);

		for (URLSpan span : currentSpans)
		{
			int end = text.getSpanEnd(span);
			int start = text.getSpanStart(span);
			buffer.setSpan(span, start, end, 0);
		}
		return buffer;
	}

	/**
	 * Taken from Stack Overflow - https://stackoverflow.com/a/4239019
	 *
	 * @return
	 */
	private boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
