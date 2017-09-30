package me.jamiethompson.forgeaccount;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by jamie on 27/09/17.
 */

public class ForgePagerAdapter extends FragmentPagerAdapter
{
	final int NUM_OF_TABS = 2;
	private Context mAppContext;
	private StoreFragment mStoreFragment;
	private GeneratorFragment mGeneratorFragment;

	public ForgePagerAdapter(FragmentManager fm, Context mAppContext)
	{
		super(fm);
		this.mAppContext = mAppContext;
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
				mGeneratorFragment = GeneratorFragment.newInstance();
				return mGeneratorFragment;
			}
			case Constants.STORE_TAB:
			{
				mStoreFragment = StoreFragment.newInstance();
				return mStoreFragment;
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
				return mAppContext.getString(R.string.tab_generate);
			case Constants.STORE_TAB:
				return mAppContext.getString(R.string.tab_store);
		}
		return null;
	}

	public StoreFragment getStoreFragment()
	{
		return mStoreFragment;
	}

	public GeneratorFragment getGeneratorFragment()
	{
		return mGeneratorFragment;
	}
}
