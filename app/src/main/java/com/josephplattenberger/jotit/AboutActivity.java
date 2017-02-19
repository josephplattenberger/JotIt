package com.josephplattenberger.jotit;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;

public class AboutActivity extends DrawerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		// Sets title & icon of action bar
		setTitle("About");
		try {
			getActionBar().setIcon(R.drawable.ic_action_about_dark);
		} catch (NullPointerException e) {
			Log.e("Null ActionBar Icon", e.getMessage());
		}
		// Setup navigation drawer
		String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        set(navMenuTitles, navMenuIcons);
        setSelected(3);
	}
}
