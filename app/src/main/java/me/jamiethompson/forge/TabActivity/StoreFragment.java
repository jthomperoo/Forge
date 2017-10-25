package me.jamiethompson.forge.TabActivity;


import android.app.Activity;
import android.content.Context;
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

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Files.FileManager;
import me.jamiethompson.forge.Interfaces.ReloadInterface;
import me.jamiethompson.forge.ListView.AccountListAdapter;
import me.jamiethompson.forge.R;


/**
 * Storage Fragment handles displaying the account storage to the user
 */
public class StoreFragment extends Fragment implements ListView.OnItemClickListener, ReloadInterface {
    // Parent activity
    private Activity activity;
    // Fragment context
    private Context context;
    // Fragment view
    private View view;
    // Storage map
    private HashMap<UUID, ForgeAccount> accounts;
    // Accounts list view
    private ListView accountList;

    /**
     * Generates a new Store Fragment instance
     *
     * @return new store fragment instance
     */
    public static StoreFragment newInstance() {
        return new StoreFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_store, container, false);
        this.view = view;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        accounts = new HashMap<>();
        accountList = this.view.findViewById(R.id.account_list);
        accountList.setOnItemClickListener(this);
        activity = getActivity();
        context = getContext();
        setUpList();
        load();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ForgeAccount account = (ForgeAccount) adapterView.getItemAtPosition(i);
        ((Forge) activity).loadAccount(account);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        view = null;
    }


    @Override
    public void reload() {
        // Refresh and load the storage
        load();
    }

    /**
     * Refreshes and loads the storage from local storage
     */
    public void load() {
        accounts = FileManager.load(activity);
        if (accountList != null) {
            // If the account list exists, display this in the UI
            setUpList();
        }
    }

    /**
     * Set up list view UI to show the stored accounts
     */
    public void setUpList() {
        if (!accounts.isEmpty()) {
            // If there are accounts stored, remove all existing items from the list view
            accountList.setAdapter(null);
        }
        // Create an account list adapter with the loaded accounts and assign it to the list view
        AccountListAdapter adapter = new AccountListAdapter(
                context,
                R.layout.item_account,
                new ArrayList<>(accounts.values()),
                activity,
                this);
        accountList.setAdapter(adapter);
    }


}
