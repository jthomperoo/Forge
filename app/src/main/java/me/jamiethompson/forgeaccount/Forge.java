package me.jamiethompson.forgeaccount;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

public class Forge extends AppCompatActivity
{

	private ForgePagerAdapter mForgePagerAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forge);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		mForgePagerAdapter = new ForgePagerAdapter(getSupportFragmentManager(), getApplicationContext());

		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mForgePagerAdapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);


	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_forge, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.settings)
		{
			startActivity(new Intent(this, Preferences.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void reloadSaveList()
	{
		StoreFragment storage = mForgePagerAdapter.getStoreFragment();
		storage.reload(this);
	}

	public void loadAccount(ForgeAccount account)
	{
		GeneratorFragment generator = mForgePagerAdapter.getGeneratorFragment();
		generator.load(account);
		mViewPager.setCurrentItem(Constants.GENERATE_TAB);
	}

}
