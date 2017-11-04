package me.jamiethompson.forge.TabActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import me.jamiethompson.forge.Data.ForgeAccount;
import me.jamiethompson.forge.Preferences.Preferences;
import me.jamiethompson.forge.R;
import me.jamiethompson.forge.UI.Notifications;

/**
 * Main activity class, holding both the generator page and the storage page
 */
public class Forge extends AppCompatActivity {
    // Error log key
    final public static String ERROR_LOG = "error";
    // Fragment tab types
    final public static int GENERATE_TAB = 0;
    final public static int STORE_TAB = 1;
    // Fragment Pager Adapter for managing tab fragments
    private ForgePagerAdapter forgePagerAdapter;
    // View Pager for displaying fragments
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forge);
        // Open with keyboard hidden
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set up fragment pager adapter
        forgePagerAdapter = new ForgePagerAdapter(getSupportFragmentManager(), getApplicationContext());
        // Clear all scroll flags
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);
        // Set up viewpager
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(forgePagerAdapter);
        // Set up tab layout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        // Set up notification channel
        Notifications.setUpChannels(this);
        // Get shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Get activity starting intent
        Intent intent = getIntent();
        if (intent.hasExtra(Notifications.NOTIFICATION_NAVIGATION)) {
            // If the activity was opened from a notification click
            // Set the current fragment viewed to the one requested
            viewPager.setCurrentItem(intent.getIntExtra(Notifications.NOTIFICATION_NAVIGATION, GENERATE_TAB));
        }
        // If the user preferences have not been set yet, set them to defaults
        if (!sharedPref.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_general, true);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_password, true);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_date_of_birth, true);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(Notifications.NOTIFICATION_NAVIGATION)) {
            // If the activity was opened from a notification click
            // Set the current fragment viewed to the one requested
            viewPager.setCurrentItem(intent.getIntExtra(Notifications.NOTIFICATION_NAVIGATION, GENERATE_TAB));
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
            // If preferences button pressed, open preferences class
            startActivity(new Intent(this, Preferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Refreshes the storage fragment's account list
     */
    public void reloadSaveList() {
        // Reload list on storage fragment
        StoreFragment storage = forgePagerAdapter.getStoreFragment();
        storage.reload();
    }

    /**
     * Loads an account into the generator page
     * @param account the account to be loaded
     */
    public void loadAccount(ForgeAccount account) {
        GeneratorFragment generator = forgePagerAdapter.getGeneratorFragment();
        generator.load(account);
        viewPager.setCurrentItem(GENERATE_TAB);
    }


}
