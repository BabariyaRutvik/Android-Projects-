package com.example.quicknotes.BottomSheet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Adapter.SelectionCategoryAdapter;
import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class DefaultFolderBottomSheet extends BottomSheetDialogFragment {

    private ArrayList<CategoryModel> categoryList;
    private OnFolderSelectedListener listener;

    public interface OnFolderSelectedListener {
        void onFolderSelected(String categoryKey);
    }

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }

    public void setOnFolderSelectedListener(OnFolderSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_default_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvDefaultFolder = view.findViewById(R.id.rv_default_folder);
        rvDefaultFolder.setLayoutManager(new LinearLayoutManager(requireContext()));

        initializeCategories();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        String currentDefault = sharedPreferences.getString("default_folder", "All");

        SelectionCategoryAdapter adapter = new SelectionCategoryAdapter(requireContext(), categoryList, category -> {
            sharedPreferences.edit().putString("default_folder", category.getCategoryKey()).apply();
            if (listener != null) {
                listener.onFolderSelected(category.getCategoryKey());
            }
            dismiss();
        });
        
        adapter.setSelectedCategory(currentDefault);
        rvDefaultFolder.setAdapter(adapter);
    }

    private void initializeCategories() {
        categoryList = new ArrayList<>();
        CategoryPrefs prefs = new CategoryPrefs(requireContext());
        
        // Standard categories
        categoryList.add(new CategoryModel("All", prefs.getCategoryName("All", "All"), ContextCompat.getColor(requireContext(), R.color.primary_blue), ContextCompat.getColor(requireContext(), R.color.light_sky_blue), false));
        categoryList.add(new CategoryModel("Personal", prefs.getCategoryName("Personal", "Personal"), ContextCompat.getColor(requireContext(), R.color.badge_personal_text), ContextCompat.getColor(requireContext(), R.color.badge_personal_bg), false));
        categoryList.add(new CategoryModel("Work", prefs.getCategoryName("Work", "Work"), ContextCompat.getColor(requireContext(), R.color.badge_work_text), ContextCompat.getColor(requireContext(), R.color.badge_work_bg), false));
        categoryList.add(new CategoryModel("Others", prefs.getCategoryName("Others", "Others"), ContextCompat.getColor(requireContext(), R.color.badge_others_text), ContextCompat.getColor(requireContext(), R.color.badge_others_bg), false));
        categoryList.add(new CategoryModel("Untitled_Red", prefs.getCategoryName("Untitled_Red", "Untitled"), ContextCompat.getColor(requireContext(), R.color.badge_untitled_red_text), ContextCompat.getColor(requireContext(), R.color.badge_untitled_red_bg), false));
        categoryList.add(new CategoryModel("Untitled_Orange", prefs.getCategoryName("Untitled_Orange", "Untitled"), ContextCompat.getColor(requireContext(), R.color.badge_untitled_orange_text), ContextCompat.getColor(requireContext(), R.color.badge_untitled_orange_bg), false));
        categoryList.add(new CategoryModel("Untitled_Pink", prefs.getCategoryName("Untitled_Pink", "Untitled"), ContextCompat.getColor(requireContext(), R.color.badge_untitled_pink_text), ContextCompat.getColor(requireContext(), R.color.badge_untitled_pink_bg), false));
        categoryList.add(new CategoryModel("Untitled_Purple", prefs.getCategoryName("Untitled_Purple", "Untitled"), ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_text), ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_bg), false));
        categoryList.add(new CategoryModel("Untitled_DarkGray", prefs.getCategoryName("Untitled_DarkGray", "Untitled"), ContextCompat.getColor(requireContext(), R.color.badge_untitled_dark_gray_text), ContextCompat.getColor(requireContext(), R.color.badge_untitled_dark_gray_bg), false));
        categoryList.add(new CategoryModel("Untitled_Gray", prefs.getCategoryName("Untitled_Gray", "Untitled"), ContextCompat.getColor(requireContext(), R.color.badge_untitled_gray_text), ContextCompat.getColor(requireContext(), R.color.badge_untitled_gray_bg), false));
    }
}
