package com.josephplattenberger.jotit;

import info.android.sqlite.model.Group;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

public class GroupsActivity extends DrawerActivity implements DeleteDialog.DeleteListener{

	final Context context = this;
	private ActionMode mMode;
	// Adapter to display all groups to a grid
	private GroupGridAdapter gridAdapter;
	private GridView gridview;
	private List<GroupClass> mGroupList = new ArrayList<>();
    // "Choose Theme" & delete dialogs and spinner attributes
	private Dialog groupDialog;
	private DeleteDialog deleteDialog;
	private String chosenTheme;
	private boolean changeOnMultiselect;
	private boolean editTextEmpty;
	private boolean themeIsChosen;
    // Database
	private JotIt jotIt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Get Database
		jotIt = ((JotIt)getApplicationContext());
		// Sets title of action bar
		setTitle("Groups");
		try {
			getActionBar().setIcon(R.drawable.ic_action_group_dark);
		} catch (NullPointerException e){
			Log.e("Null ActionBar Icon", e.getMessage());
		}
		// Initialize grid and set group list
		setGroupList();
		setGrid();
		// Setup navigation drawer
        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        set(navMenuTitles, navMenuIcons);
        setSelected(1);
	}
	private void setGroupList(){
		Cursor cursor = jotIt.mDBHelper.fetchGroupPreview();
		cursor.moveToFirst();
		String groupName;
		int backColor, textColor, highColor;
		while (!cursor.isAfterLast()){
			groupName = cursor.getString(0);
			backColor = cursor.getInt(1);
			textColor = cursor.getInt(2);
			highColor = cursor.getInt(3);
    		GroupClass mGroup = new GroupClass(groupName, backColor, textColor, highColor);
    		mGroupList.add(mGroup);
    		cursor.moveToNext();
		}
		cursor.close();
    }

	private void setGrid(){
		gridview = (GridView) findViewById(R.id.gridview);
		gridAdapter = new GroupGridAdapter(this, mGroupList);
		gridview.setAdapter(gridAdapter);

		gridview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// Get group to be viewed
				GroupClass mGroup = mGroupList.get(position);
				// Bundle attributes and start ViewGroupActivity
				Intent myIntent = new Intent(context, ViewGroupActivity.class);
				myIntent.putExtra("group_name", mGroup.getName());
				context.startActivity(myIntent);
			}
		});
		// Enable multi-select on a long press
		gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		gridview.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			// Inflate Context ActionBar (Multi-select mode bar)
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.group_theme_cab, menu);
				return true;
			}

			@Override
			// Toggle items during multi-select mode
			public void onItemCheckedStateChanged(ActionMode mode,
												  int position, long id, boolean checked) {
				if (checked)
					mGroupList.get(position).setBackgroundColor(
							mGroupList.get(position).getHighlightColor());
				else
					mGroupList.get(position).setBackgroundColor(
							mGroupList.get(position).getBackgroundColorCopy());
				// Capture total checked items
				final int checkedCount = gridview.getCheckedItemCount();
				// Set the Context ActionBar (Multi-select mode bar) title to total checked items
				mode.setTitle(checkedCount + " Selected");
				gridAdapter.toggleSelection(mGroupList.get(position));
			}

			@Override
			// Handle Context ActionBar (Multi-select mode bar) buttons
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				mMode = mode;
				switch (item.getItemId()) {
					case R.id.delete:
						FragmentManager fm = getFragmentManager();
						deleteDialog = new DeleteDialog();
						deleteDialog.show(fm, "Delete Dialog");
						return true;
					default:
						return false;
				}
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// if no change on Multiselect, change backgrounds back to original color
				if (!changeOnMultiselect){
					List<GroupClass> selected = gridAdapter.getSelectedIds();
					for (int i = 0; i < selected.size(); i++) {
						GroupClass selecteditem = selected.get(i);
						selecteditem.setBackgroundColor(selecteditem.getBackgroundColorCopy());
					}
				}
				changeOnMultiselect = false;
				gridAdapter.removeSelection();
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

	public void delete(){
		// Get selected notes to be deleted
		List<GroupClass> selected = gridAdapter.getSelectedIds();
		// Delete and remove each selected group using a loop
		for (int i = 0; i < selected.size(); i++) {
			GroupClass selectedItem = selected.get(i);
			String mGroupName = selectedItem.getName();
			// if the selected group isn't the default then delete,
			// else don't delete
			if (!mGroupName.equals("Other Notes")) {
				jotIt.mDBHelper.deleteGroup(mGroupName);
				gridAdapter.remove(selectedItem);
			} else {
				mGroupList.get(mGroupList.indexOf(selectedItem))
						.setBackgroundColor(
								mGroupList.get(mGroupList.indexOf(selectedItem))
										.getBackgroundColorCopy());
				Toast.makeText(getBaseContext(),
						"Cannot Delete Other Notes Group",
						Toast.LENGTH_LONG).show();
			}
		}
		changeOnMultiselect = true;
		// Close Context ActionBar (Multi-select mode bar)
		mMode.finish();
		deleteDialog.dismiss();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.groups, menu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case R.id.new_group:
				newGroupDialog();
        	    return true;
            default:
		        return super.onOptionsItemSelected(item);
		}
	}

	private void newGroupDialog(){
		// Setup dialog and spinners
		groupDialog = new Dialog(context);
		groupDialog.setTitle("New Group");
		groupDialog.setContentView(R.layout.make_new_group);
		final EditText editText = (EditText) groupDialog.findViewById(
				R.id.newGroupEditText);
		final Spinner mSpinner = (Spinner) groupDialog.findViewById(R.id.newGroupSpinner);
		// Initialize accept button so it can be hidden
		final Button mAcceptButton = (Button) groupDialog.findViewById(
				R.id.newGroupAcceptButton);
		// Get all theme names
		Cursor themeCursor = jotIt.mDBHelper.fetchAllThemeNames();
		themeCursor.moveToFirst();
		ArrayList<String> mThemes = new ArrayList<>();
		mThemes.add("Choose a Theme..");
		while(!themeCursor.isAfterLast()){
			mThemes.add(themeCursor.getString(0));
			themeCursor.moveToNext();
		}
		themeCursor.close();
		// Get all theme colors
		ArrayList<Integer> themeBackColors = new ArrayList<>();
		ArrayList<Integer> themeTextColors = new ArrayList<>();
		themeBackColors.add(0xFFFFFFFF);
		themeTextColors.add(0xFF000000);
		Cursor schemeCursor;
		boolean skippedFirst = false;
		for (String theme : mThemes){
			// Skip "Choose a Theme" in list
			if (skippedFirst) {
				schemeCursor = jotIt.mDBHelper.fetchTheme(theme);
				schemeCursor.moveToFirst();
				themeBackColors.add(schemeCursor.getInt(0));
				themeTextColors.add(schemeCursor.getInt(1));
				schemeCursor.close();
			}else{
				skippedFirst = true;
			}
		}
		// Send lists (theme names and colors) to spinner
		SpinnerAdapter adapter = new SpinnerAdapter(getBaseContext(), mThemes,
				themeBackColors, themeTextColors);
		mSpinner.setAdapter(adapter);
		mSpinner.setSelection(0);

		editTextEmpty = true;
		themeIsChosen = false;
		mAcceptButton.setVisibility(View.INVISIBLE);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				if (position != 0){
					themeIsChosen = true;
					// Get chosen theme attributes and update theme preview
					chosenTheme = mSpinner.getItemAtPosition(position).toString();
					setAcceptButtonVisibility();
				}else{
					themeIsChosen = false;
					setAcceptButtonVisibility();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		// Checks if group edit text is empty
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				editTextEmpty = s.toString().equals("");
				setAcceptButtonVisibility();
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		// Handle accept button
		mAcceptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get all group names & check if a group with the same name already exists
				Cursor groupCursor = jotIt.mDBHelper.fetchAllGroupNames();
				groupCursor.moveToFirst();
				boolean groupExists = false;
				while (!groupCursor.isAfterLast()){
					if (groupCursor.getString(0).equals(editText.getText().toString())){
						Toast.makeText(getBaseContext(), "Group Already Exists",
								Toast.LENGTH_SHORT).show();
						groupExists = true;
					}
					groupCursor.moveToNext();
				}
				groupCursor.close();
				// if group with same name doesn't exist then insert, add, and close dialog
				if (!groupExists) {
					Group insertGroup = new Group(editText.getText().toString(), chosenTheme);
					jotIt.mDBHelper.insertGroup(insertGroup);
					Cursor themeCursor = jotIt.mDBHelper.fetchAGroupThemeName(
							insertGroup.getGroup_Name());
					themeCursor.moveToFirst();
					Cursor colorCursor = jotIt.mDBHelper.fetchTheme(themeCursor.getString(0));
					colorCursor.moveToFirst();

					GroupClass mGroup = new GroupClass(insertGroup.getGroup_Name(),
							colorCursor.getInt(0), colorCursor.getInt(1),
							colorCursor.getInt(2));
					colorCursor.close();
					themeCursor.close();
					gridAdapter.add(mGroup);
					groupDialog.dismiss();
				}
			}

		});
		groupDialog.show();
	}

	private void setAcceptButtonVisibility(){
		Button mAcceptButton = (Button) groupDialog.findViewById(
				R.id.newGroupAcceptButton);
		if (!editTextEmpty && themeIsChosen){
			mAcceptButton.setVisibility(View.VISIBLE);
		} else {
			mAcceptButton.setVisibility(View.INVISIBLE);
		}
	}
}
