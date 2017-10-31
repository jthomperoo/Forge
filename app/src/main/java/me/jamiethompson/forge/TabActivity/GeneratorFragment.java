package me.jamiethompson.forge.TabActivity;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import me.jamiethompson.forge.Constants.General;
import me.jamiethompson.forge.Constants.UI;
import me.jamiethompson.forge.Data.EmailAddress;
import me.jamiethompson.forge.Data.EmailMessage;
import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.CurrentManager;
import me.jamiethompson.forge.Files.FileManager;
import me.jamiethompson.forge.Generator.ForgeGenerator;
import me.jamiethompson.forge.Interfaces.EmailInterface;
import me.jamiethompson.forge.Interfaces.LoadInterface;
import me.jamiethompson.forge.ListView.EmailListAdapter;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.Tutorial;
import me.jamiethompson.forge.UI.Feedback;
import me.jamiethompson.forge.UI.SaveListener;
import me.jamiethompson.forge.Util;

/**
 * Created by jamie on 27/09/17.
 * Generator Fragment handles creating new Forge Accounts and displaying them to the user
 */

public class GeneratorFragment extends Fragment implements View.OnClickListener, EmailInterface, ListView.OnItemClickListener, LoadInterface {
    // Handler for polling the mail APIs
    final private Handler mailPollHandler = new Handler();
    // Fragment context
    private Context context;
    // Fragment view
    private View view;
    // Snackbars for user feedback
    private Snackbar noInternetMessage;
    private Snackbar connectingMessage;
    // Forge account generator
    private ForgeGenerator generator;
    // Current Forge account
    private ForgeAccount account;
    // Progress bars for loading email information
    private ProgressBar addressProgress;
    private ProgressBar mailProgress;
    // Email text wrapper
    private TextInputLayout emailWrapper;
    // Drop down for choosing email provider
    private Spinner mailDomain;
    // Email entry
    private EditText emailIdentifier;
    // Email inbox list
    private ListView emailList;
    // Account entry
    private EditText accountNameEntry;
    // Loaded email inbox messages
    private List<EmailMessage> emailMessages;
    // Show more details toggled
    private boolean moreToggled = false;
    // Additional fields shown when more is toggled
    private List<LinearLayout> moreFields;
    // More/less toggle button
    private Button moreToggle;
    // If the current account has been loaded from storage or not
    private boolean loaded = false;
    // If an email dialog is currently open, true = dialog open, false = no dialog
    private boolean emailShown = false;
    // The email dialog that is currently (or last) open
    private Dialog emailDialog = null;
    // The email polling runnable, polls the email inbox repeatedly with a set delay between
    private Runnable emailPollRunnable = new Runnable() {
        public void run() {
            if (account.getEmail().getAddress() != null) {
                // If the email is set
                // Show the emails loading progress
                showEmailsProgress();
                if (Util.isNetworkAvailable(context)) {
                    // If there is an internet connection
                    if (emailMessages.isEmpty()) {
                        // If there are no email messages
                        // Load the email messages, with no latest email message set
                        generator.refreshEmails(account.getEmail(), null);
                    } else {
                        // If there are email messages
                        // Load the email messages, with the latest one provided
                        generator.refreshEmails(account.getEmail(), emailMessages.get(0));
                    }
                } else {
                    // If there is no internet, display no internet message
                    toggleNoInternetMessage(true);
                }
                // Call this handler again, waiting a certain delay
                mailPollHandler.postDelayed(this, General.EMAIL_REFRESH_DELAY);
            }
        }
    };

    /**
     * Creates and returns a new instance of the Generator Fragment
     *
     * @return the newly instantiated Generator Fragment
     */
    public static GeneratorFragment newInstance() {
        return new GeneratorFragment();
    }

