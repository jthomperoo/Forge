package me.jamiethompson.forgeaccount.Autofill;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.assist.AssistStructure;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * Created by jamie on 02/10/17.
 */

public class ForgeAutoFill extends AccessibilityService
{
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event)
	{
		Log.v("mega", "type: " + event.getEventType());
		AccessibilityNodeInfo info = event.getSource();
		Log.v("mega", "id: " + info.getText());
		info = getRootInActiveWindow();
		Log.v("mega", "info: " + info.getText());
		String id = info.getViewIdResourceName();
		Log.v("mega", "id2: " + info.getText());
	}

	@Override
	public void onInterrupt()
	{
	}

}
