package com.example.quicknotes.BottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quicknotes.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReminderBottomSheet extends BottomSheetDialogFragment {

    private static final String NOTE_ID = "note_ud";

    public static ReminderBottomSheet newInstance(int noteId){

        ReminderBottomSheet bottomSheet = new ReminderBottomSheet();

        Bundle args = new Bundle();
        args.putInt(NOTE_ID, noteId);
        bottomSheet.setArguments(args);

        return  bottomSheet;

    }
    public ReminderBottomSheet(){
        // default constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_reminder,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int noteId = -1;

        if (getArguments() != null){
            noteId = getArguments().getInt(NOTE_ID);
        }
    }
}
