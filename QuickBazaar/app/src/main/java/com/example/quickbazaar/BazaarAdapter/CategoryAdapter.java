package com.example.quickbazaar.BazaarAdapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarModel.Category;
import com.example.quickbazaar.QuickActivity.ProductListActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ItemCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryView> {
    private final Context context;
    private final List<Category> categoryList;
    private static final String TAG = "CategoryAdapter_DEBUG";

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public void updateData(List<Category> newList) {
        this.categoryList.clear();
        this.categoryList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CategoryView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryView holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());

        Glide.with(context)
                .load(category.getIconUrl())
                .placeholder(R.drawable.ic_categories)
                .error(R.drawable.ic_categories)
                .into(holder.binding.imgCategory);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Category clicked: " + category.getName() + " with ID: " + category.getId());
                Intent intent = new Intent(context, ProductListActivity.class);
                intent.putExtra("categoryId", category.getId());
                intent.putExtra("categoryName", category.getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class CategoryView extends RecyclerView.ViewHolder {
        ItemCategoryBinding binding;
        public CategoryView(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
