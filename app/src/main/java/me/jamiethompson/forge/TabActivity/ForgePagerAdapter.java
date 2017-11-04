package me.jamiethompson.forge.TabActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.jamiethompson.forge.R;

/**
 * Created by jamie on 27/09/17.
 * FragmentPagerAdapter for handling tab fragments within the main Forge activity
 */

class ForgePagerAdapter extends FragmentPagerAdapter {
    // Application context
    private Context appContext;
    // Storage Fragment
    private StoreFragment storeFragment;
    // Generator Fragment
    private GeneratorFragment generatorFragment;

    /**
     * @param fm fragment manager
     * @param appContext application context
     */
    ForgePagerAdapter(FragmentManager fm, Context appContext) {
        super(fm);
        this.appContext = appContext;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page
        switch (position) {
            case Forge.GENERATE_TAB: {
                generatorFragment = GeneratorFragment.newInstance();
                return generatorFragment;
            }
            case Forge.STORE_TAB: {
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
            case Forge.GENERATE_TAB:
                return appContext.getString(R.string.tab_generate);
            case Forge.STORE_TAB:
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
