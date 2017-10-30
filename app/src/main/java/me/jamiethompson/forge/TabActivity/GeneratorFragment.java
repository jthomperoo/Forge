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
    private EditText emailEntry;
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

    private boolean emailShown = false;

    private Dialog emailDialog = null;

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
        displayAccount();
        ForgeAccount currentAccount = CurrentManager.loadCurrentAccount(context);
        if (currentAccount != null) {
            if (currentAccount.getEmail() != null) {
                load(currentAccount);
            }
        }
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs),
                Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(General.FIRST_RUN, true)) {
            Tutorial tutorial = new Tutorial(getActivity(),
                    R.id.refresh,
                    R.id.refresh_username,
                    R.id.copy_username,
                    R.id.email_list,
                    (ScrollView) view.findViewById(R.id.scroll));
            tutorial.startTutorial();
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
        boolean networkAvailable = Util.isNetworkAvailable(context);
        if (networkAvailable) {
            toggleNoInternetMessage(false);
        } else {
            toggleNoInternetMessage(true);
        }
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
                account = generator.refreshItem(account, UI.FIRSTNAME, networkAvailable);
                break;
            }
            case R.id.refresh_middlename: {
                account = generator.refreshItem(account, UI.MIDDLENAME, networkAvailable);
                break;
            }
            case R.id.refresh_lastname: {
                account = generator.refreshItem(account, UI.LASTNAME, networkAvailable);
                break;
            }
            case R.id.refresh_username: {
                account = generator.refreshItem(account, UI.USERNAME, networkAvailable);
                break;
            }
            case R.id.refresh_email: {
                account = generator.refreshItem(account, UI.EMAIL, networkAvailable);
                this.emailMessages = new ArrayList<>();
                emailList.setAdapter(null);
                setListViewHeightBasedOnChildren(emailList);
                if (networkAvailable) {
                    showAddressProgress();
                }
                break;
            }
            case R.id.refresh_password: {
                generator.refreshItem(account, UI.PASSWORD, networkAvailable);
                break;
            }
            case R.id.refresh_date: {
                generator.refreshItem(account, UI.DATE, networkAvailable);
                break;
            }
            case R.id.copy_firstname: {
                String nameTag = getString(R.string.firstname);
                addToClipboard(nameTag, account.getFirstName());
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
                addToClipboard(nameTag, String.format("%d/%d/%d", dob.get(Calendar.YEAR), dob.get(Calendar.MONTH) + 1, dob.get(Calendar.DAY_OF_MONTH)));
                Feedback.displayMessage(String.format("%s %s", nameTag, getString(R.string.copy_to_clip)), this.view);
                break;
            }
            case R.id.more_toggle: {
                if (moreToggled) {
                    for (LinearLayout layout : moreFields) {
                        layout.setVisibility(View.GONE);
                    }
                    moreToggle.setText(getString(R.string.more_fields));
                } else {
                    for (LinearLayout layout : moreFields) {
                        layout.setVisibility(View.VISIBLE);
                    }
                    moreToggle.setText(getString(R.string.less_fields));
                }
                moreToggled = !moreToggled;
            }
        }
        CurrentManager.updateCurrentAccount(account, context);
        displayAccount();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ((CheckBox) view.findViewById(R.id.read)).setChecked(false);
        emailMessages.get(i).setRead(true);
        EmailMessage email = emailMessages.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_email, null))
                .setPositiveButton(getString(R.string.dialog_action_dismiss), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                emailShown = false;
                emailDialog = null;
            }
        });
        dialog.show();
        emailShown = true;
        emailDialog = dialog;
        ((TextView) dialog.findViewById(R.id.subject)).setText(email.getSubject());
        ((TextView) dialog.findViewById(R.id.from)).setText(email.getFrom());
        ((TextView) dialog.findViewById(R.id.time)).setText(email.getTime());
        if (Util.isNetworkAvailable(context)) {
            generator.fetchEmail(email);
        } else {
            TextView body = emailDialog.findViewById(R.id.body);
            emailDialog.findViewById(R.id.body_loading).setVisibility(View.GONE);
            body.setVisibility(View.VISIBLE);
            body.setText(linkifyHtml(email.getBody(), Linkify.WEB_URLS));
            body.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mailPollHandler.postDelayed(new Runnable() {
            public void run() {
                if (account.getEmail().getAddress() != null) {
                    showEmailsProgress();
                    if (Util.isNetworkAvailable(context)) {
                        if (emailMessages.isEmpty()) {
                            generator.refreshEmails(account.getEmail(), null);
                        } else {
                            generator.refreshEmails(account.getEmail(), emailMessages.get(0));
                        }
                    } else {
                        toggleNoInternetMessage(true);
                    }
                    mailPollHandler.postDelayed(this, General.EMAIL_REFRESH_DELAY);
                }
            }
        }, General.EMAIL_REFRESH_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        mailPollHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loadAddress(final EmailAddress email) {
        if (email != null) {
            Feedback.displayMessage(String.format(getString(R.string.email_set_message), email.getAddress()), view);
            mailPollHandler.removeCallbacksAndMessages(null);
            account.setEmail(email);
            emailEntry.setText(email.getAddress().split("@")[0]);
            emailMessages = new ArrayList<>();
            emailList.setAdapter(null);

            hideAddressProgress();
            showEmailsProgress();

            if (Util.isNetworkAvailable(context)) {
                if (emailMessages.isEmpty()) {
                    generator.refreshEmails(email, null);
                } else {
                    generator.refreshEmails(email, emailMessages.get(0));
                }
            } else {
                toggleNoInternetMessage(true);
            }
            CurrentManager.updateCurrentAccount(account, context);


            mailPollHandler.postDelayed(new Runnable() {
                public void run() {
                    showEmailsProgress();
                    if (Util.isNetworkAvailable(context)) {
                        if (emailMessages.isEmpty()) {
                            generator.refreshEmails(email, null);
                        } else {
                            generator.refreshEmails(email, emailMessages.get(0));
                        }
                    } else {
                        toggleNoInternetMessage(true);
                    }
                    mailPollHandler.postDelayed(this, General.EMAIL_REFRESH_DELAY);
                }
            }, General.EMAIL_REFRESH_DELAY);
        }
    }

    @Override
    public void loadEmail(EmailMessage email) {
        if (emailShown) {
            TextView body = emailDialog.findViewById(R.id.body);
            emailDialog.findViewById(R.id.body_loading).setVisibility(View.GONE);
            body.setVisibility(View.VISIBLE);
            body.setText(linkifyHtml(email.getBody(), Linkify.WEB_URLS));
            body.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void loadEmails(List<EmailMessage> emails) {
        if (!emails.isEmpty()) {
            emailList.setAdapter(null);
            for (EmailMessage message : emails) {
                if (!containsID(emailMessages, message.getId())) {
                    emailMessages.add(message);
                }
            }
        }
        hideEmailsProgress();
        EmailListAdapter adapter = new EmailListAdapter(context, R.layout.item_email, this.emailMessages);
        emailList.setAdapter(adapter);
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
        Feedback.displayMessage(getString(R.string.message_account_loaded), view);
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
        // External save
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
        account = generator.forgeAccount(Util.isNetworkAvailable(context));
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
        account = generator.forgeAccount(Util.isNetworkAvailable(context));
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
        emailEntry = view.findViewById(R.id.email);

        emailEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    generator.setEmailAddress(new EmailAddress(
                            String.format("%s@%s", emailEntry.getText().toString(), General.MAIL_DOMAIN),
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
