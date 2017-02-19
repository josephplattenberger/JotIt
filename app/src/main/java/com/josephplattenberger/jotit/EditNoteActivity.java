package com.josephplattenberger.jotit;

import info.android.sqlite.model.Note;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditNoteActivity extends Activity implements DeleteDialog.DeleteListener,
		ChangeGroupDialog.UpdateListener{

	final Context context = this;
	// Note attributes
    boolean storedNote = false;
	private int noteID;
	private String subject, mGroup, originalGroup, mNoteBody;
	private int mBackColor, mTextColor, mHighColor;
	// Views and Layouts and Dialogs
	private EditText bodyView, subjectView;
	private TextView dateText;
	private RelativeLayout mNoteLayout;
	private DeleteDialog deleteDialog;
	private ChangeGroupDialog changeGroupDialog;
	// Database
	private JotIt jotIt;
	// Alarm attributes -- future update
	// private int mHour, mMin, mYear, mMonth, mDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_note);
		jotIt = ((JotIt)getApplicationContext());
		try {
			getActionBar().setIcon(R.drawable.ic_action_edit);
		} catch (NullPointerException e){
			Log.e("Null Home Icon",e.getMessage());
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Initialize Views and Layout
    	subjectView = (EditText) findViewById(R.id.subjectEditText);
		dateText = (TextView) findViewById(R.id.dateText);
    	mNoteLayout = (RelativeLayout) findViewById(R.id.mNoteLayout);
		bodyView = (EditText) findViewById(R.id.bodyEditText);
		// Autolink all (phone, map, email, website)
		bodyView.setMovementMethod(LinkMovementMethod.getInstance());
		handleAddingLinks();
		// get open bundle, set links, set colors
		openBundle();
		Linkify.addLinks(bodyView, Linkify.ALL);
		updateColors();

		handleDeleteButton();
		handleEditTextViewsTouchListeners();
	}
	private void openBundle(){
		Bundle bundle = getIntent().getExtras();
		// if its a stored note or a new note from ViewGroupActivity
		if (bundle != null){
			if (bundle.getBoolean("stored_note")) {
				noteID = bundle.getInt("note_id");
				subject = bundle.getString("note_subject");
				// Fetch note attributes
				Cursor cursor = jotIt.mDBHelper.fetchNote(noteID);
				cursor.moveToFirst();
				originalGroup = mGroup = cursor.getString(cursor.getColumnIndex("Group_Name"));
				mNoteBody = cursor.getString(cursor.getColumnIndex("Note_Text"));
				String mDate = cursor.getString(cursor.getColumnIndex("Updated_Date_Time"));
				cursor.close();
				// Set text attributes
				dateText.setText(mDate);
				subjectView.setText(subject);
				bodyView.setText(mNoteBody);
				storedNote = true;
			} else {
				// New note from ViewGroupActivity
				originalGroup = mGroup = bundle.getString("group_name");
			}
			// Set color attributes
			mBackColor = bundle.getInt("backColor");
			mTextColor = bundle.getInt("textColor");
			mHighColor = bundle.getInt("highColor");
		} else {
			Cursor cursor = jotIt.mDBHelper.fetchTheme("Default");
			cursor.moveToFirst();
			mBackColor = cursor.getInt(0);
			mTextColor = cursor.getInt(1);
			mHighColor = cursor.getInt(2);
		}
	}

	private void handleAddingLinks(){
		bodyView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				Linkify.addLinks(s, Linkify.ALL);
			}
		});
	}

	private void updateColors(){
		mNoteLayout.setBackgroundColor(mBackColor);
		subjectView.setBackgroundColor(mBackColor);
		subjectView.setTextColor(mTextColor);
		subjectView.setHighlightColor(mHighColor);
		bodyView.setBackgroundColor(mBackColor);
		bodyView.setTextColor(mTextColor);
		bodyView.setHighlightColor(mHighColor);
		dateText.setTextColor(mTextColor);
	}

	private void handleDeleteButton(){
		// Initialize and handle delete button
		Button deleteButton = (Button) findViewById(R.id.trashButton);
		deleteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				FragmentManager fm = getFragmentManager();
				deleteDialog = new DeleteDialog();
				deleteDialog.show(fm, "Delete Dialog");
			}
		});
	}

	private void handleEditTextViewsTouchListeners (){
		// Calls Context Action Bar for check button that will
		subjectView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startActionMode(new ActionModeCallback());
				return false;
			}
		});

		bodyView.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startActionMode(new ActionModeCallback());
				return false;
			}
		});
	}

	private void saveNote(){
	 	final EditText bodyView = (EditText) findViewById(R.id.bodyEditText);
	 	final EditText subjectView = (EditText) findViewById(R.id.subjectEditText);
         //if editing stored note, not new note
		 Note mDBNote;
		 if (storedNote){
			//if the note has changed update it
			if (!subjectView.getText().toString().equals(subject) ||
					!bodyView.getText().toString().equals(mNoteBody) ||
					!originalGroup.equals(mGroup)) {
				mDBNote = new Note(subjectView.getText().toString(), mGroup,
						bodyView.getText().toString());
				jotIt.mDBHelper.updateNote(mDBNote, noteID);
			}
		//else if new note and has a subject
	 	} else if (!subjectView.getText().toString().equals("")){
			//and if group never changed
			if (mGroup == null) {
				mDBNote = new Note(subjectView.getText().toString(), "Other Notes",
						bodyView.getText().toString());
				originalGroup = mGroup = "Other Notes";
			//else if group did change
			} else {
				mDBNote = new Note(subjectView.getText().toString(), mGroup,
						bodyView.getText().toString());
			}
	 		jotIt.mDBHelper.insertNote(mDBNote);
            storedNote = true;
        }
	 }

	private void deleteNote(){
         final EditText subjectView = (EditText) findViewById(R.id.subjectEditText);
          //if editing stored note, not new note
		 if (storedNote) {
             jotIt.mDBHelper.deleteNote(noteID);
         }
		 // Set subject to nothing so new note is not created
		 // this is because saveNote() will be called on finish
         subjectView.setText("");
         finish();
	 }

	public void delete(){
		deleteDialog.dismiss();
		deleteNote();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar items
		switch (item.getItemId()){
			case android.R.id.home:
				saveNote();
            	finish();
            	return true;
			case R.id.change_group:
				changeGroup();
				return true;
			/******************************************************************
			 * Alarm -- future update
			 * case R.id.add_reminder:
			 * mNoteLayout = (RelativeLayout) findViewById(R.id.mNoteLayout);
			 * showDatePickerDialog(mNoteLayout);
			 * return true;
			 *****************************************************************/
        default:             
            return super.onOptionsItemSelected(item);
		}
	}

	public class ActionModeCallback implements ActionMode.Callback {
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.edit_note, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false;
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
				case R.id.change_group:
					changeGroup();
					return true;
				// future update
				/*case R.id.add_reminder:
				 * mNoteLayout = (RelativeLayout) findViewById(R.id.mNoteLayout);
				 * showDatePickerDialog(mNoteLayout);
				 * mode.finish(); // Action picked, so close the CAB
				 * return true;
	              */
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
			// hides keyboard
            final RelativeLayout mNoteLayout = (RelativeLayout) findViewById(R.id.mNoteLayout);
	    	InputMethodManager mgr = (InputMethodManager) getSystemService(
					Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(mNoteLayout.getWindowToken(), 0);
	    }
	}

	private void changeGroup(){
		FragmentManager fm = getFragmentManager();
		Cursor cursor = jotIt.mDBHelper.fetchGroupPreview();
		// Initialize dialog
		changeGroupDialog = new ChangeGroupDialog();
		changeGroupDialog.setValues(getBaseContext(), mGroup, cursor);
		changeGroupDialog.show(fm, "Change Group Dialog");
	}

	public void update(){
		mGroup = changeGroupDialog.getChangeGroup();
		// Get new group colors
		Cursor themeCursor = jotIt.mDBHelper.fetchAGroupThemeName(mGroup);
		themeCursor.moveToFirst();
		Cursor schemeCursor = jotIt.mDBHelper.fetchTheme(themeCursor.getString(0));
		schemeCursor.moveToFirst();
		mBackColor = schemeCursor.getInt(0);
		mTextColor = schemeCursor.getInt(1);
		mHighColor = schemeCursor.getInt(2);
		themeCursor.close();
		schemeCursor.close();
		// update UI, save, close dialog
		updateColors();
		saveNote();
		changeGroupDialog.dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_note, menu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public void onPause(){
		saveNote();
		super.onPause();
	}

	@Override
	public void onStop(){
		saveNote();
		super.onStop();
	}

	@Override
	public void onDestroy(){
		saveNote();
		super.onDestroy();
	}

	/*********************************************************************************************
	 *********************************************************************************************
	 * Alarm code -- future update
	 *******************************************************************************************
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day){
		mYear = year;
		mMonth = month;
		mDay = day;
		showTimePickerDialog(findViewById(R.id.mNoteLayout));
	}

	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getFragmentManager(), "timePicker");
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mHour = hourOfDay;
		mMin = minute;
		setupAlarm();
	}

	public void setupAlarm(){
		Calendar myCal = Calendar.getInstance();
		myCal.set(mYear, mMonth, mDay, mHour, mMin);

		Intent myIntent = new Intent(context, AlarmReceiver.class);
		myIntent.putExtra("note_id", noteID);
		myIntent.putExtra("note_subject", subjectView.getText().toString());
		myIntent.putExtra("note_body", bodyView.getText().toString());
		myIntent.putExtra("backColor", mBackColor);
		myIntent.putExtra("textColor", mTextColor);
		myIntent.putExtra("highColor", mHighColor);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, myIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager service = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		service.set(AlarmManager.RTC_WAKEUP, myCal.getTimeInMillis(), pending);
	}
	********************************************************************************************
	 *********************************************************************************************/
}

