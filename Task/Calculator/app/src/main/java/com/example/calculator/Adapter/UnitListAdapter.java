package com.example.calculator.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Model.UnitItem;
import com.example.calculator.R;

import java.util.List;

public class UnitListAdapter extends RecyclerView.Adapter<UnitListAdapter.UnitViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(UnitItem item);
    }

    private final List<UnitItem> items;
    private final OnItemClickListener clickListener;

    public UnitListAdapter(List<UnitItem> items, OnItemClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit_list, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        UnitItem item = items.get(position);

        // Formats your display precisely into standard styles: "Kilogram (kg)"
        holder.textUnitDisplay.setText(item.getName() + " (" + item.getCode() + ")");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class UnitViewHolder extends RecyclerView.ViewHolder {
        TextView textUnitDisplay;

        public UnitViewHolder(@NonNull View itemView) {
            super(itemView);

            textUnitDisplay = itemView.findViewById(R.id.text_unit_name);
        }
    }
}
