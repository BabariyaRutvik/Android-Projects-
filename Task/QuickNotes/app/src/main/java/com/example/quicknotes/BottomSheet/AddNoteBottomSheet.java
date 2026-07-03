package com.example.quicknotes.BottomSheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddNoteBottomSheet extends BottomSheetDialogFragment {

    private String selectedCategory = "All";
    private long selectedDateMillis = -1;

    public AddNoteBottomSheet() {
        // Required empty constructor
    }

    public AddNoteBottomSheet(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public AddNoteBottomSheet(String selectedCategory, long selectedDateMillis) {
        this.selectedCategory = selectedCategory;
        this.selectedDateMillis = selectedDateMillis;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_add_note, container, false);

        View txt = view.findViewById(R.id.cardTxt);
        View checklist = view.findViewById(R.id.cardChecklist);

        txt.setOnClickListener(v -> {

            dismiss();

            Intent intent = new Intent(requireActivity(), AddNoteActivity.class);

            intent.putExtra("category", selectedCategory);
            intent.putExtra("extra_selected_date", selectedDateMillis);

            startActivity(intent);
        });

        checklist.setOnClickListener(v -> {

            dismiss();

            Intent intent = new Intent(requireActivity(), AddCheckListActivity.class);

            intent.putExtra("category", selectedCategory);
            intent.putExtra("extra_selected_date", selectedDateMillis);

            startActivity(intent);
        });

        return view;
    }
}