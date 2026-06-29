package com.example.quicknotes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quicknotes.Model.WidgetModel;
import com.example.quicknotes.R;
import com.example.quicknotes.databinding.ItemWidgetPreviewBinding;
import java.util.List;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder> {

    private List<WidgetModel> widgetList;
    private int selectedPosition = -1;

    private OnWidgetSelectedListener onWidgetSelectedListener;

    public interface OnWidgetSelectedListener {
        void onWidgetSelected(WidgetModel widget);
    }

    public void setOnWidgetSelectedListener(OnWidgetSelectedListener listener) {
        this.onWidgetSelectedListener = listener;
    }

    public WidgetAdapter(List<WidgetModel> widgetList) {
        this.widgetList = widgetList;
    }

    @NonNull
    @Override
    public WidgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWidgetPreviewBinding binding = ItemWidgetPreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WidgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetViewHolder holder, int position) {
        WidgetModel widget = widgetList.get(position);
        holder.binding.txtWidgetSize.setText(widget.getSize());
        holder.binding.imgWidgetPreview.setImageResource(widget.getPreviewImage());

        if (position == selectedPosition) {
            holder.binding.cardWidgetPreview.setStrokeWidth(4);
        } else {
            holder.binding.cardWidgetPreview.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (onWidgetSelectedListener != null) {
                onWidgetSelectedListener.onWidgetSelected(widget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return widgetList.size();
    }

    public WidgetModel getSelectedWidget() {
        if (selectedPosition != -1) {
            return widgetList.get(selectedPosition);
        }
        return null;
    }

    public static class WidgetViewHolder extends RecyclerView.ViewHolder {
        ItemWidgetPreviewBinding binding;
        public WidgetViewHolder(ItemWidgetPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}