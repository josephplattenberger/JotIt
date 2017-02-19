package com.josephplattenberger.jotit;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class DeleteDialog extends DialogFragment {

    public interface DeleteListener {
        void delete();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (!(context instanceof DeleteListener)){
            throw new ClassCastException(context.toString() + " must implement DeleteListener");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.delete_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Button cancelButton = (Button) rootView.findViewById(R.id.deleteDialogCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                dismiss();
            }
        });
        Button deleteButton = (Button) rootView.findViewById(R.id.deleteDialogDeleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeleteListener)getActivity()).delete();
            }
        });
        return rootView;
    }
}
