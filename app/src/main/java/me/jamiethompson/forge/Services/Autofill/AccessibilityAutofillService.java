package me.jamiethompson.forge.Services.Autofill;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.InputType;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.CurrentManager;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Notifications;

/**
 * Created by jamie on 09/10/17.
 * Service for detecting valid fields for auto fill and populating them on notifcation click
 */

public class AccessibilityAutofillService extends AccessibilityService {
    // Current instance
    public static AccessibilityAutofillService instance;
    // List of nodes that are edit texts in the window
    private ArrayList<AccessibilityNodeInfo> editTextNodes;
    // Edit text nodes that are valid for autofill
    private ArrayList<AutoFillNode> validNodes;
    // Current package loaded
    private CharSequence currentPackage = "";
    // For browsers, last loaded URL
    private String lastURL = "";
    // Is the auto fill notification visible
    private boolean notificationDisplayed = false;

    public AccessibilityAutofillService() {
        instance = this;
        validNodes = new ArrayList<>();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        switch (accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                // When the content of the window has changed
                if (accessibilityEvent.getPackageName() != null) {
                    // Get the package
                    CharSequence packageName = accessibilityEvent.getPackageName();
                    if (!currentPackage.equals(packageName)
                            && !packageName.equals("com.android.systemui")
                            && !packageName.equals("me.jamiethompson.forge")) {
                        // If the package is new and not system UI or forge
                        // Clear the nodes, remove the notificiation and update current package
                        currentPackage = accessibilityEvent.getPackageName();
                        validNodes = new ArrayList<>();
                        Notifications.removeHelperNotification(getApplicationContext());
                        notificationDisplayed = false;
                    }
                }
                // Get root node
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                // Get a list of all edit texts in the window
                editTextNodes = new ArrayList<>();
                findChildViews(rootNode);

                for (AccessibilityNodeInfo node : editTextNodes) {
                    // Iterate through each edit text
                    if (!alreadyExists(node)) {
                        // If it's not already in the valid nodes list
                        boolean added = false;
                        if (node.getViewIdResourceName() != null) {
                            // Get the view ID
                            String viewIdName = node.getViewIdResourceName();
                            if (viewIdName != null) {
                                viewIdName = viewIdName.toLowerCase();
                                if (!viewIdName.contains("url_bar")) {
                                    // If the ID contains any of the matching keywords, add it to
                                    // the valid nodes with the input type
                                    if (viewIdName.contains("pass")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.PASSWORD));
                                        added = true;
                                    } else if (viewIdName.contains("email")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.EMAIL));
                                        added = true;
                                    } else if (viewIdName.contains("first") || viewIdName.contains("given")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.FIRST_NAME));
                                        added = true;
                                    } else if (viewIdName.contains("middle")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.MIDDLE_NAME));
                                        added = true;
                                    } else if (viewIdName.contains("full")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.FULL_NAME));
                                        added = true;
                                    } else if (viewIdName.contains("last") || viewIdName.contains("family")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.MIDDLE_NAME));
                                        added = true;
                                    } else if (viewIdName.contains("user")) {
                                        validNodes.add(new AutoFillNode(node, AutoFillNode.USERNAME));
                                        added = true;
                                    }
                                } else {
                                    // If the edit text is for the URL BAR
                                    if (node.getText() != null) {
                                        if (!node.getText().toString().equals(lastURL)) {
                                            // If the URL is different from the last saved URL
                                            // Clear the nodes, remove the notification and
                                            // update the last URL
                                            lastURL = node.getText().toString();
                                            validNodes = new ArrayList<>();
                                            Notifications.removeHelperNotification(getApplicationContext());
                                            notificationDisplayed = false;
                                        }
                                    }
                                }
                            }
                            if (!added) {
                                // If the node hasn't been added to the valid nodes list
                                /*
                                 * For some reason, get input type seems to return the input type integer + 1, which
                                 * is why I am subtracting 1 from the value, strange
                                 */
                                if (viewIdName == null || !viewIdName.contains("url_bar")) {
                                    // If there is a view ID and it isn't the URL bar
                                    // If the edit text has any of the following input types, add
                                    // it to the valid nodes with the input type
                                    switch (node.getInputType() - 1) {
                                        case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                                        case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                                        case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                                            added = true;
                                            validNodes.add(new AutoFillNode(node, AutoFillNode.PASSWORD));
                                            break;
                                        case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
                                            added = true;
                                            validNodes.add(new AutoFillNode(node, AutoFillNode.USERNAME));
                                            break;
                                        case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                                        case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                                            added = true;
                                            validNodes.add(new AutoFillNode(node, AutoFillNode.EMAIL));
                                            break;
                                    }
                                }
                            }
                            if (!notificationDisplayed) {
                                // If there is no notification displayed
                                if (added) {
                                    // If there is a valid node
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    if (sharedPref.getBoolean(getString(R.string.pref_helper_key), false)) {
                                        // If the helper notification is enabled, show the
                                        // notification and update the toggle
                                        Notifications.displayHelperNotification(this, false);
                                        notificationDisplayed = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Traverses the node structure and finds edit texts, adding them to the edit text node list
     * @param parentView
     */
    private void findChildViews(AccessibilityNodeInfo parentView) {
        if (parentView != null) {
            // Get number of children of a node
            int childCount = parentView.getChildCount();

            if (parentView.getClassName() == null) {
                return;
            }
            // Get node class name
            String classname = parentView.getClassName().toString();
            if (childCount == 0 && (classname.contentEquals("android.widget.EditText"))) {
                // If the node is an end node and it is an edit text, add it to the list
                editTextNodes.add(parentView);
            } else {
                // Otherwise loop through any child nodes and try on them
                for (int i = 0; i < childCount; i++) {
                    findChildViews(parentView.getChild(i));
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * Fills any valid nodes with the matching Forge details
     */
    public void autofill() {
        // Get the currently loaded Forge Account
        ForgeAccount account = CurrentManager.loadCurrentAccount(getApplicationContext());
        for (AutoFillNode node : validNodes) {
            // For each valid node
            // Get the node input text
            String input = getInput(node.getType(), account);
            // Perform an action and put the text into the field
            Bundle arguments = new Bundle();
            arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, input);
            node.getAccessibilityNode().performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments);
        }
    }

    /**
     * Gets the correct field text from the Forge Account based on the Input Type of the node
     * @param type input type of the node
     * @param account forge account to retrieve text from
     * @return the text to put into the node field
     */
    private String getInput(int type, ForgeAccount account) {
        switch (type) {
            case AutoFillNode.PASSWORD:
                return account.getPassword();
            case AutoFillNode.USERNAME:
                return account.getUsername();
            case AutoFillNode.EMAIL:
                return account.getEmail().getAddress();
            case AutoFillNode.FIRST_NAME:
                return account.getFirstName();
            case AutoFillNode.MIDDLE_NAME:
                return account.getMiddleName();
            case AutoFillNode.LAST_NAME:
                return account.getLastName();
            case AutoFillNode.FULL_NAME:
                return account.getFirstName() + " " + account.getLastName();
            default:
                return "";
        }
    }

    /**
     * Checks if a node has already been added to the valid node list
     * @param node the node to check
     * @return true = node already in the list, false = node not yet in the list
     */
    private boolean alreadyExists(AccessibilityNodeInfo node) {
        if (node.getViewIdResourceName() != null) {
            int i = 0;
            while (i < validNodes.size()) {
                // Iterate through each valid node in the list
                AccessibilityNodeInfo compareNode = validNodes.get(i).getAccessibilityNode();
                if (compareNode.getViewIdResourceName() != null) {
                    if (compareNode.getViewIdResourceName().equals(node.getViewIdResourceName())) {
                        // If there is a node in the list with a matching ID, it already exists in
                        // the lists
                        return true;
                    }
                }
                i++;
            }
        }
        return false;
    }

}
