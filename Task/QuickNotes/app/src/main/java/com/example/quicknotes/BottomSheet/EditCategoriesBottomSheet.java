package com.example.quicknotes.BottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Adapter.EditCategoryAdapter;
import com.example.quicknotes.Adapter.SelectionCategoryAdapter;
import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collections;

public class EditCategoriesBottomSheet extends BottomSheetDialogFragment {

    public enum Mode {
        EDIT, MOVE
    }

    private ArrayList<CategoryModel> categoryList;
    private OnCategoriesUpdatedListener updateListener;
    private OnCategorySelectedListener moveListener;
    private Mode mode = Mode.EDIT;
    private CategoryModel selectedMoveCategory;

    public interface OnCategoriesUpdatedListener {
        void onCategoriesUpdated(ArrayList<CategoryModel> updatedList);
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryModel category);
    }

    public void setOnCategoriesUpdatedListener(OnCategoriesUpdatedListener listener) {
        this.updateListener = listener;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.moveListener = listener;
    }

    public EditCategoriesBottomSheet(ArrayList<CategoryModel> categoryList, Mode mode) {
        this.mode = mode;
        this.categoryList = new ArrayList<>();
        for (CategoryModel model : categoryList) {
            if (!model.getCategoryKey().equalsIgnoreCase("All")) {
                this.categoryList.add(new CategoryModel(model.getCategoryKey(), model.getCategoryName(), model.getBackgroundColor(), model.getLightColor(), model.isSelected()));
            }
        }
    }

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_edit_categories, container, false);

        TextView txtTitle = view.findViewById(R.id.txtTitle);
        if (mode == Mode.MOVE) {
            txtTitle.setText(R.string.move_to);
        } else {
            txtTitle.setText(R.string.edit);
        }

        RecyclerView rvEditCategories = view.findViewById(R.id.rvEditCategories);
        rvEditCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        if (mode == Mode.MOVE) {
            SelectionCategoryAdapter adapter = new SelectionCategoryAdapter(requireContext(), categoryList, category -> {
                selectedMoveCategory = category;
            });
            rvEditCategories.setAdapter(adapter);
        } else {
            EditCategoryAdapter adapter = new EditCategoryAdapter(requireContext(), categoryList);
            rvEditCategories.setAdapter(adapter);
        }

        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            if (mode == Mode.MOVE) {
                if (selectedMoveCategory != null && moveListener != null) {
                    moveListener.onCategorySelected(selectedMoveCategory);
                }
            } else {
                CategoryPrefs prefs = new CategoryPrefs(requireContext());
                for (CategoryModel model : categoryList) {
                    prefs.saveCategoryName(model.getCategoryKey(), model.getCategoryName());
                }
                if (updateListener != null) {
                    updateListener.onCategoriesUpdated(categoryList);
                }
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setDraggable(false);

                // Set a fixed height (e.g., 70% of screen) to match the screenshot look
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int desiredHeight = (int) (screenHeight * 0.70); 
                
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = desiredHeight;
                bottomSheet.setLayoutParams(layoutParams);

                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }
}
