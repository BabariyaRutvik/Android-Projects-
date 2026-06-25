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

    private String selectedCategory;

    public AddNoteBottomSheet() {
        // Required empty public constructor
        this.selectedCategory = "All";
    }

    public AddNoteBottomSheet(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_note, container, false);

        view.findViewById(R.id.cardTxt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddNoteActivity.class);
                if (!"All".equalsIgnoreCase(selectedCategory)) {
                    i.putExtra("category", selectedCategory);
                }
                startActivity(i);
                dismiss();
            }
        });

        view.findViewById(R.id.cardChecklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddCheckListActivity.class);
                if (!"All".equalsIgnoreCase(selectedCategory)) {
                    intent.putExtra("category", selectedCategory);
                }
                startActivity(intent);
                dismiss();
            }
        });
        return view;
    }
}
