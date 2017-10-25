package me.jamiethompson.forge.TabActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.jamiethompson.forge.Constants.General;
import me.jamiethompson.forge.R;

/**
 * Created by jamie on 27/09/17.
 */

class ForgePagerAdapter extends FragmentPagerAdapter {
    private Context appContext;
    private StoreFragment storeFragment;
    private GeneratorFragment generatorFragment;

    ForgePagerAdapter(FragmentManager fm, Context appContext) {
        super(fm);
        this.appContext = appContext;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case General.GENERATE_TAB: {
                generatorFragment = GeneratorFragment.newInstance();
                return generatorFragment;
            }
            case General.STORE_TAB: {
                storeFragment = StoreFragment.newInstance();
                return storeFragment;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case General.GENERATE_TAB:
                return appContext.getString(R.string.tab_generate);
            case General.STORE_TAB:
                return appContext.getString(R.string.tab_store);
        }
        return null;
    }

    StoreFragment getStoreFragment() {
        return storeFragment;
    }

    GeneratorFragment getGeneratorFragment() {
        return generatorFragment;
    }
}
