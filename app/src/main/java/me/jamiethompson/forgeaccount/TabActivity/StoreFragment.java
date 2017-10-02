package me.jamiethompson.forgeaccount.TabActivity;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import me.jamiethompson.forgeaccount.Data.ForgeAccount;
import me.jamiethompson.forgeaccount.Files.FileManager;
import me.jamiethompson.forgeaccount.ListView.AccountListAdapter;
import me.jamiethompson.forgeaccount.R;
import me.jamiethompson.forgeaccount.ReloadInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoreFragment extends Fragment implements ListView.OnItemClickListener, ReloadInterface
{

	private View mView;
	private HashMap<UUID,ForgeAccount> mAccounts;
	private ListView mAccountList;

	public static StoreFragment newInstance()
	{
		StoreFragment fragment = new StoreFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_store, container, false);
		this.mView = view;
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		mAccounts = new HashMap<>();
		mAccountList = mView.findViewById(R.id.account_list);
		mAccountList.setOnItemClickListener(this);
		setUpList();
		load(getActivity());
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	{
		ForgeAccount account = (ForgeAccount) adapterView.getItemAtPosition(i);
		((Forge) getActivity()).loadAccount(account);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mView = null;
	}


	@Override
	public void reload(Activity activity)
	{
		load(activity);
	}

	public void load(Activity activity)
	{
		mAccounts = FileManager.load(activity);
		if(mAccountList != null)
		{
			setUpList();
		}
	}

	public void setUpList()
	{
		if (!mAccounts.isEmpty())
		{
			mAccountList.setAdapter(null);
		}
		AccountListAdapter adapter = new AccountListAdapter(getActivity().getApplicationContext(), R.layout.item_account, new ArrayList<>(mAccounts.values()), getActivity(), this);
		mAccountList.setAdapter(adapter);
	}


}
