package com.example.calculator.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Model.CalculatorItem;
import com.example.calculator.R;

import java.util.List;

public class OtherCalculatorAdapter extends RecyclerView.Adapter<OtherCalculatorAdapter.ViewHolder> {

    private List<CalculatorItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CalculatorItem item);
    }

    public OtherCalculatorAdapter(List<CalculatorItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_calculator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalculatorItem item = items.get(position);
        holder.textName.setText(holder.itemView.getContext().getString(item.getNameResId()));
        holder.imgIcon.setImageResource(item.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView textName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_calculator);
            textName = itemView.findViewById(R.id.text_calculator_name);
        }
    }
}