    /**
     * Taken from Stack Overflow - https://stackoverflow.com/a/26501296
     * Updates the ListView height based on its children
     * @param listView the ListView to adjust
     */

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
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
     * Decodes an encoded HTML string
     * @param html the encoded HTML string to decode
     * @return the decoded HTML string
     */

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /**
     * Taken from Stack Overflow - https://stackoverflow.com/a/17201376/6052295
     * Adds links to a HTML string
     * @param html the HTML string to add links to
     * @param linkifyMask the link type
     * @return The spannable text with clickable links
     */

    public static Spannable linkifyHtml(String html, int linkifyMask) {
        Spanned text = fromHtml(fromHtml(html).toString());
        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);
        Linkify.addLinks(buffer, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        return buffer;
    }

    /**
     * Checks if a list of email messages contains an ID
     *
     * @param messageList the list of email messages
     * @param id          the id to check for
     * @return true = list contains the email, false = list doesn't contain the email
     */
    public static boolean containsID(Collection<EmailMessage> messageList, String id) {
        for (EmailMessage message : messageList) {
            // For each message
            if (message != null && message.getId().equals(id)) {
                // If the message id equals the ID being searched for
                return true;
            }
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_generator, container, false);
        this.view = view;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get fragment context
        context = getContext();
        // Set up global variables
        setUpGlobals();
        // Set up user interface variables and UI
        setUpUserInterface();
        // Get any account that is stored in temporary storage
        ForgeAccount currentAccount = CurrentManager.loadCurrentAccount(context);
        if (currentAccount != null) {
            // If the account exists
            if (currentAccount.getEmail() != null) {
                // If the account has an email, load that account
                load(currentAccount);
            }
        } else {
            // If the account doesn't exist, generate a new one
            account = generator.forgeAccount(Util.isNetworkAvailable(context), (String) mailDomain.getSelectedItem());
        }
        // Display the account
        displayAccount();
        // Open shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(General.FIRST_RUN, true)) {
            // If it is the first run of the app, show the tutorial
            Tutorial tutorial = new Tutorial(getActivity(),
                    R.id.refresh,
                    R.id.refresh_username,
                    R.id.copy_username,
                    R.id.email_list,
                    (ScrollView) view.findViewById(R.id.scroll));
            tutorial.startTutorial();
            // Mark the app as having been run
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(General.FIRST_RUN, false);
            editor.apply();
        }
    }

    @Override
    public void onDetach() {
        view = null;
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        // If the network is available
        boolean networkAvailable = Util.isNetworkAvailable(context);
        if (networkAvailable) {
            // If available, hide any no internet messages
            toggleNoInternetMessage(false);
        } else {
            // If not available, display no internet message
            toggleNoInternetMessage(true);
        }
        // Get which element has been clicked, switch to determine action
        switch (view.getId()) {
            case R.id.save: {
                save();
                break;
            }
            case R.id.refresh: {
                refresh();
                break;
            }
            case R.id.refresh_firstname: {
                account = generator.refreshItem(account, UI.FIRSTNAME, networkAvailable, (String) mailDomain.getSelectedItem());
                break;
            }
            case R.id.refresh_middlename: {
                account = generator.refreshItem(account, UI.MIDDLENAME, networkAvailable, (String) mailDomain.getSelectedItem());
                break;
            }
            case R.id.refresh_lastname: {
                account = generator.refreshItem(account, UI.LASTNAME, networkAvailable, (String) mailDomain.getSelectedItem());
                break;
            }
            case R.id.refresh_username: {
                account = generator.refreshItem(account, UI.USERNAME, networkAvailable, (String) mailDomain.getSelectedItem());
                break;
            }
            case R.id.refresh_email: {
                // Get a new email
                account = generator.refreshItem(account, UI.EMAIL, networkAvailable, (String) mailDomain.getSelectedItem());
                // Clear inbox
                this.emailMessages = new ArrayList<>();
                emailList.setAdapter(null);
                setListViewHeightBasedOnChildren(emailList);
                if (networkAvailable) {
                    // If there is a network connection, show the address loading bar
                    showAddressProgress();
                }
                break;
            }
            case R.id.refresh_password: {
                generator.refreshItem(account, UI.PASSWORD, networkAvailable, (String) mailDomain.getSelectedItem());
                break;
            }
            case R.id.refresh_date: {
                generator.refreshItem(account, UI.DATE, networkAvailable, (String) mailDomain.getSelectedItem());
                break;
            }
            case R.id.copy_firstname: {
                // Copy the item to the clipboard
                String nameTag = getString(R.string.firstname);
                addToClipboard(nameTag, account.getFirstName());
                // Display a message informing the user that the item has been copied to the clipboard
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.copy_middlename: {
                String nameTag = getString(R.string.middlename);
                addToClipboard(nameTag, account.getMiddleName());
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.copy_lastname: {
                String nameTag = getString(R.string.lastname);
                addToClipboard(nameTag, account.getLastName());
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.copy_username: {
                String nameTag = getString(R.string.username);
                addToClipboard(nameTag, account.getUsername());
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.copy_email: {
                String nameTag = getString(R.string.email);
                addToClipboard(nameTag, account.getEmail().getAddress());
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.copy_password: {
                String nameTag = getString(R.string.password);
                addToClipboard(nameTag, account.getPassword());
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.copy_date: {
                String nameTag = getString(R.string.date);
                Calendar dob = account.getDateOfBirth();
                // Copy to clipboard in date format
                addToClipboard(nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH) + 1, dob.get(Calendar.DAY_OF_MONTH)));
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.more_toggle: {
                if (moreToggled) {
                    // If more toggled off, hide each advanced field
                    for (LinearLayout layout : moreFields) {
                        layout.setVisibility(View.GONE);
                    }
                    // Update the more button text
                    moreToggle.setText(getString(R.string.more_fields));
                } else {
                    // If more toggled on, show each advanced field
                    for (LinearLayout layout : moreFields) {
                        layout.setVisibility(View.VISIBLE);
                    }
                    // Update the more button text
                    moreToggle.setText(getString(R.string.less_fields));
                }
                // Invert more toggled
                moreToggled = !moreToggled;
            }
        }
        // Update the currently loaded account
        CurrentManager.updateCurrentAccount(account, context);
        // Display the account
        displayAccount();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // When an email is opened
        // Set the email read checkbox to be unchecked
        ((CheckBox) view.findViewById(R.id.read)).setChecked(false);
        // Get the email
        EmailMessage email = emailMessages.get(i);
        // Set the email as having been read
        email.setRead(true);
        // Set up alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Set up layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set alert dialog listeners and layout
        builder.setView(inflater.inflate(R.layout.dialog_email, null))
                .setPositiveButton(getString(R.string.dialog_action_dismiss), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // When button is pressed, dismiss the email dialog
                        dialogInterface.dismiss();
                    }
                });
        // Create the dialog
        Dialog dialog = builder.create();
        // Set up on dismiss listener
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // On dismiss, update global dialog variables
                emailShown = false;
                emailDialog = null;
            }
        });
        // Display the dialog
        dialog.show();
        // Update global dialog variables
        emailShown = true;
        emailDialog = dialog;
        // Populate dialog text views with email content
        ((TextView) dialog.findViewById(R.id.subject)).setText(email.getSubject());
        ((TextView) dialog.findViewById(R.id.from)).setText(email.getFrom());
        ((TextView) dialog.findViewById(R.id.time)).setText(email.getTime());
        if (Util.isNetworkAvailable(context)) {
            // If there is an internet connection, get more info on the email
            generator.fetchEmail(email);
        } else {
            // If there is no internet connection, set the body to what is currently loaded
            // and display the body, hiding any loading bars
            TextView body = emailDialog.findViewById(R.id.body);
            emailDialog.findViewById(R.id.body_loading).setVisibility(View.GONE);
            body.setVisibility(View.VISIBLE);
            // Make links clickable
            body.setText(linkifyHtml(email.getBody(), Linkify.WEB_URLS));
            body.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity is opened again, run the handler that polls for new
        // emails into the inbox every set period of time
        mailPollHandler.postDelayed(emailPollRunnable, General.EMAIL_REFRESH_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the app is paused, stop the email inbox poll handler
        mailPollHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loadAddress(final EmailAddress email) {
        if (email != null) {
            // Stop any email inbox polling
            mailPollHandler.removeCallbacksAndMessages(null);
            // Get the first, identifier part of the email address
            String identifier = email.getAddress().split("@")[0];
            // Set the email address to use the identifier and the selected mail domain
            email.setAddress(String.format("%s%s", identifier, mailDomain.getSelectedItem()));
            // Display a message to the user informing them what the address has been set to
            Feedback.displayMessage(String.format(getString(R.string.email_set_message), email.getAddress()), view);
            // Update the account to use the email
            account.setEmail(email);
            emailIdentifier.setText(identifier);
            // Clear inbox
            emailMessages = new ArrayList<>();
            emailList.setAdapter(null);

            hideAddressProgress();
            showEmailsProgress();

            if (Util.isNetworkAvailable(context)) {
                // If there is an internet connection
                if (emailMessages.isEmpty()) {
                    // If there are no email messages
                    // Load the email messages, with no latest email message set
                    generator.refreshEmails(account.getEmail(), null);
                } else {
                    // If there are email messages
                    // Load the email messages, with the latest one provided
                    generator.refreshEmails(account.getEmail(), emailMessages.get(0));
                }
            } else {
                // If there is no internet, display no internet message
                toggleNoInternetMessage(true);
            }
            // Update the temporary current account to the account with the new email
            CurrentManager.updateCurrentAccount(account, context);
            // Run the handler that polls for new emails into the inbox every set period of time
            mailPollHandler.postDelayed(emailPollRunnable, General.EMAIL_REFRESH_DELAY);
        }
    }

    @Override
    public void loadEmail(EmailMessage email) {
        if (emailShown) {
            // If there is an email dialog shown
            // Update the dialog's body text
            TextView body = emailDialog.findViewById(R.id.body);
            // Hide the progress bar, show the body text
            emailDialog.findViewById(R.id.body_loading).setVisibility(View.GONE);
            body.setVisibility(View.VISIBLE);
            // Make the links clickable
            body.setText(linkifyHtml(email.getBody(), Linkify.WEB_URLS));
            body.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void loadEmails(List<EmailMessage> emails) {
        if (!emails.isEmpty()) {
            // If there are already are emails
            // Clear the emails
            emailList.setAdapter(null);
            // For each email, if it doesn't already exist in the list, add it
            for (EmailMessage message : emails) {
                if (!containsID(emailMessages, message.getId())) {
                    emailMessages.add(message);
                }
            }
        }

        hideEmailsProgress();
        // Set up email list adapter
        EmailListAdapter adapter = new EmailListAdapter(context, R.layout.item_email, this.emailMessages);
        // Set email list to use adapter
        emailList.setAdapter(adapter);
        // Adjust height of list view based on its children
        setListViewHeightBasedOnChildren(emailList);
    }

    @Override
    public void load(ForgeAccount account) {
        loaded = true;
        this.account = account;
        this.emailMessages = new ArrayList<>();
        emailList.setAdapter(null);
        setListViewHeightBasedOnChildren(emailList);

        if (Util.isNetworkAvailable(context)) {
            generator.setEmailAddress(account.getEmail());
            showAddressProgress();
        }
        CurrentManager.updateCurrentAccount(this.account, context);
        displayAccount();
    }

    private void save() {
        account.setAccountName(accountNameEntry.getText().toString());
        if (loaded) {
            DialogInterface.OnClickListener dialogClickListener = new SaveListener(account, getActivity(), view);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(getString(R.string.dialog_overwrite))
                    .setPositiveButton(getString(R.string.option_overwrite), dialogClickListener)
                    .setNegativeButton(getString(R.string.option_save_new), dialogClickListener)
                    .setNeutralButton(getString(R.string.option_cancel), dialogClickListener)
                    .show();
        } else {
            ForgeAccount saveAccount = FileManager.add(getActivity(), account);
            if (saveAccount != null) {
                Feedback.displayMessage(getString(R.string.message_account_saved), view);
                loaded = true;
            }
        }
        ((Forge) getActivity()).reloadSaveList();
    }

    private void refresh() {
        if (Util.isNetworkAvailable(context)) {
            toggleNoInternetMessage(false);
            showAddressProgress();
        } else {
            toggleNoInternetMessage(true);
        }
        loaded = false;
        this.emailMessages = new ArrayList<>();
        emailList.setAdapter(null);
        setListViewHeightBasedOnChildren(emailList);
        account = generator.forgeAccount(Util.isNetworkAvailable(context), (String) mailDomain.getSelectedItem());
        displayAccount();
    }

    private void reload() {
        if (Util.isNetworkAvailable(context)) {
            toggleNoInternetMessage(false);
            generator.setEmailAddress(account.getEmail());
            showAddressProgress();
        } else {
            toggleNoInternetMessage(true);
        }
    }

    private void setUpGlobals() {
        emailMessages = new ArrayList<>();
        generator = new ForgeGenerator(this, context);
        loaded = false;
        emailMessages = new ArrayList<>();
    }

    private void setUpUserInterface() {
        ((TextInputLayout) view.findViewById(R.id.account_name_wrapper)).setHint(getString(R.string.account_name));
        ((TextInputLayout) view.findViewById(R.id.firstname_wrapper)).setHint(getString(R.string.firstname));
        ((TextInputLayout) view.findViewById(R.id.middlename_wrapper)).setHint(getString(R.string.middlename));
        ((TextInputLayout) view.findViewById(R.id.lastname_wrapper)).setHint(getString(R.string.lastname));
        ((TextInputLayout) view.findViewById(R.id.username_wrapper)).setHint(getString(R.string.username));
        emailWrapper = view.findViewById(R.id.email_wrapper);
        emailWrapper.setHint(getString(R.string.email));
        mailDomain = view.findViewById(R.id.mail_domain);
        mailDomain.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String mailWithDomain = String.format("%s%s", emailIdentifier.getText().toString(), mailDomain.getSelectedItem());
                account.setEmail(new EmailAddress(mailWithDomain, account.getEmail().getSidToken()));
                CurrentManager.updateCurrentAccount(account, context);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ((TextInputLayout) view.findViewById(R.id.account_name_wrapper)).setHint(getString(R.string.account_name));
        ((TextInputLayout) view.findViewById(R.id.year_wrapper)).setHint(getString(R.string.year));
        ((TextInputLayout) view.findViewById(R.id.month_wrapper)).setHint(getString(R.string.month));
        ((TextInputLayout) view.findViewById(R.id.day_wrapper)).setHint(getString(R.string.day));


        moreToggle = view.findViewById(R.id.more_toggle);
        moreToggle.setOnClickListener(this);

        moreFields = new ArrayList<>();

        moreFields.add((LinearLayout) view.findViewById(R.id.firstname_row));
        moreFields.add((LinearLayout) view.findViewById(R.id.middlename_row));
        moreFields.add((LinearLayout) view.findViewById(R.id.lastname_row));
        moreFields.add((LinearLayout) view.findViewById(R.id.dob_row));

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
        // save button
        view.findViewById(R.id.save).setOnClickListener(this);
        // assign globals
        accountNameEntry = view.findViewById(R.id.account_name);
        emailIdentifier = view.findViewById(R.id.email);

        emailIdentifier.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    generator.setEmailAddress(new EmailAddress(
                            String.format("%s%s", emailIdentifier.getText().toString(), mailDomain.getSelectedItem()),
                            null));
                }
                return false;
            }
        });

        addressProgress = view.findViewById(R.id.address_progress);
        mailProgress = view.findViewById(R.id.mail_progress);
        emailList = view.findViewById(R.id.email_list);
        // listview
        emailList.setOnItemClickListener(this);
        emailList.setEmptyView(view.findViewById(R.id.empty));
        emailList.setAdapter(null);
        setListViewHeightBasedOnChildren(emailList);
        noInternetMessage = Snackbar.make(view, R.string.network_unavailable, Snackbar.LENGTH_INDEFINITE);
        noInternetMessage.setAction(R.string.reload, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        }).addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                if (connectingMessage.isShown()) {
                    connectingMessage.dismiss();
                }
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (!Util.isNetworkAvailable(context)) {
                    connectingMessage.show();
                }
            }
        });

        connectingMessage = Snackbar.make(view, R.string.network_connecting, Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout connectingView = (Snackbar.SnackbarLayout) connectingMessage.getView();
        connectingView.addView(new ProgressBar(context));
        connectingMessage.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                if (!Util.isNetworkAvailable(context)) {
                    noInternetMessage.show();
                }
            }
        });

        if (Util.isNetworkAvailable(context)) {
            showAddressProgress();
        } else {
            hideAddressProgress();
        }

    }

    private void displayAccount() {
        ((TextView) view.findViewById(R.id.account_name)).setText(account.getAccountName());
        ((TextView) view.findViewById(R.id.firstname)).setText(account.getFirstName());
        ((TextView) view.findViewById(R.id.middlename)).setText(account.getMiddleName());
        ((TextView) view.findViewById(R.id.lastname)).setText(account.getLastName());
        ((TextView) view.findViewById(R.id.username)).setText(account.getUsername());
        if (account.getEmail() != null) {
            ((TextView) view.findViewById(R.id.email)).setText(account.getEmail().getAddress().split("@")[0]);
            mailDomain.setSelection(getSpinnerIndex(mailDomain, "@" + account.getEmail().getAddress().split("@")[1]),true);
        }
        ((TextView) view.findViewById(R.id.password)).setText(account.getPassword());
        Calendar dob = account.getDateOfBirth();
        ((TextView) view.findViewById(R.id.day)).setText(String.valueOf(dob.get(Calendar.DAY_OF_MONTH)));
        ((TextView) view.findViewById(R.id.month)).setText(String.valueOf(dob.get(Calendar.MONTH) + 1));
        ((TextView) view.findViewById(R.id.year)).setText(String.valueOf(dob.get(Calendar.YEAR)));
    }

    private void toggleNoInternetMessage(boolean show) {
        if (show) {
            if (!noInternetMessage.isShown()) {
                noInternetMessage.show();
                connectingMessage.dismiss();
            }
        } else {
            if (noInternetMessage.isShown()) {
                noInternetMessage.dismiss();
                connectingMessage.dismiss();
            }
        }
    }

    private void hideAddressProgress() {
        emailWrapper.setVisibility(View.VISIBLE);
        mailDomain.setVisibility(View.VISIBLE);
        addressProgress.setVisibility(View.GONE);
    }

    private void showAddressProgress() {
        emailWrapper.setVisibility(View.GONE);
        mailDomain.setVisibility(View.GONE);
        addressProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Taken from Stack Overflow - https://stackoverflow.com/a/14640612/6052295
     *
     * @param spinner
     * @param myString
     * @return
     */
    private int getSpinnerIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void hideEmailsProgress() {
        mailProgress.setVisibility(View.GONE);
    }

    private void showEmailsProgress() {
        mailProgress.setVisibility(View.VISIBLE);
    }

    private void addToClipboard(String label, String content) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, content);
        clipboard.setPrimaryClip(clip);
    }

}
