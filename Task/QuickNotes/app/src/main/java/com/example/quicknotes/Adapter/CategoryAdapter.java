package com.example.quicknotes.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final ArrayList<CategoryModel> categoryList = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryModel model);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public CategoryAdapter(Context context) {
        this.context = context;
        initializeCategories();
    }


    public void setCategories(ArrayList<CategoryModel> categories) {
        this.categoryList.clear();
        this.categoryList.addAll(categories);
        notifyDataSetChanged();
    }

    public ArrayList<CategoryModel> getCategoryList() {
        return categoryList;
    }

    private void initializeCategories() {
        CategoryPrefs prefs = new CategoryPrefs(context);
        categoryList.add(new CategoryModel("All", prefs.getCategoryName("All", "All"), ContextCompat.getColor(context, R.color.primary_blue), ContextCompat.getColor(context, R.color.light_sky_blue), true));
        categoryList.add(new CategoryModel("Personal", prefs.getCategoryName("Personal", "Personal"), ContextCompat.getColor(context, R.color.badge_personal_text), ContextCompat.getColor(context, R.color.badge_personal_bg), false));
        categoryList.add(new CategoryModel("Work", prefs.getCategoryName("Work", "Work"), ContextCompat.getColor(context, R.color.badge_work_text), ContextCompat.getColor(context, R.color.badge_work_bg), false));
        categoryList.add(new CategoryModel("Others", prefs.getCategoryName("Others", "Others"), ContextCompat.getColor(context, R.color.badge_others_text), ContextCompat.getColor(context, R.color.badge_others_bg), false));
        categoryList.add(new CategoryModel("Untitled_Red", prefs.getCategoryName("Untitled_Red", "Untitled"), ContextCompat.getColor(context, R.color.badge_untitled_red_text), ContextCompat.getColor(context, R.color.badge_untitled_red_bg), false));
        categoryList.add(new CategoryModel("Untitled_Orange", prefs.getCategoryName("Untitled_Orange", "Untitled"), ContextCompat.getColor(context, R.color.badge_untitled_orange_text), ContextCompat.getColor(context, R.color.badge_untitled_orange_bg), false));
        categoryList.add(new CategoryModel("Untitled_Pink", prefs.getCategoryName("Untitled_Pink", "Untitled"), ContextCompat.getColor(context, R.color.badge_untitled_pink_text), ContextCompat.getColor(context, R.color.badge_untitled_pink_bg), false));
        categoryList.add(new CategoryModel("Untitled_Purple", prefs.getCategoryName("Untitled_Purple", "Untitled"), ContextCompat.getColor(context, R.color.badge_untitled_purple_text), ContextCompat.getColor(context, R.color.badge_untitled_purple_bg), false));
        categoryList.add(new CategoryModel("Untitled_DarkGray", prefs.getCategoryName("Untitled_DarkGray", "Untitled"), ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_text), ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_bg), false));
        categoryList.add(new CategoryModel("Untitled_Gray", prefs.getCategoryName("Untitled_Gray", "Untitled"), ContextCompat.getColor(context, R.color.badge_untitled_gray_text), ContextCompat.getColor(context, R.color.badge_untitled_gray_bg), false));
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel model = categoryList.get(position);
        
        holder.txtCategory.setText(model.getCategoryName());

        updateItemStyle(holder, model);

        holder.itemView.setOnClickListener(v -> handleItemClick(model));
    }

    private void updateItemStyle(CategoryViewHolder holder, CategoryModel model) {
        if (model.isSelected()) {
            holder.cardCategory.setCardBackgroundColor(model.getBackgroundColor());
            holder.txtCategory.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.cardCategory.setCardBackgroundColor(model.getLightColor());
            holder.txtCategory.setTextColor(model.getBackgroundColor());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleItemClick(CategoryModel clickedModel) {
        for (CategoryModel item : categoryList) {
            item.setSelected(false);
        }
        clickedModel.setSelected(true);
        notifyDataSetChanged();

        if (listener != null) {
            listener.onCategoryClick(clickedModel);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCategory;
        TextView txtCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.cardCategory);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }
}
