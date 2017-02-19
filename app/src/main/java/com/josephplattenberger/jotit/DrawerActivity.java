package com.josephplattenberger.jotit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class DrawerActivity extends Activity {
	private int drawerPosition;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawer);
	}
	public void setSelected(int position) {
		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);
		// store position of drawer item selected so selecting same item doesnt reload activity
		drawerPosition = position;
	}

	public void set(String[] navMenuTitles, TypedArray navMenuIcons) {
		mTitle = getTitle();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();

		// adding nav drawer items
		if (navMenuIcons == null) {
			for (String navMenuTitle : navMenuTitles) {
				navDrawerItems.add(new NavDrawerItem(navMenuTitle));
			}
		} else {
			for (int i = 0; i < navMenuTitles.length; i++) {
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[i],
						navMenuIcons.getResourceId(i, -1)));
			}
		}

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		try {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		} catch (NullPointerException e){
			Log.e("ActionBar Back Button", e.getMessage());
		}
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				null, // nav menu toggle icon
				R.string.app_name,
				R.string.app_name
		// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(R.string.app_name);
			}
		};
		mDrawerLayout.addDrawerListener(mDrawerToggle);

	}

	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	// Called when invalidateOptionsMenu() is triggered
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	// Diplaying fragment view for selected nav drawer list item
	private void displayView(int position) {

		switch (position) {
		case 0:
			if (position != drawerPosition) {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			}
			break;
		case 1:
			if (position != drawerPosition) {
				Intent intent1 = new Intent(this, GroupsActivity.class);
				startActivity(intent1);
				finish();
			}
			break;
		case 2:
			if (position != drawerPosition) {
				Intent intent2 = new Intent(this, EditThemesActivity.class);
				startActivity(intent2);
				finish();
			}
		    break;
		case 3:
			if (position != drawerPosition) {
				Intent intent3 = new Intent(this, AboutActivity.class);
				startActivity(intent3);
				finish();
			}
		    break;
		default:
			break;
		}
		// Update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;

		try {
			getActionBar().setTitle(mTitle);
		} catch (NullPointerException e){
			Log.e("Null Title - Action Bar", e.getMessage());
		}
	}

	public void closeDrawer(){
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}
	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}