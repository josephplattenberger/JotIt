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

class GroupGridAdapter extends BaseAdapter {
	private Context mContext;
    private List<GroupClass> mSelectedItemsIds;
    private List<GroupClass> mGroupList;

    GroupGridAdapter(Context c, List<GroupClass> mGroupList) {
    	this.mContext = c;
        mSelectedItemsIds = new ArrayList<>();
        this.mGroupList = mGroupList;
        
    }

	void toggleSelection(GroupClass group) {
		if (mSelectedItemsIds.contains(group)){
			mSelectedItemsIds.remove(group);
		}else{
			mSelectedItemsIds.add(group);
		}
		notifyDataSetChanged();
	}

    void removeSelection(){
    	mSelectedItemsIds = new ArrayList<>();
    	notifyDataSetChanged();
    }

	List<GroupClass> getSelectedIds(){
		return mSelectedItemsIds;
	}

	public int getCount() {
        return mGroupList.size();
    }

    public GroupClass getItem(int position) {
        return mGroupList.get(position);
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
        textView.setText(mGroupList.get(position).getName());
        textView.setMaxLines(3);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setBackgroundResource(R.drawable.rounded_corner);
        textView.setTextColor(mGroupList.get(position).getTextColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setElevation(10);
        }
        GradientDrawable drawable = (GradientDrawable) textView.getBackground();
        drawable.setColor(mGroupList.get(position).getBackgroundColor());
        drawable.setCornerRadius(30);
        return textView;
    }
    
	void remove(GroupClass selecteditem) {
		mGroupList.remove(selecteditem);
		notifyDataSetChanged();
		
	}

	void add(GroupClass mGroup) {
		mGroupList.add(mGroup);
		notifyDataSetChanged();
	}
}
