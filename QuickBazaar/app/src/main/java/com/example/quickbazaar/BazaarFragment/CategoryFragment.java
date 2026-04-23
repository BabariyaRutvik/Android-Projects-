package com.example.quickbazaar.BazaarFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.quickbazaar.BazaarAdapter.CategoryGridAdapter;
import com.example.quickbazaar.BazaarModel.Category;
import com.example.quickbazaar.databinding.FragmentCategoryBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private FirebaseFirestore firestore;
    private CategoryGridAdapter categoryAdapter;
    private List<Category> categoryList;

    private static final String TAG = "CategoryFragment";

    public CategoryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryGridAdapter(getActivity(), categoryList);
        binding.rvAllCategories.setAdapter(categoryAdapter);

        setupSearchView();
        fetchCategories();
    }

    private void setupSearchView() {
        binding.searchViewCategories.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (categoryAdapter != null) {
                    categoryAdapter.filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (categoryAdapter != null) {
                    categoryAdapter.filter(newText);
                }
                return false;
            }
        });
    }

    private void fetchCategories() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestore.collection("Category")
                .orderBy("priority")
                .get()
                .addOnCompleteListener(task -> {
                    if (binding == null || !isAdded()) return;
                    
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Category category = documentSnapshot.toObject(Category.class);
                            categoryList.add(category);
                        }
                        categoryAdapter.updateData(categoryList);
                    } else {
                        Log.e(TAG, "Error getting categories: ", task.getException());
                        Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
