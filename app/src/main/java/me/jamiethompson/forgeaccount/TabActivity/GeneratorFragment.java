package me.jamiethompson.forgeaccount.TabActivity;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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

import me.jamiethompson.forgeaccount.Constants;
import me.jamiethompson.forgeaccount.Data.EmailAddress;
import me.jamiethompson.forgeaccount.Data.EmailMessage;
import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.EmailInterface;
import me.jamiethompson.forgeaccount.Files.CurrentManager;
import me.jamiethompson.forgeaccount.Files.FileManager;
import me.jamiethompson.forgeaccount.Generator.ForgeGenerator;
import me.jamiethompson.forgeaccount.ListView.EmailListAdapter;
import me.jamiethompson.forgeaccount.LoadInterface;
import me.jamiethompson.forgeaccount.Preferences.DateOfBirthPreferenceFragment;
import me.jamiethompson.forgeaccount.Preferences.PasswordPreferenceFragment;
import me.jamiethompson.forgeaccount.Preferences.Preferences;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.Tutorial;
import me.jamiethompson.forgeaccount.UI.Feedback;
import me.jamiethompson.forgeaccount.UI.SaveDialogListener;

/**
 * Created by jamie on 27/09/17.
 */

public class GeneratorFragment extends Fragment implements View.OnClickListener, EmailInterface, ListView.OnItemClickListener, LoadInterface
{
	private static Context context;
	final private Handler mailPollHandler = new Handler();
	private Snackbar noInternetMessage;
	private Snackbar connectingMessage;
	private ForgeGenerator generator;
	private ForgeAccount account;
	private View view;
	private ProgressBar addressProgress;
	private ProgressBar mailProgress;
	private TextInputLayout emailWrapper;
	private TextView emailEntry;
	private ListView emailList;
	private EditText accountNameEntry;
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
		this.view = view;
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		context = getContext();
		setUpGlobals();
		setUpUserInterface();
		displayAccount();
		ForgeAccount currentAccount = CurrentManager.loadCurrentAccount(context);
		if (currentAccount != null)
		{
			if (currentAccount.getEmail() != null)
			{
				load(currentAccount);
			}
		}
		SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
		if (sharedPref.getBoolean(Constants.FIRST_RUN, true))
		{
			Tutorial tutorial = new Tutorial(getActivity(), R.id.refresh, R.id.refresh_firstname, R.id.copy_firstname, R.id.preferences_password, R.id.email_list);
			tutorial.startTutorial();
			sharedPref.edit().putBoolean(Constants.FIRST_RUN, true);
		}
	}

	@Override
	public void onDetach()
	{
		view = null;
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
				account = generator.refreshItem(account, Constants.FIRSTNAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_middlename:
			{
				account = generator.refreshItem(account, Constants.MIDDLENAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_lastname:
			{
				account = generator.refreshItem(account, Constants.LASTNAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_username:
			{
				account = generator.refreshItem(account, Constants.USERNAME, isNetworkAvailable());
				break;
			}
			case R.id.refresh_email:
			{
				account = generator.refreshItem(account, Constants.EMAIL, isNetworkAvailable());
				this.emailMessages = new ArrayList<>();
				emailList.setAdapter(null);
				setListViewHeightBasedOnChildren(emailList);
				if (isNetworkAvailable())
				{
					showAddressProgress();
				}
				break;
			}
			case R.id.refresh_password:
			{
				generator.refreshItem(account, Constants.PASSWORD, isNetworkAvailable());
				break;
			}
			case R.id.refresh_date:
			{
				generator.refreshItem(account, Constants.DATE, isNetworkAvailable());
				break;
			}
			case R.id.copy_firstname:
			{
				String nameTag = getString(R.string.firstname);
				addToClipboard(nameTag, account.getFirstName());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
				break;
			}
			case R.id.copy_middlename:
			{
				String nameTag = getString(R.string.middlename);
				addToClipboard(nameTag, account.getMiddleName());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
				break;
			}
			case R.id.copy_lastname:
			{
				String nameTag = getString(R.string.lastname);
				addToClipboard(nameTag, account.getLastName());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
				break;
			}
			case R.id.copy_username:
			{
				String nameTag = getString(R.string.username);
				addToClipboard(nameTag, account.getUsername());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
				break;
			}
			case R.id.copy_email:
			{
				String nameTag = getString(R.string.email);
				addToClipboard(nameTag, account.getEmail().getAddress());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
				break;
			}
			case R.id.copy_password:
			{
				String nameTag = getString(R.string.password);
				addToClipboard(nameTag, account.getPassword());
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
				break;
			}
			case R.id.copy_date:
			{
				String nameTag = getString(R.string.date);
				Calendar dob = account.getDateOfBirth();
				addToClipboard(nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH) + 1, dob.get(Calendar.DAY_OF_MONTH)));
				Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
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
		CurrentManager.updateCurrentAccount(account, context);
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
			account.setEmail(emailAddress);
			emailEntry.setText(emailAddress.getAddress());

			hideAddressProgress();
			showEmailsProgress();

			if (isNetworkAvailable())
			{
				generator.refreshEmails(emailAddress);
			}
			else
			{
				toggleNoInternetMessage(true);
			}
			CurrentManager.updateCurrentAccount(account, context);

			mailPollHandler.removeCallbacksAndMessages(null);

			mailPollHandler.postDelayed(new Runnable()
			{
				public void run()
				{
					if (emailAddress != null)
					{
						showEmailsProgress();
						if (isNetworkAvailable())
						{
							generator.refreshEmails(emailAddress);
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
			emailList.setAdapter(null);
			this.emailMessages = emails;
		}
		hideEmailsProgress();
		EmailListAdapter adapter = new EmailListAdapter(context, R.layout.item_email, this.emailMessages);
		emailList.setAdapter(adapter);
		setListViewHeightBasedOnChildren(emailList);
	}

	@Override
	public void load(ForgeAccount account)
	{
		mLoaded = true;
		this.account = account;
		this.emailMessages = new ArrayList<>();
		emailList.setAdapter(null);
		setListViewHeightBasedOnChildren(emailList);

		if (isNetworkAvailable())
		{
			generator.setEmailAddress(account.getEmail());
			showAddressProgress();
		}
		CurrentManager.updateCurrentAccount(this.account, context);
		displayAccount();
		Feedback.displayMessage(getString(R.string.message_account_loaded), view);
	}

	private void save()
	{
		account.setAccountName(accountNameEntry.getText().toString());
		if (mLoaded)
		{
			DialogInterface.OnClickListener dialogClickListener = new SaveDialogListener(account, getActivity(), view);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(getString(R.string.dialog_overwrite))
					.setPositiveButton(getString(R.string.option_overwrite), dialogClickListener)
					.setNegativeButton(getString(R.string.option_save_new), dialogClickListener)
					.setNeutralButton(getString(R.string.option_cancel), dialogClickListener)
					.show();
		}
		else
		{
			ForgeAccount saveAccount = FileManager.add(getActivity(), account);
			if (saveAccount != null)
			{
				Feedback.displayMessage(getString(R.string.message_account_saved), view);
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
		emailList.setAdapter(null);
		setListViewHeightBasedOnChildren(emailList);
		account = generator.forgeAccount(isNetworkAvailable());
		displayAccount();
	}

	private void reload()
	{
		if (isNetworkAvailable())
		{
			toggleNoInternetMessage(false);
			generator.setEmailAddress(account.getEmail());
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
		generator = new ForgeGenerator(this, context);
		mLoaded = false;
		emailMessages = new ArrayList<>();
		account = generator.forgeAccount(isNetworkAvailable());
	}

	private void setUpUserInterface()
	{
		((TextInputLayout) view.findViewById(R.id.account_name_wrapper)).setHint(getString(R.string.account_name));
		((TextInputLayout) view.findViewById(R.id.firstname_wrapper)).setHint(getString(R.string.firstname));
		((TextInputLayout) view.findViewById(R.id.middlename_wrapper)).setHint(getString(R.string.middlename));
		((TextInputLayout) view.findViewById(R.id.lastname_wrapper)).setHint(getString(R.string.lastname));
		((TextInputLayout) view.findViewById(R.id.username_wrapper)).setHint(getString(R.string.username));
		emailWrapper = ((TextInputLayout) view.findViewById(R.id.email_wrapper));
		emailWrapper.setHint(getString(R.string.email));
		((TextInputLayout) view.findViewById(R.id.account_name_wrapper)).setHint(getString(R.string.account_name));
		((TextInputLayout) view.findViewById(R.id.year_wrapper)).setHint(getString(R.string.year));
		((TextInputLayout) view.findViewById(R.id.month_wrapper)).setHint(getString(R.string.month));
		((TextInputLayout) view.findViewById(R.id.day_wrapper)).setHint(getString(R.string.day));

		// setting up click listeners
		// refresh
		view.findViewById(R.id.refresh).setOnClickListener(this);
		view.findViewById(R.id.refresh_firstname).setOnClickListener(this);
		view.findViewById(R.id.refresh_middlename).setOnClickListener(this);
		view.findViewById(R.id.refresh_lastname).setOnClickListener(this);
		view.findViewById(R.id.refresh_username).setOnClickListener(this);
		view.findViewById(R.id.refresh_email).setOnClickListener(this);
		view.findViewById(R.id.refresh_password).setOnClickListener(this);
		view.findViewById(R.id.refresh_date).setOnClickListener(this);
		// copy & paste
		view.findViewById(R.id.copy_firstname).setOnClickListener(this);
		view.findViewById(R.id.copy_middlename).setOnClickListener(this);
		view.findViewById(R.id.copy_lastname).setOnClickListener(this);
		view.findViewById(R.id.copy_username).setOnClickListener(this);
		view.findViewById(R.id.copy_email).setOnClickListener(this);
		view.findViewById(R.id.copy_password).setOnClickListener(this);
		view.findViewById(R.id.copy_date).setOnClickListener(this);
		// settings
		view.findViewById(R.id.preferences_password).setOnClickListener(this);
		view.findViewById(R.id.preferences_date).setOnClickListener(this);
		// save button
		view.findViewById(R.id.save).setOnClickListener(this);
		// assign globals
		accountNameEntry = view.findViewById(R.id.account_name);
		emailEntry = view.findViewById(R.id.email);
		addressProgress = view.findViewById(R.id.address_progress);
		mailProgress = view.findViewById(R.id.mail_progress);
		emailList = view.findViewById(R.id.email_list);
		// listview
		emailList.setOnItemClickListener(this);
		emailList.setOnTouchListener(new View.OnTouchListener()
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
		emailList.setAdapter(null);
		setListViewHeightBasedOnChildren(emailList);
		noInternetMessage = Snackbar.make(view, R.string.network_unavailable, Snackbar.LENGTH_INDEFINITE);
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

		connectingMessage = Snackbar.make(view, R.string.network_connecting, Snackbar.LENGTH_INDEFINITE);
		Snackbar.SnackbarLayout connectingView = (Snackbar.SnackbarLayout) connectingMessage.getView();
		connectingView.addView(new ProgressBar(context));
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
		((TextView) view.findViewById(R.id.account_name)).setText(account.getAccountName());
		((TextView) view.findViewById(R.id.firstname)).setText(account.getFirstName());
		((TextView) view.findViewById(R.id.middlename)).setText(account.getMiddleName());
		((TextView) view.findViewById(R.id.lastname)).setText(account.getLastName());
		((TextView) view.findViewById(R.id.username)).setText(account.getUsername());
		if (account.getEmail() != null)
		{
			((TextView) view.findViewById(R.id.email)).setText(account.getEmail().getAddress());
		}
		((TextView) view.findViewById(R.id.password)).setText(account.getPassword());
		Calendar dob = account.getDateOfBirth();
		((TextView) view.findViewById(R.id.day)).setText(String.valueOf(dob.get(Calendar.DAY_OF_MONTH)));
		((TextView) view.findViewById(R.id.month)).setText(String.valueOf(dob.get(Calendar.MONTH) + 1));
		((TextView) view.findViewById(R.id.year)).setText(String.valueOf(dob.get(Calendar.YEAR)));
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
		emailWrapper.setVisibility(View.VISIBLE);
		addressProgress.setVisibility(View.GONE);
	}

	private void showAddressProgress()
	{
		emailWrapper.setVisibility(View.GONE);
		addressProgress.setVisibility(View.VISIBLE);
	}

	private void hideEmailsProgress()
	{
		mailProgress.setVisibility(View.GONE);
	}

	private void showEmailsProgress()
	{
		mailProgress.setVisibility(View.VISIBLE);
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
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
