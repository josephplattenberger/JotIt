package com.josephplattenberger.jotit;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;


public class MainActivity extends DrawerActivity implements DeleteDialog.DeleteListener,
        ChangeGroupDialog.UpdateListener{
	final Context context = this;
    // Adapter to display all notes to a grid
	private NotesGridAdapter gridAdapter;
    // Adapter to display searched notes to a grid
    private MyCursorAdapter searchAdapter;
	private GridView gridview;
    private SearchView searchView;
    private ActionMode mMode;
    private DeleteDialog deleteDialog;
    private ChangeGroupDialog changeGroupDialog;
    private String searchString;
	private boolean changeOnMultiselect = false;
    private boolean searching = false;
    private String orderBy = "UZA";
    // Actionbar "order by" spinner items
	private String[] actions = new String[] {
			"Newest", "Oldest", "A-Z", "Z-A", "Group"
	    };
	private List<NoteClass> mNoteList = new ArrayList<>();
    private List<NoteClass> searchedNoteList = new ArrayList<>();
    // Database
    private JotIt jotIt;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            getActionBar().setIcon(R.drawable.ic_home_grey600);
        } catch (NullPointerException e){
            Log.e("Null ActionBar Icon", e.getMessage());
        }
        // Get database
        jotIt = ((JotIt)getApplicationContext());
        jotIt.mDBHelper = new DatabaseHelper(getApplicationContext());
        // Sets title of action bar
        setTitle("All Notes");
        // Setup navigation drawer
        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        set(navMenuTitles, navMenuIcons);
        setSelected(0);
        // Initialize grid of icons before adding items
        setGrid();
        // Setup actionbar "order by" spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
				android.R.layout.simple_spinner_dropdown_item, actions);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Setup actionbar "order by" spinner listener, auto enters case 0 and sets note list
        ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                closeDrawer();
                switch (itemPosition){
                    case 0:
                        orderBy = "UZA";
                        setNoteList("UZA");
                        Toast.makeText(getBaseContext(), "Sorting by Newest",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    case 1:
                        orderBy = "UAZ";
                        setNoteList("UAZ");
                        Toast.makeText(getBaseContext(), "Sorting by Oldest",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    case 2:
                        orderBy = "AZ";
                	    setNoteList("AZ");
                        Toast.makeText(getBaseContext(), "Sorting Alphabetically",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    case 3:
                        orderBy = "ZA";
                        setNoteList("ZA");
                        Toast.makeText(getBaseContext(), "Sorting by Reverse Alphabetical Order",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    case 4:
                        orderBy = "GAZ";
                	    setNoteList("GAZ");
                	    Toast.makeText(getBaseContext(), "Sorting by Group",
                                Toast.LENGTH_SHORT).show();
                	    return false;
                }
                return false;
            }
        };
        getActionBar().setListNavigationCallbacks(adapter, navigationListener);
    }

    private void setNoteList(String sortType){
        gridAdapter.removeAll();
    	Cursor cursor = jotIt.mDBHelper.fetchNotePreview("All Notes", sortType);
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
        cursor.close();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Reinitialize note list
        setNoteList(orderBy);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the actionBar to the screen
        getMenuInflater().inflate(R.menu.main, menu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
        // Setting up the search
        searchAdapter = new MyCursorAdapter(MainActivity.this,  null, 0, mNoteList);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                 searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
                // Reinitialize note list after search closed
                gridview.setAdapter(gridAdapter);
                searching = false;
				return false;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String s){
                return searchQuery(s);
            }

            @Override
            public boolean onQueryTextChange(String s){
                return searchQuery(s);
            }

            private boolean searchQuery(String s){
                closeDrawer();
                // Remove apostrophes because they are unrecognized by sqllite and crash the app
                if (s.contains("'")) {
                    StringBuilder removeApostrophe = new StringBuilder(s);
                    int i = 0;
                    for (Character c : removeApostrophe.toString().toCharArray()){
                        if (c == '\''){
                            removeApostrophe.deleteCharAt(i);
                            i--;
                        }
                        i++;
                    }
                    s = removeApostrophe.toString();
                }

                searching = true;
                // Save search query in case of update or delete
                searchString = s;
                // Search for query and swap cursor on grid
                Cursor cursor = jotIt.mDBHelper.fetchSearch(s);
                if (s.equals("")) {
                    cursor = null;
                }
                gridview.setAdapter(searchAdapter);
                searchAdapter.swapCursor(cursor);
                // Set the searched note list
                Cursor c = jotIt.mDBHelper.fetchSearch(s);
                searchedNoteList = searchAdapter.getSearchedNotes(c);
                return false;
            }
        });
		return true;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle actionbar buttons
    	switch (item.getItemId()) {
        case R.id.action_search:
            return true;
        case R.id.new_note:
            closeDrawer();
            Intent myIntent = new Intent(this, EditNoteActivity.class);
    		startActivity(myIntent);
        	return true;
        default:             
            return super.onOptionsItemSelected(item);
            }
    }

    private void setGrid(){
		gridview = (GridView) findViewById(R.id.gridview);
		gridAdapter = new NotesGridAdapter(this, mNoteList);
        gridview.setAdapter(gridAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get note to be edited
                NoteClass mNote;
                if (!searching){
                    mNote = mNoteList.get(position);
                }else{
                    mNote = searchedNoteList.get(position);
                    //clears search query from searchView
                    searchView.setIconified(true);
                    //closes searchView before switching activities
                    searchView.setIconified(true);
                }
                // Get note attributes and bundle to send to EditNoteActivity
                int mNoteID = mNote.getNoteID();
                String mSubject =  mNote.getSubject();
				int mBackColor = mNote.getBackgroundColor();
				int mTextColor = mNote.getTextColor();
				int mHighColor = mNote.getHighlightColor();
				Intent myIntent = new Intent(context, EditNoteActivity.class);
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
                if (!searching) {
				    if (checked) {
                        mNoteList.get(position).setBackgroundColor(
                            mNoteList.get(position).getHighlightColor());
                    } else {
                        mNoteList.get(position).setBackgroundColor(
                            mNoteList.get(position).getBackgroundColorCopy());
                    }
                    gridAdapter.toggleSelection(mNoteList.get(position));
                } else {
                    if (checked) {
                        searchedNoteList.get(position).setBackgroundColor(
                                searchedNoteList.get(position).getHighlightColor());
                    } else {
                        searchedNoteList.get(position).setBackgroundColor(
                                searchedNoteList.get(position).getBackgroundColorCopy());
                    }
                    searchAdapter.toggleSelection(searchedNoteList.get(position));
                }
				// Capture total checked items
				final int checkedCount = gridview.getCheckedItemCount();
				// Set the Context ActionBar (Multi-select mode bar) title to total checked items
				mode.setTitle(checkedCount + " Selected");
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
                        changeGroupDialog = new ChangeGroupDialog();
                        changeGroupDialog.setValues(getBaseContext(), null, cursor);
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
                    List<NoteClass> selected;
                    if (!searching) {
                        selected = gridAdapter.getSelectedIds();
                    }else{
                        selected = searchAdapter.getSelectedIds();
                    }
                    for (int i = 0; i < selected.size(); i++) {
                        NoteClass selectedItem = selected.get(i);
                        selectedItem.setBackgroundColor(
                                selectedItem.getBackgroundColorCopy());
                    }
				}
				changeOnMultiselect = false;
				gridAdapter.removeSelection();
                searchAdapter.removeSelection();
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
        List<NoteClass> selected;
        if(!searching){
            selected = gridAdapter.getSelectedIds();
        }else {
            selected = searchAdapter.getSelectedIds();
        }
        // Delete and remove each selected note using a loop
        for (int i = 0; i < selected.size(); i++) {
            NoteClass selectedItem = selected.get(i);
            jotIt.mDBHelper.deleteNote(selectedItem.getNoteID());
            mNoteList.remove(selectedItem);
        }
        // Re-look up searched query to show new search results
        Cursor searchCursor = jotIt.mDBHelper.fetchSearch(searchString);
        if (searchString != null && searchString.equals("")){
            searchCursor = null;
        }
        searchAdapter.swapCursor(searchCursor);
        // Get the updated searched note list
        Cursor c = jotIt.mDBHelper.fetchSearch(searchString);
        searchedNoteList = searchAdapter.getSearchedNotes(c);
        c.close();
        // Set changeOnMultiselect flag
        changeOnMultiselect = true;
        // Close Context ActionBar (Multi-select mode bar)
        mMode.finish();
        deleteDialog.dismiss();
    }

    public void update(){
        // Get selected notes to be updated
        List<NoteClass> selected;
        if(!searching){
            selected = gridAdapter.getSelectedIds();
        }else {
            selected = searchAdapter.getSelectedIds();
        }
        // Update each selected note using a loop
        for (int i = 0; i < selected.size(); i++) {
            NoteClass selectedItem = selected.get(i);
            jotIt.mDBHelper.updateNoteMultiple(
                    selectedItem.getNoteID(), changeGroupDialog.getChangeGroup());
        }
        // Reinitialize grid with all notes
        setNoteList(orderBy);
        // Re-look up searched query to show new search results
        Cursor searchCursor = jotIt.mDBHelper.fetchSearch(searchString);
        if (searchString != null && searchString.equals("")){
            searchCursor = null;
        }
        searchAdapter.swapCursor(searchCursor);
        // Get the updated searched note list
        Cursor c = jotIt.mDBHelper.fetchSearch(searchString);
        searchedNoteList = searchAdapter.getSearchedNotes(c);
        // Set flag because onDestroy ActionBar doesn't need to switch
        // note back to the old background color
        changeOnMultiselect = true;
        // Close Context ActionBar (Multi-select mode bar) and dialog
        mMode.finish();
        changeGroupDialog.dismiss();
    }
}