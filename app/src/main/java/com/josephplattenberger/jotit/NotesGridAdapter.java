package com.josephplattenberger.jotit;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

class NotesGridAdapter extends BaseAdapter {
	private Context mContext;
    private List<NoteClass> mSelectedItemsIds;
    private List<NoteClass> mNoteList;

    NotesGridAdapter(Context c, List<NoteClass> mNoteList) {
    	this.mContext = c;
        mSelectedItemsIds = new ArrayList<>();
        this.mNoteList = mNoteList;
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

	public int getCount() {
        return mNoteList.size();
    }

    public NoteClass getItem(int position) {
        return mNoteList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // Create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
        	textView = new TextView(mContext);
            textView.setLayoutParams(new GridView.LayoutParams(
                    (int)mContext.getResources().getDimension(R.dimen.item_size),
                    (int)mContext.getResources().getDimension(R.dimen.item_size)));
            textView.setPadding((int)mContext.getResources().getDimension(R.dimen.item_padding),
                    (int)mContext.getResources().getDimension(R.dimen.item_padding),
                    (int)mContext.getResources().getDimension(R.dimen.item_padding),
                    (int)mContext.getResources().getDimension(R.dimen.item_padding));
        } else {
            textView = (TextView) convertView;
        }
        // Set TextView attributes
        textView.setText(mNoteList.get(position).getSubject());
        textView.setMaxLines(3);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setBackgroundResource(R.drawable.rounded_corner);
        textView.setTextColor(mNoteList.get(position).getTextColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setElevation(10);
        }
        GradientDrawable drawable = (GradientDrawable) textView.getBackground();
        drawable.setColor(mNoteList.get(position).getBackgroundColor());
        drawable.setCornerRadius(30);
        return textView;
    }

    void removeAll(){
        mNoteList.clear();
        notifyDataSetChanged();
    }
}
