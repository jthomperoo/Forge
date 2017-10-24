package me.jamiethompson.forge.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.InputType;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

import me.jamiethompson.forge.AutoFillNode;
import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.CurrentManager;

/**
 * Created by jamie on 09/10/17.
 */

public class AccessibilityAutofillService extends AccessibilityService {
    public static AccessibilityAutofillService instance;
    private ArrayList<AccessibilityNodeInfo> editTextNodes;
    private ArrayList<AutoFillNode> validNodes;
    private CharSequence currentPackage = "";
    private String lastURL = "";

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
                if (accessibilityEvent.getPackageName() != null) {
                    CharSequence packageName = accessibilityEvent.getPackageName();
                    if (!currentPackage.equals(packageName)
                            && !packageName.equals("com.android.systemui")
                            && !packageName.equals("me.jamiethompson.forge")) {
                        currentPackage = accessibilityEvent.getPackageName();
                        validNodes = new ArrayList<>();
                    }
                }
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                editTextNodes = new ArrayList<>();
                findChildViews(rootNode);

                for (AccessibilityNodeInfo node : editTextNodes) {
                    if (!alreadyExists(node)) {
                        boolean added = false;
                        if (node.getViewIdResourceName() != null) {
                            String viewIdName = node.getViewIdResourceName();
                            if (viewIdName != null) {
                                viewIdName = viewIdName.toLowerCase();
                                if (!viewIdName.contains("url_bar")) {
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
                                    if (node.getText() != null) {
                                        if (!node.getText().toString().equals(lastURL)) {
                                            lastURL = node.getText().toString();
                                            validNodes = new ArrayList<>();
                                        }
                                    }
                                }
                            }
                            if (!added) {
                            /*
                             * For some reason, get input type seems to return the input type integer + 1, which
                             * is why I am subtracting 1 from the value, strange
                             */
                                if (viewIdName == null || !viewIdName.contains("url_bar")) {
                                    switch (node.getInputType() - 1) {
                                        case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                                        case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                                        case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                                            validNodes.add(new AutoFillNode(node, AutoFillNode.PASSWORD));
                                            break;
                                        case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
                                            validNodes.add(new AutoFillNode(node, AutoFillNode.USERNAME));
                                            break;
                                        case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                                        case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                                            validNodes.add(new AutoFillNode(node, AutoFillNode.EMAIL));
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void findChildViews(AccessibilityNodeInfo parentView) {
        if (parentView != null) {
            int childCount = parentView.getChildCount();

            if (parentView.getClassName() == null) {
                return;
            }
            String classname = parentView.getClassName().toString();
            if (childCount == 0 && (classname.contentEquals("android.widget.EditText"))) {
                editTextNodes.add(parentView);
            } else {
                for (int i = 0; i < childCount; i++) {
                    findChildViews(parentView.getChild(i));
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    public void autofill() {
        ForgeAccount account = CurrentManager.loadCurrentAccount(getApplicationContext());
        for (AutoFillNode node : validNodes) {
            String input = getInput(node.getType(), account);
            Bundle arguments = new Bundle();
            arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, input);
            node.getAccessibilityNode().performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments);
            SystemClock.sleep(200);
        }
    }

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

    private boolean alreadyExists(AccessibilityNodeInfo node) {
        if (node.getViewIdResourceName() != null) {
            int i = 0;
            while (i < validNodes.size()) {
                AccessibilityNodeInfo compareNode = validNodes.get(i).getAccessibilityNode();
                if (compareNode.getViewIdResourceName() != null) {
                    if (compareNode.getViewIdResourceName().equals(node.getViewIdResourceName())) {
                        return true;
                    }
                }
                i++;
            }
        }
        return false;
    }

}
