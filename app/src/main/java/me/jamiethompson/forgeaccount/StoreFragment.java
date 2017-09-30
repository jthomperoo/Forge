package me.jamiethompson.forgeaccount;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoreFragment extends Fragment implements ListView.OnItemClickListener, ReloadInterface
{

	private View mView;
	private List<ForgeAccount> mAccounts;
	private ListView mAccountList;

	public StoreFragment()
	{
		// Required empty public constructor
	}

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
		mAccounts = new ArrayList<>();
		mAccountList = mView.findViewById(R.id.account_list);
		mAccountList.setOnItemClickListener(this);
		setUpList();
		load(getActivity());
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	{
		ForgeAccount account = mAccounts.get(i);
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
		AccountListAdapter adapter = new AccountListAdapter(getActivity().getApplicationContext(), R.layout.item_account, mAccounts, getActivity(), this);
		mAccountList.setAdapter(adapter);
	}


}
