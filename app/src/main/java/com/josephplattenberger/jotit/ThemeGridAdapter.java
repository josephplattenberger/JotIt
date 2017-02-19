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

class ThemeGridAdapter extends BaseAdapter {
	private Context mContext;
    private List<ThemeClass> mSelectedItemsIds;
    private List<ThemeClass> mThemeList;

    ThemeGridAdapter(Context c, List<ThemeClass> mThemeList) {
    	this.mContext = c;
        mSelectedItemsIds = new ArrayList<>();
        this.mThemeList = mThemeList;
        
    }

	void toggleSelection(ThemeClass theme) {
		if (mSelectedItemsIds.contains(theme)){
			mSelectedItemsIds.remove(theme);
		}else{
			mSelectedItemsIds.add(theme);
		}
		notifyDataSetChanged();
	}

    void removeSelection(){
    	mSelectedItemsIds = new ArrayList<>();
    	notifyDataSetChanged();
    }

	List<ThemeClass> getSelectedIds(){
		return mSelectedItemsIds;
	}

	public int getCount() {
        return mThemeList.size();
    }

    public ThemeClass getItem(int position) {
        return mThemeList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // Create a new TextView for each item referenced by the Adapter
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
        textView.setText(mThemeList.get(position).getName());
		textView.setMaxLines(3);
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setBackgroundResource(R.drawable.rounded_corner);
        textView.setTextColor(mThemeList.get(position).getTextColor());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			textView.setElevation(10);
		}
		GradientDrawable drawable = (GradientDrawable) textView.getBackground();
		drawable.setColor(mThemeList.get(position).getBackgroundColor());
		drawable.setCornerRadius(30);
        return textView;
    }
    
	void remove(ThemeClass selecteditem) {
		mThemeList.remove(selecteditem);
		notifyDataSetChanged();
		
	}

	void add(ThemeClass mTheme) {
		mThemeList.add(mTheme);
		notifyDataSetChanged();
	}
}
