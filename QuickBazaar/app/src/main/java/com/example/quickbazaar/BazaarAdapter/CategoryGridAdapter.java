package com.example.quickbazaar.BazaarAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarModel.Category;
import com.example.quickbazaar.QuickActivity.ProductListActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ItemCategoryGridBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.CategoryGridViewHolder> {

    private final Context context;
    private List<Category> categoryList;
    private List<Category> filteredList;

    public CategoryGridAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.filteredList = new ArrayList<>(categoryList);
    }
    // filter method to filter the list based on the query
    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(categoryList);

        }
        else {
            String lowercaseQuery = query.toLowerCase().trim();
            for (Category category : categoryList){
                if (category.getName().toLowerCase().contains(lowercaseQuery)){
                    filteredList.add(category);
                }
            }
        }
        notifyDataSetChanged();
    }

    // update method to update the data in the adapter
    public void updateData(List<Category> newList){
        this.categoryList = newList;
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryGridBinding binding = ItemCategoryGridBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CategoryGridViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryGridViewHolder holder, int position) {
        Category category = filteredList.get(position);

        holder.binding.tvCategoryNameGrid.setText(category.getName());

        Glide.with(context)
                .load(category.getIconUrl())
                .placeholder(R.drawable.ic_categories)
                .into(holder.binding.imgCategoryGrid);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductListActivity.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class CategoryGridViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryGridBinding binding;

        public CategoryGridViewHolder(ItemCategoryGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
