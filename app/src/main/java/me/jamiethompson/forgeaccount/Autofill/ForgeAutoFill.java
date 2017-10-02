package me.jamiethompson.forgeaccount.Autofill;

import android.annotation.TargetApi;
import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;

import java.util.List;

/**
 * Created by jamie on 02/10/17.
 */

@TargetApi(Build.VERSION_CODES.O)
public class ForgeAutoFill extends AutofillService
{
	final String TARGET_ELEMENT_NAME = "EditText";

	@Override
	public void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, FillCallback fillCallback)
	{
	}

	@Override
	public void onSaveRequest(SaveRequest saveRequest, SaveCallback saveCallback)
	{

	}

	void identifyFields(AssistStructure.ViewNode node, List<AssistStructure.ViewNode> fields, List<String> identifiers)
	{
		if (node.getClassName().contains(TARGET_ELEMENT_NAME))
		{
			String viewId = node.getIdEntry();
			if (viewId != null)
			{
				boolean added = false;
				int i = 0;
				while (i < identifiers.size() && !added)
				{
					if (viewId.contains(identifiers.get(i)))
					{
						fields.add(node);
						return;
					}
				}
			}
		}
	}

}
