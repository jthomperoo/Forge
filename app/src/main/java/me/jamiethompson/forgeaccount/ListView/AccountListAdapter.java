package me.jamiethompson.forgeaccount.ListView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.jamiethompson.forgeaccount.Files.FileManager;
import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.ReloadInterface;

/**
 * Created by jamie on 29/09/17.
 */

public class AccountListAdapter extends ArrayAdapter<ForgeAccount>
{

	private Activity mActivity;
	private ReloadInterface mListFragment;

	public AccountListAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
	}

	public AccountListAdapter(Context context, int resource, List<ForgeAccount> accounts, Activity activity, ReloadInterface listFragment)
	{
		super(context, resource, accounts);
		mActivity = activity;
		mListFragment = listFragment;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{

		View v = convertView;

		if (v == null)
		{
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.item_account, null);
		}

		final ForgeAccount account = getItem(position);

		if (account != null)
		{
			TextView subject = v.findViewById(R.id.name);
			v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							switch (which)
							{
								case DialogInterface.BUTTON_POSITIVE:
									FileManager.delete(mActivity, account);
									mListFragment.reload(mActivity);
									dialog.dismiss();
									break;
								case DialogInterface.BUTTON_NEGATIVE:
									dialog.dismiss();
									break;
							}
						}
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setMessage(mActivity.getString(R.string.dialog_delete))
							.setPositiveButton(mActivity.getString(R.string.option_delete), dialogClickListener)
							.setNegativeButton(mActivity.getString(R.string.option_cancel), dialogClickListener).show();
				}
			});

			if (subject != null)
			{
				String accountTitle = account.getAccountName();
				if (accountTitle.isEmpty())
				{
					accountTitle = account.getEmail().getAddress();
				}
				subject.setText(accountTitle);
			}
		}

		return v;
	}

}
