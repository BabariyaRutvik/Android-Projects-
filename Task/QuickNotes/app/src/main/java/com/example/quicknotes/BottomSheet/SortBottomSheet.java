package com.example.quicknotes.BottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quicknotes.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheet extends BottomSheetDialogFragment {

    private OnSortOptionSelectedListener listener;

    public interface OnSortOptionSelectedListener {
        void onSortOptionSelected(int id);
    }

    public void setOnSortOptionSelectedListener(OnSortOptionSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_sort, container, false);

        RadioGroup generalGroup = view.findViewById(R.id.geneeral_group);
        RadioGroup timeGroup = view.findViewById(R.id.radio_time_group);

        generalGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                timeGroup.clearCheck();
                if (listener != null) listener.onSortOptionSelected(checkedId);
                dismiss();
            }
        });

        timeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                generalGroup.clearCheck();
                if (listener != null) listener.onSortOptionSelected(checkedId);
                dismiss();
            }
        });

        return view;
    }
}
