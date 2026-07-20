package com.example.calculator.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.R;

import java.util.List;

public class UnitCategoryAdapter extends RecyclerView.Adapter<UnitCategoryAdapter.UnitCategoryViewHolder> {
    private final List<String> categories;
    private final OnCategorySelectedListener listener;
    private int selectedPosition = 0;

    @NonNull
    @Override
    public UnitCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_item_category, parent, false);
       return new UnitCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitCategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.textCategoryName.setText(category);

        // Update selection state to reflect in UI (background and text color)
        holder.textCategoryName.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v->{
            int currentPosition = holder.getAdapterPosition();

            if (selectedPosition != currentPosition && currentPosition != RecyclerView.NO_POSITION){

                int previousSelected = selectedPosition;
                selectedPosition = currentPosition;

                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
            }
            if (listener != null) {
                listener.onCategorySelected(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // interface to handle clicks event in sctivity
    public interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }
    // constructor
    public UnitCategoryAdapter(List<String> categories, OnCategorySelectedListener listener) {
        this.categories = categories;
        this.listener = listener;
    }
    public static class UnitCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textCategoryName;

        public UnitCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.text_unit_category_name);
        }
    }
}
