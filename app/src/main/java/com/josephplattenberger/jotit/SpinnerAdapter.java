package com.josephplattenberger.jotit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

class SpinnerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> listText;
    private ArrayList<Integer> backgroundColors;
    private ArrayList<Integer> textColors;

    SpinnerAdapter(Context context, int [] backgroundColors, String [] listText){

        // Initialize variables
        this.context = context;
        this.backgroundColors = new ArrayList<>();
        this.listText = new ArrayList<>();
        // Add contents to lists
        for (int color : backgroundColors){
            this.backgroundColors.add(color);
        }
        this.listText.addAll(Arrays.asList(listText));

    }

    SpinnerAdapter (Context context, ArrayList<String> listText, ArrayList<Integer> backgroundColors,
                           ArrayList<Integer> textColors){

        this.context = context;
        this.listText = listText;
        this.backgroundColors = backgroundColors;
        this.textColors = textColors;
    }

    @Override
    public int getCount(){
        return backgroundColors.size();
    }

    @Override
    public Object getItem(int arg0){
        return listText.get(arg0);
    }

    @Override
    public long getItemId(int arg0){
        return arg0;
    }

    int getPosition(String arg0){
        return listText.indexOf(arg0);
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent){
        LayoutInflater inflater=LayoutInflater.from(context);
        view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        TextView textView =(TextView)view.findViewById(android.R.id.text1);
        textView.setBackgroundColor(backgroundColors.get(pos));
        textView.setText(listText.get(pos));
        textView.setTextSize(25);
        if (textColors != null){
            textView.setTextColor(textColors.get(pos));
        }
        return view;
    }

}
