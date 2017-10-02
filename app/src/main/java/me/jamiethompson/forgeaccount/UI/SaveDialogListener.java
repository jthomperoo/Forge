package me.jamiethompson.forgeaccount.UI;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;

import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.Files.FileManager;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.TabActivity.Forge;

/**
 * Created by jamie on 02/10/17.
 */

public class SaveDialogListener implements DialogInterface.OnClickListener
{

	private ForgeAccount mAccount;
	private Boolean mLoaded;
	private Activity mActivity;
	private View mView;


	public SaveDialogListener(ForgeAccount mAccount, Boolean mLoaded, Activity mActivity, View mView)
	{
		this.mAccount = mAccount;
		this.mLoaded = mLoaded;
		this.mActivity = mActivity;
		this.mView = mView;
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		switch (which)
		{
			case DialogInterface.BUTTON_POSITIVE:
			{
				ForgeAccount saveAccount = FileManager.replace(mActivity, mAccount);
				if (saveAccount != null)
				{
					mAccount = saveAccount;
					mLoaded = true;
					Feedback.displayMessage(mActivity.getString(R.string.message_account_saved), mView);
				}
				((Forge) mActivity).reloadSaveList();
				dialog.dismiss();
				break;
			}
			case DialogInterface.BUTTON_NEGATIVE:
			{
				ForgeAccount saveAccount = FileManager.add(mActivity, mAccount);
				if (saveAccount != null)
				{
					mAccount = saveAccount;
					mLoaded = true;
					Feedback.displayMessage(mActivity.getString(R.string.message_account_saved), mView);
				}
				((Forge) mActivity).reloadSaveList();
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
}
