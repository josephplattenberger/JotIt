package com.josephplattenberger.jotit;

import info.android.sqlite.model.Group;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

public class ViewGroupActivity extends Activity implements DeleteDialog.DeleteListener,
		ChangeGroupDialog.UpdateListener{
	final Context context = this;
	private ActionMode mMode;
	// Adapter to display all notes to a grid
	private NotesGridAdapter gridAdapter;
	private GridView gridview;
	private List<NoteClass> mNoteList = new ArrayList<>();
	// Edit group and change group dialog attributes
	private Dialog editGroupDialog;
	private String mChangeTheme;
	private boolean changeOnMultiselect;
	private boolean editTextEmpty;
	private boolean themeIsChosen;
    private String mGroupName;
	private DeleteDialog deleteDialog;
	private ChangeGroupDialog changeGroupDialog;
	// Database
    private JotIt jotIt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			getActionBar().setIcon(R.drawable.ic_action_group_dark);
		} catch (NullPointerException e){
			Log.e("Null ActionBar Icon", e.getMessage());
		}
		// Get database
		jotIt = ((JotIt)getApplicationContext());
		Bundle bundle = getIntent().getExtras();
		mGroupName = bundle.getString("group_name");
		// Set ActionBar Title
		setTitle(mGroupName);
		// Initialize grid, onResume() will add note list
		setGrid();
		// Action bar displays button to go back to GroupsActivity
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
	}

	@Override
	public void onResume(){
		super.onResume();
		setNoteList();
	}

	private void setNoteList(){
		gridAdapter.removeAll();
		Cursor cursor = jotIt.mDBHelper.fetchNotePreview(mGroupName, "UZA");
		cursor.moveToFirst();
		String subject;
		int noteID, backColor, textColor, highColor;
		while(!cursor.isAfterLast()){
			noteID = cursor.getInt(cursor.getColumnIndex("Note_id"));
			subject = cursor.getString(cursor.getColumnIndex("Subject"));
			backColor = cursor.getInt(cursor.getColumnIndex("Bcode"));
			textColor = cursor.getInt(cursor.getColumnIndex("Tcode"));
			highColor = cursor.getInt(cursor.getColumnIndex("Hcode"));
			NoteClass mNote = new NoteClass(noteID, subject, backColor, textColor, highColor);
    		mNoteList.add(mNote);
			cursor.moveToNext();
		}
	}
	private void setGrid(){
		gridview = (GridView) findViewById(R.id.gridview);
        gridAdapter = new NotesGridAdapter(this, mNoteList);
        gridview.setAdapter(gridAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Get note attributes and bundle to send to EditNoteActivity
				NoteClass mNote = mNoteList.get(position);
				int mNoteID = mNote.getNoteID();
				String mSubject =  mNote.getSubject();
				int mBackColor = mNote.getBackgroundColor();
				int mTextColor = mNote.getTextColor();
				int mHighColor = mNote.getHighlightColor();
				Intent myIntent = new Intent(context,EditNoteActivity.class);
				myIntent.putExtra("stored_note", true);
				myIntent.putExtra("note_id", mNoteID);
				myIntent.putExtra("note_subject", mSubject);
				myIntent.putExtra("backColor", mBackColor);
				myIntent.putExtra("textColor", mTextColor);
				myIntent.putExtra("highColor", mHighColor);
				startActivity(myIntent);
			}
        	
        });
		// Enable multi-select on a long press
        gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridview.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			// Inflate Context ActionBar (Multi-select mode bar)
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.maincab, menu);
				return true;
			}

			@Override
			// Toggle items during multi-select mode
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				if (checked)
				mNoteList.get(position).setBackgroundColor(
						mNoteList.get(position).getHighlightColor());
				else
					mNoteList.get(position).setBackgroundColor(
							mNoteList.get(position).getBackgroundColorCopy());
				// Capture total checked items
				final int checkedCount = gridview.getCheckedItemCount();
				// Set the Context ActionBar (Multi-select mode bar) title to total checked items
				mode.setTitle(checkedCount + " Selected");
				gridAdapter.toggleSelection(mNoteList.get(position));
			}
 
			@Override
			// Handle Context ActionBar (Multi-select mode bar) buttons
			public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
				mMode = mode;
				FragmentManager fm = getFragmentManager();
				switch (item.getItemId()) {
					case R.id.delete:
						deleteDialog = new DeleteDialog();
						deleteDialog.show(fm, "Delete Dialog");
						return true;
					case R.id.change_group:
						Cursor cursor = jotIt.mDBHelper.fetchGroupPreview();
						// Setup change group dialog
						changeGroupDialog = new ChangeGroupDialog();
						changeGroupDialog.setValues(getBaseContext(), mGroupName, cursor);
						changeGroupDialog.show(fm, "Change Group Dialog");
						return true;
					default:
						return false;
				}
			}

			@Override
			// Runs when closing Context ActionBar (Multi-select mode bar)
			public void onDestroyActionMode(ActionMode mode) {
				// if no change on Multiselect, change backgrounds back to original color
				if (!changeOnMultiselect){
					List<NoteClass> selected = gridAdapter.getSelectedIds();
					for (int i = 0; i < selected.size(); i++) {
						NoteClass selectedItem = selected.get(i);
							selectedItem.setBackgroundColor(
									selectedItem.getBackgroundColorCopy());
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
		List<NoteClass> selected = gridAdapter.getSelectedIds();
		// Delete and remove each selected note using a loop
		for (int i = 0; i < selected.size(); i++) {
			NoteClass selectedItem = selected.get(i);
			jotIt.mDBHelper.deleteNote(selectedItem.getNoteID());
			mNoteList.remove(selectedItem);
		}
		changeOnMultiselect = true;
		// Close Context ActionBar (Multi-select mode bar)
		mMode.finish();
		deleteDialog.dismiss();
	}

	public void update(){
		// Get selected notes to be updated
		List<NoteClass> selected = gridAdapter.getSelectedIds();
		// Update each selected note using a loop
		for (int i = 0; i < selected.size(); i++) {
			NoteClass selectedItem = selected.get(i);
			jotIt.mDBHelper.updateNoteMultiple(
					selectedItem.getNoteID(), changeGroupDialog.getChangeGroup());
			mNoteList.remove(selectedItem);
		}
		changeOnMultiselect = true;
		// Close Context ActionBar (Multi-select mode bar) and dialog
		mMode.finish();
		changeGroupDialog.dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_group, menu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.new_note:
			startNewNote();
        	return true;
        case R.id.edit_group:
			editGroupDialog();
        	return true;
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:             
            return super.onOptionsItemSelected(item);
            }
	}
	private void setAcceptButtonVisibility(){
		Button mAcceptButton = (Button) editGroupDialog.findViewById(
				R.id.newGroupAcceptButton);
		if (!editTextEmpty && themeIsChosen){
			mAcceptButton.setVisibility(View.VISIBLE);
		} else {
			mAcceptButton.setVisibility(View.INVISIBLE);
		}
	}

	private void startNewNote(){
		// Get group attributes, bundle, and start EditNoteActivity
		Cursor themeCursor = jotIt.mDBHelper.fetchAGroupThemeName(mGroupName);
		themeCursor.moveToFirst();
		Cursor colorCursor = jotIt.mDBHelper.fetchTheme(themeCursor.getString(0));
		colorCursor.moveToFirst();
		Intent myIntent = new Intent(this,EditNoteActivity.class);
		myIntent.putExtra("stored_note", false);
		myIntent.putExtra("backColor", colorCursor.getInt(0));
		myIntent.putExtra("textColor", colorCursor.getInt(1));
		myIntent.putExtra("highColor", colorCursor.getInt(2));
		myIntent.putExtra("group_name", mGroupName);
		colorCursor.close();
		themeCursor.close();
		startActivity(myIntent);
	}

	private void editGroupDialog(){
		// Setup edit group dialog
		editGroupDialog = new Dialog(context);
		editGroupDialog.setTitle("Edit " + mGroupName + " Group");
		editGroupDialog.setContentView(R.layout.make_new_group);
		EditText mEditText = (EditText) editGroupDialog.findViewById(R.id.newGroupEditText);
		mEditText.setText(mGroupName);
		if (mGroupName.equals("Other Notes")){
			mEditText.setVisibility(View.GONE);
		}
		// Initialize Accept Button so it can be hidden until a theme is chosen
		final Button mAcceptButton = (Button) editGroupDialog.findViewById(
				R.id.newGroupAcceptButton);
		// Populate lists (theme names, back & text colors) to send to theme spinner
		Cursor groupThemeCursor = jotIt.mDBHelper.fetchAGroupThemeName(mGroupName);
		groupThemeCursor.moveToFirst();
		final String mGroupTheme = groupThemeCursor.getString(0);
		groupThemeCursor.close();
		Cursor cursor = jotIt.mDBHelper.fetchAllThemeNames();
		final ArrayList<String> mThemes = new ArrayList<>();
		mThemes.add("Choose a Theme..");
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			mThemes.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		ArrayList<Integer> themeBackColors = new ArrayList<>();
		ArrayList<Integer> themeTextColors = new ArrayList<>();
		themeBackColors.add(0xFFFFFFFF);
		themeTextColors.add(0xFF000000);
		Cursor schemeCursor;
		boolean skippedFirst = false;
		for (String theme : mThemes){
			//skip "Choose a Theme" in list
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
		// Initialize theme spinner with lists
		SpinnerAdapter adapter = new SpinnerAdapter(getBaseContext(), mThemes,
				themeBackColors, themeTextColors);
		final Spinner mSpinner = (Spinner) editGroupDialog.findViewById(R.id.newGroupSpinner);
		mSpinner.setAdapter(adapter);
		int mSpinnerPos = adapter.getPosition(mGroupTheme);
		// Set to group's current theme
		mSpinner.setSelection(mSpinnerPos);

		editTextEmpty = false;
		themeIsChosen = true;

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				// if not "Choose Theme..." and editText is not empty then set accept visible
				if (position != 0){
					themeIsChosen = true;
					mChangeTheme = mThemes.get(position);
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
		// Handle Accept Button
		mAcceptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get all group names & check if a group with the same name already exists
				EditText mEditText = (EditText) editGroupDialog.findViewById(
						R.id.newGroupEditText);
				Cursor groupCursor = jotIt.mDBHelper.fetchAllGroupNames();
				groupCursor.moveToFirst();
				ArrayList<String> groups = new ArrayList<>();
				while (!groupCursor.isAfterLast()){
					groups.add(groupCursor.getString(0));
					groupCursor.moveToNext();
				}
				groupCursor.close();
				groups.remove(mGroupName);
				// if group with same name doesn't exist then insert, add, and close dialog
				if (groups.contains(mEditText.getText().toString())){
					Toast.makeText(getBaseContext(), "Group Already Exists",
							Toast.LENGTH_SHORT).show();
				} else {
					Group mDBGroup = new Group(mEditText.getText().toString(), mChangeTheme);
					jotIt.mDBHelper.updateGroup(mDBGroup, mGroupName);
					mGroupName = mEditText.getText().toString();
					setTitle(mGroupName);
					setNoteList();
					editGroupDialog.dismiss();
				}
			}
		});
		editGroupDialog.show();
	}
}
