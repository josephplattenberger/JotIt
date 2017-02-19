package com.josephplattenberger.jotit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class MyCursorAdapter extends CursorAdapter {
    private Context context;
    private List<NoteClass> mNoteList;
    private List<NoteClass> mSelectedItemsIds;
    private List<NoteClass> searchedNotes;


    MyCursorAdapter(Context context, Cursor c, int flags, List<NoteClass> mNoteList) {
        super(context, c, flags);
        this.context = context;
        this.mNoteList = mNoteList;
        mSelectedItemsIds = new ArrayList<>();
        searchedNotes = new ArrayList<>();
    }
    // Returns the searched notes list w/ correct index of the searched notes in the full note list
    List<NoteClass> getSearchedNotes(Cursor c){
        searchedNotes.clear();
        if (c != null) {
            c.moveToFirst();
            while(!c.isAfterLast()){
                int noteID = c.getInt(c.getColumnIndex("Note_id"));
                int i = indexOfNote(noteID);
                if (i >= 0){
                    NoteClass temp = mNoteList.get(i);
                    searchedNotes.add(temp);
                }
                c.moveToNext();
            }
        }
        return searchedNotes;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (!super.getCursor().moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        // Create a new TextView for each item referenced by the Adapter
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new TextView(context);
            textView.setLayoutParams(new GridView.LayoutParams(
                    (int)context.getResources().getDimension(R.dimen.item_size),
                    (int)context.getResources().getDimension(R.dimen.item_size)));
            textView.setPadding((int)context.getResources().getDimension(R.dimen.item_padding),
                    (int)context.getResources().getDimension(R.dimen.item_padding),
                    (int)context.getResources().getDimension(R.dimen.item_padding),
                    (int)context.getResources().getDimension(R.dimen.item_padding));
        } else {
            textView = (TextView) convertView;
        }
        /*
         * Get id of searched note in order to find its index in the note list
         * because using the position variable will get the position of the note
         * in the original "all notes" grid
         */
        int noteID = super.getCursor().getInt(super.getCursor().getColumnIndex("Note_id"));
        int i = indexOfNote(noteID);
        if (i >= 0){
            // Set TextView attributes
            NoteClass temp = mNoteList.get(i);
            textView.setText(temp.getSubject());
            textView.setMaxLines(3);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setBackgroundResource(R.drawable.rounded_corner);
            textView.setTextColor(temp.getTextColor());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textView.setElevation(10);
            }
            GradientDrawable drawable = (GradientDrawable) textView.getBackground();
            drawable.setColor(mNoteList.get(i).getBackgroundColor());
            drawable.setCornerRadius(30);
        }
        return textView;
    }
    // Returns the correct index of the searched note in the full note list
    private int indexOfNote(int id){
        for (int i = 0; i < mNoteList.size(); i++){
            if (mNoteList.get(i).getNoteID() == id){
                return i;
            }
        }
        return -1;
    }

    void toggleSelection(NoteClass note) {
        if (mSelectedItemsIds.contains(note)){
            mSelectedItemsIds.remove(note);
        }else{
            mSelectedItemsIds.add(note);
        }
        notifyDataSetChanged();
    }

    void removeSelection(){
        mSelectedItemsIds.clear();
        notifyDataSetChanged();
    }

    List<NoteClass> getSelectedIds(){
        return mSelectedItemsIds;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }
}