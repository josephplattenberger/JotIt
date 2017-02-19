package com.josephplattenberger.jotit;

import info.android.sqlite.model.Theme;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
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

public class EditThemesActivity extends DrawerActivity implements DeleteDialog.DeleteListener{
	private Context context = this;
	private ActionMode mMode;
	// Adapter to display all themes to a grid
	private ThemeGridAdapter gridAdapter;
	private GridView gridview;
	private List<ThemeClass> mThemeList = new ArrayList<>();
	// Theme and Delete dialog attributes
	private Dialog mThemeDialog;
	private DeleteDialog deleteDialog;
	private String[] mColorNames;
    private int[] mColors;
    private String mChangedBack, mChangedText, mChangedHigh;
    private int mChangedBackColor, mChangedTextColor, mChangedHighColor;
	private boolean changeOnMultiselect;
	private boolean[] check = new boolean[3];
	private boolean editTextEmpty;
    // Database
	private JotIt jotIt;
	// Edit app theme attribute -- future update
	// private int changeAppColorPosition;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Get Database
		jotIt = ((JotIt)getApplicationContext());
		// Sets title of action bar
		setTitle("Themes");
		try {
			getActionBar().setIcon(R.drawable.ic_brush_grey600);
		} catch (NullPointerException e) {
			Log.e("Null ActionBar Icon", e.getMessage());
		}
		// Initialize grid
		setThemeList();
		getColors();
        setGrid();
		// Setup navigation drawer
		String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        set(navMenuTitles, navMenuIcons);
        setSelected(2);
	}
	private void setThemeList(){
		Cursor cursor = jotIt.mDBHelper.fetchThemePreview();
		cursor.moveToFirst();
		String themeName;
		int backColor;
		int textColor;
		int highColor;
		while(!cursor.isAfterLast()){
			themeName = cursor.getString(0);
			backColor = cursor.getInt(1);
			textColor = cursor.getInt(2);
			highColor = cursor.getInt(3);
			ThemeClass mTheme = new ThemeClass(themeName, backColor, textColor, highColor);
    		mThemeList.add(mTheme);
			cursor.moveToNext();
		}    	
		cursor.close();
    }

	private void getColors(){
		Cursor cursor = jotIt.mDBHelper.fetchColor();
		mColorNames = new String[cursor.getCount()+1];
		mColors = new int[cursor.getCount()+1];
		mColorNames[0] = "Choose a Color..";
		mColors[0] = 0xFFFFFFFF;
		int i = 1;
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			mColorNames[i] = cursor.getString(0);
			mColors[i] = cursor.getInt(1);
			i++;
			cursor.moveToNext();
		}
		cursor.close();
	}

	/* ************************************
	 * Pull up theme dialog
	 * if new theme position should be -1
	 **************************************/
	private void themeDialog(final int position){
		// Setup dialog and spinners
		mThemeDialog = new Dialog(context);
		mThemeDialog.setContentView(R.layout.make_new_theme);
		final Spinner backSpinner = (Spinner) mThemeDialog.findViewById(R.id.newThemeBackSpinner);
		SpinnerAdapter backAdapter = new SpinnerAdapter(getBaseContext(), mColors,
				mColorNames);
		backSpinner.setAdapter(backAdapter);
		final Spinner textSpinner = (Spinner) mThemeDialog.findViewById(R.id.newThemeTextSpinner);
		SpinnerAdapter textAdapter = new SpinnerAdapter(getBaseContext(), mColors,
				mColorNames);
		textSpinner.setAdapter(textAdapter);
		final Spinner highSpinner = (Spinner) mThemeDialog.findViewById(R.id.newThemeHighSpinner);
		SpinnerAdapter highAdapter = new SpinnerAdapter(getBaseContext(), mColors,
				mColorNames);
		highSpinner.setAdapter(highAdapter);
		// Initialize accept button so it can be hidden
		final Button mAcceptButton = (Button) mThemeDialog.findViewById(R.id.newThemeAcceptButton);

		final ThemeClass mTheme;
		EditText mEditText = (EditText) mThemeDialog.findViewById(R.id.newThemeNameEditText);
		// if existing theme, set attributes
		if (position > -1) {
			mTheme = mThemeList.get(position);
			mThemeDialog.setTitle("Edit " + mTheme.getName() + " Theme");
			mEditText.setText(mTheme.getName());
			if (mTheme.getName().equals("Default")){
				mEditText.setVisibility(View.GONE);
			}
			Cursor themeColorCursor = jotIt.mDBHelper.fetchThemeScheme(mTheme.getName());
			themeColorCursor.moveToFirst();
			String mOldBackColor = themeColorCursor.getString(0);
			String mOldTextColor = themeColorCursor.getString(1);
			String mOldHighColor = themeColorCursor.getString(2);
			themeColorCursor.close();
			backSpinner.setSelection(backAdapter.getPosition(mOldBackColor));
			textSpinner.setSelection(textAdapter.getPosition(mOldTextColor));
			highSpinner.setSelection(highAdapter.getPosition(mOldHighColor));
			editTextEmpty = false;
		} else {
			mTheme = null;
			mThemeDialog.setTitle("New Theme");
			backSpinner.setSelection(0);
			textSpinner.setSelection(0);
			highSpinner.setSelection(0);
			editTextEmpty = true;
			mAcceptButton.setVisibility(View.INVISIBLE);
		}

		backSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				backSpinner.setBackgroundColor(mColors[position]);
				if (position != 0){
					check[0] = true;
					setAcceptButtonVisibility();
				}else{
					check[0] = false;
					setAcceptButtonVisibility();
				}
				mChangedBack = backSpinner.getItemAtPosition(position).toString();
				mChangedBackColor = mColors[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		textSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				textSpinner.setBackgroundColor(mColors[position]);
				if (position != 0){
					check[1] = true;
					setAcceptButtonVisibility();
				}else{
					check[1] = false;
					setAcceptButtonVisibility();
				}
				mChangedText = textSpinner.getItemAtPosition(position).toString();
				mChangedTextColor = mColors[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		highSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				highSpinner.setBackgroundColor(mColors[position]);
				if (position != 0){
					check[2] = true;
					setAcceptButtonVisibility();
				}else{
					check[2] = false;
					setAcceptButtonVisibility();
				}
				mChangedHigh = highSpinner.getItemAtPosition(position).toString();
				mChangedHighColor = mColors[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		// Checks if group edit text is empty
		mEditText.addTextChangedListener(new TextWatcher() {
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
				EditText mEditText = (EditText) mThemeDialog.findViewById(
						R.id.newThemeNameEditText);
				// Copy all theme names to a list
				ArrayList<String> themes = new ArrayList<>();
				for (ThemeClass t : mThemeList) {
					themes.add(t.getName());
				}
				// if existing theme, remove it from the list
				if (mTheme != null) {
					themes.remove(mTheme.getName());
				}
				// if new theme name already exists do nothing
				if (themes.contains(mEditText.getText().toString())) {
					Toast.makeText(getBaseContext(), "Theme Already Exists",
							Toast.LENGTH_SHORT).show();
				} else {
					Theme mDBTheme = new Theme(mEditText.getText().toString(), mChangedBack,
							mChangedText, mChangedHigh);
					// if existing theme then update, else insert
					if (mTheme != null) {
						gridAdapter.remove(mTheme);
						jotIt.mDBHelper.updateTheme(mDBTheme, mTheme.getName());
					} else {
						jotIt.mDBHelper.insertTheme(mDBTheme);
					}
					// Add new theme to grid, exit dialog
					ThemeClass mNewTheme = new ThemeClass(mEditText.getText().toString(),
							mChangedBackColor, mChangedTextColor, mChangedHighColor);
					gridAdapter.add(mNewTheme);
					mThemeDialog.dismiss();
				}
			}
		});
		mThemeDialog.show();
	}

	private void setAcceptButtonVisibility(){
		Button mAcceptButton = (Button) mThemeDialog.findViewById(R.id.newThemeAcceptButton);
		if (check[0] && check[1] && check[2] && !editTextEmpty){
			mAcceptButton.setVisibility(View.VISIBLE);
		} else {
			mAcceptButton.setVisibility(View.INVISIBLE);
		}
	}

	private void setGrid(){
		gridview = (GridView) findViewById(R.id.gridview);
        gridAdapter = new ThemeGridAdapter(this, mThemeList);
        gridview.setAdapter(gridAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				themeDialog(position);
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
					mThemeList.get(position).setBackgroundColor(
							mThemeList.get(position).getHighlightColor());
				else
					mThemeList.get(position).setBackgroundColor(
							mThemeList.get(position).getBackgroundColorCopy());
				// Capture total checked items
				final int checkedCount = gridview.getCheckedItemCount();
				// Set the Context ActionBar (Multi-select mode bar) title to total checked items
				mode.setTitle(checkedCount + " Selected");
				gridAdapter.toggleSelection(mThemeList.get(position));
			}
 
			@Override
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
					List<ThemeClass> selected = gridAdapter.getSelectedIds();
					for (int i = 0; i < selected.size(); i++) {
						ThemeClass selecteditem = selected.get(i);
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
		// Get selected themes to delete
		List<ThemeClass> selected = gridAdapter.getSelectedIds();
		// Delete and remove each selected theme using a loop
		for (int i = 0; i < selected.size(); i++) {
			ThemeClass selectedItem = selected.get(i);
			String mThemeName = selectedItem.getName();
			// if the selected theme is not the default then delete,
			// else don't delete
			if (!mThemeName.equals("Default")) {
				jotIt.mDBHelper.deleteTheme(mThemeName);
				gridAdapter.remove(selectedItem);
			} else {
				mThemeList.get(mThemeList.indexOf(selectedItem))
						.setBackgroundColor(
								mThemeList.get(mThemeList.indexOf(selectedItem))
										.getBackgroundColorCopy());
				Toast.makeText(getBaseContext(), "Cannot Delete Default Theme",
						Toast.LENGTH_LONG).show();
			}
		}
		// Context ActionBar (Multi-select mode bar)
		changeOnMultiselect = true;
		mMode.finish();
		deleteDialog.dismiss();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_themes, menu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_theme:
				themeDialog(-1);
        		return true;
			/*************************************************************************************
			 * Edit app theme code -- future update
			 * case R.id.edit_app_theme:
			 * 		appThemeDialog();
			 * 		return true;
			 ***********************************************************************************/
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/*****************************************************************************************
	 *  Future update
	 *
	private void appThemeDialog(){
		final Dialog appThemeDialog = new Dialog(context);
		appThemeDialog.setTitle("Change App Background");
		appThemeDialog.setContentView(R.layout.change_app_theme);

		final Button appThemeAcceptButton = (Button) appThemeDialog.findViewById(
				R.id.appThemeAcceptButton);

		final Spinner colorSpinner = (Spinner) appThemeDialog.findViewById(
				R.id.appThemeSpinner);
		SpinnerAdapter colorAdapter = new SpinnerAdapter(getBaseContext(), mColors,
				mColorNames);
		colorSpinner.setAdapter(colorAdapter);

		colorSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				if (position != 0){
					appThemeAcceptButton.setVisibility(View.VISIBLE);
				}else{
					appThemeAcceptButton.setVisibility(View.GONE);
				}
				changeAppColorPosition = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				appThemeAcceptButton.setVisibility(View.GONE);

			}

		});

		appThemeAcceptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int appColor = mColors[changeAppColorPosition];
				gridview.setBackgroundColor(appColor);
				appThemeDialog.dismiss();
			}
		});
		appThemeDialog.show();
	}
	 *******************************************************************************************/
}
