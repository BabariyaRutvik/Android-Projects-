package com.example.quicknotes.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class SelectionCategoryAdapter extends RecyclerView.Adapter<SelectionCategoryAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<CategoryModel> categoryList;
    private final OnCategorySelectedListener listener;
    private int selectedPosition = -1;

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryModel category);
    }

    public SelectionCategoryAdapter(Context context, ArrayList<CategoryModel> categoryList, OnCategorySelectedListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    public void setSelectedCategory(String categoryKey) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryKey().equalsIgnoreCase(categoryKey)) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selection_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel model = categoryList.get(position);
        
        holder.txtCategoryName.setText(model.getCategoryName());
        holder.imgCategoryColor.setImageTintList(ColorStateList.valueOf(model.getBackgroundColor()));
        
        // Always show the light background color
        holder.cardCategory.setCardBackgroundColor(model.getLightColor());

        if (position == selectedPosition) {
            // Selected state: Show border with category color
            holder.cardCategory.setStrokeWidth(6); // Increased width for visibility
            holder.cardCategory.setStrokeColor(model.getBackgroundColor());
            if (holder.imgSelected != null) {
                holder.imgSelected.setVisibility(View.VISIBLE);
                holder.imgSelected.setImageTintList(ColorStateList.valueOf(model.getBackgroundColor()));
            }
        } else {
            // Unselected state: No border
            holder.cardCategory.setStrokeWidth(0);
            if (holder.imgSelected != null) {
                holder.imgSelected.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onCategorySelected(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCategory;
        ImageView imgCategoryColor, imgSelected;
        TextView txtCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.cardCategory);
            imgCategoryColor = itemView.findViewById(R.id.imgCategoryColor);
            imgSelected = itemView.findViewById(R.id.imgSelected);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}
