package me.jamiethompson.forgeaccount.TabActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.jamiethompson.forgeaccount.Constants;
import me.jamiethompson.forgeaccount.R;

/**
 * Created by jamie on 27/09/17.
 */

public class ForgePagerAdapter extends FragmentPagerAdapter
{
	final int NUM_OF_TABS = 2;
	private Context appContext;
	private StoreFragment storeFragment;
	private GeneratorFragment generatorFragment;

	public ForgePagerAdapter(FragmentManager fm, Context appContext)
	{
		super(fm);
		this.appContext = appContext;
	}

	@Override
	public Fragment getItem(int position)
	{
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class below).
		switch (position)
		{
			case Constants.GENERATE_TAB:
			{
				generatorFragment = GeneratorFragment.newInstance();
				return generatorFragment;
			}
			case Constants.STORE_TAB:
			{
				storeFragment = StoreFragment.newInstance();
				return storeFragment;
			}
			default:
			{
				return null;
			}
		}
	}

	@Override
	public int getCount()
	{
		return NUM_OF_TABS;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		switch (position)
		{
			case Constants.GENERATE_TAB:
				return appContext.getString(R.string.tab_generate);
			case Constants.STORE_TAB:
				return appContext.getString(R.string.tab_store);
		}
		return null;
	}

	public StoreFragment getStoreFragment()
	{
		return storeFragment;
	}

	public GeneratorFragment getGeneratorFragment()
	{
		return generatorFragment;
	}
}
