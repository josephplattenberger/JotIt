package com.josephplattenberger.jotit;

import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class ChangeGroupDialog extends DialogFragment {

    Context context;
    private String groupName, changeGroup;
    private Cursor cursor;

    public interface UpdateListener {
        void update();
    }

    void setValues(Context context, String groupName, Cursor cursor){
        this.context = context;
        this.groupName = groupName;
        this.cursor = cursor;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(!(context instanceof UpdateListener)){
            throw new ClassCastException((context.toString() + " must implement UpdateListener"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.change_group, container, false);
        getDialog().setTitle("Change Group");
        final Button acceptButton = (Button) rootView.findViewById(R.id.changeGroupAcceptButton);
        ArrayList<String> mGroups = new ArrayList<String>();
        mGroups.add("Choose a Group..");
        cursor.moveToFirst();
        ArrayList<Integer> themeBackColors = new ArrayList<>();
        ArrayList<Integer> themeTextColors = new ArrayList<>();
        themeBackColors.add(0xFFFFFFFF);
        themeTextColors.add(0xFF000000);
        while(!cursor.isAfterLast()){
            mGroups.add(cursor.getString(0));
            themeBackColors.add(cursor.getInt(1));
            themeTextColors.add(cursor.getInt(2));
            cursor.moveToNext();
        }
        cursor.close();
        // Initialize Group Spinner with lists
        final Spinner mSpinner = (Spinner) rootView.findViewById(
                R.id.changeGroupSpinner);
        SpinnerAdapter adapter = new SpinnerAdapter(context, mGroups,
                themeBackColors, themeTextColors);
        mSpinner.setAdapter(adapter);
        // Set to "Choose group..." so Accept Button is hidden
        if (groupName != null){
            mSpinner.setSelection(adapter.getPosition(groupName));
        } else {
            mSpinner.setSelection(0);
        }

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // if group is chosen (not "choose group...") then make Accept available
                if (position != 0){
                    acceptButton.setVisibility(View.VISIBLE);
                }else{
                    acceptButton.setVisibility(View.INVISIBLE);
                }
                // Get the New Group for your selected notes
                changeGroup = mSpinner.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UpdateListener)getActivity()).update();
            }
        });
        return rootView;
    }

    public String getChangeGroup(){
        return changeGroup;
    }
}

