package me.jamiethompson.forge.TabActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import me.jamiethompson.forge.Constants;
import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Preferences.Preferences;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Notifications;

public class Forge extends AppCompatActivity {

    private ForgePagerAdapter forgePagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forge);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        forgePagerAdapter = new ForgePagerAdapter(getSupportFragmentManager(), getApplicationContext());

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);  // clear all scroll flags

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(forgePagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Notifications.setUpChannels(this);
        Notifications.displayHelperNotification(this);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.NOTIFICATION_NAVIGATION)) {
            viewPager.setCurrentItem(intent.getIntExtra(Constants.NOTIFICATION_NAVIGATION, Constants.GENERATE_TAB));
        }

        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_general, true);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_password, true);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_date_of_birth, true);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(Constants.NOTIFICATION_NAVIGATION)) {
            viewPager.setCurrentItem(intent.getIntExtra(Constants.NOTIFICATION_NAVIGATION, Constants.GENERATE_TAB));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            startActivity(new Intent(this, Preferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reloadSaveList() {
        StoreFragment storage = forgePagerAdapter.getStoreFragment();
        storage.reload(this);
    }

    public void loadAccount(ForgeAccount account) {
        GeneratorFragment generator = forgePagerAdapter.getGeneratorFragment();
        generator.load(account);
        viewPager.setCurrentItem(Constants.GENERATE_TAB);
    }


}
