package com.example.calculator.BottomSheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Adapter.UnitListAdapter;
import com.example.calculator.Model.UnitItem;
import com.example.calculator.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class UnitConverterBottomSheet extends BottomSheetDialogFragment {

    public interface OnUnitSelectedListener {
        void onUnitSelected(UnitItem unitItem);
    }

    private final String title;
    private final List<UnitItem> unitList;
    private final OnUnitSelectedListener listener;
    UnitListAdapter adapter;

    public UnitConverterBottomSheet(String title, List<UnitItem> unitList, OnUnitSelectedListener listener) {
        this.title = title;
        this.unitList = unitList;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_unit_converter, container, false);

        TextView textTitle = view.findViewById(R.id.text_unit_dialog_title);
        textTitle.setText(title + " List");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_units_converter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        adapter = new UnitListAdapter(unitList, selectedUnit -> {
            if (listener != null) {
                listener.onUnitSelected(selectedUnit);

            }
            dismiss();

        });
        recyclerView.setAdapter(adapter);

        // Apply window insets to handle edge-to-edge correctly (navigation bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

                int listSize = unitList != null ? unitList.size() : 0;

                // Threshold to decide if the list is "short" (like Current, Time, Voltage) or "long" (like Weight)
                if (listSize <= 10) {
                    // For short lists: Fit precisely to content and prevent expanding to empty full screen
                    behavior.setFitToContents(true);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    // For long lists: Multi-stage (60% -> Full)
                    behavior.setFitToContents(false);
                    behavior.setHalfExpandedRatio(0.6f);
                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }

                // Standard behavior for both
                behavior.setSkipCollapsed(true);
                behavior.setHideable(true);
            }
        }
    }
}
