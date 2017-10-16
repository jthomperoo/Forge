package me.jamiethompson.forgeaccount.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.Files.CurrentManager;

/**
 * Created by jamie on 09/10/17.
 */

public class AccessibilityAutofillService extends AccessibilityService {
    public static AccessibilityAutofillService instance;
    private ArrayList<AccessibilityNodeInfo> textViewNodes;
    private ArrayList<AccessibilityNodeInfo> validNodes;

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
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED: {
                validNodes = new ArrayList<>();
                break;
            }
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                textViewNodes = new ArrayList<>();

                findChildViews(rootNode);

                for (AccessibilityNodeInfo node : textViewNodes) {
                    /**
                     * For some reason, get input type seems to return the input type integer + 1, which
                     * is why I am subtracting 1 from the value, strange
                     */
                    switch (node.getInputType() - 1) {
                        case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        case InputType.TYPE_NUMBER_VARIATION_PASSWORD:
                        case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                        case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                            validNodes.add(node);
                            break;
                        case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
                            validNodes.add(node);
                            break;
                        case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                        case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                            validNodes.add(node);
                            break;
                    }
                }
            }
        }
    }

    private void findChildViews(AccessibilityNodeInfo parentView) {
        if (parentView != null) {
            int childCount = parentView.getChildCount();

            if (parentView == null || parentView.getClassName() == null) {
                return;
            }
            String classname = parentView.getClassName().toString();
            if (childCount == 0 && (classname.contentEquals("android.widget.EditText"))) {
                textViewNodes.add(parentView);
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
        for (int i = 0; i < validNodes.size(); i++) {
            String input = getInput(validNodes.get(i).getInputType(), account);
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, input);
            validNodes.get(i).performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT.getId(), arguments);
        }
    }

    private String getInput(int type, ForgeAccount account) {
        /**
         * For some reason, get input type seems to return the input type integer + 1, which
         * is why I am subtracting 1 from the value, strange
         */
        switch (type - 1) {
            case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
            case InputType.TYPE_NUMBER_VARIATION_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                return account.getPassword();
            case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
                return account.getUsername();
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                return account.getEmail().getAddress();
            default:
                return "";
        }
    }

}
