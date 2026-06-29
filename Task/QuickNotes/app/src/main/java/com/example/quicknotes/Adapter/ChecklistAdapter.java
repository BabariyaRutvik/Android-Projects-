package com.example.quicknotes.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Model.ChecklistItem;
import com.example.quicknotes.R;

import java.util.ArrayList;
import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD = 1;

    private List<ChecklistItem> items = new ArrayList<>();

    public ChecklistAdapter(List<ChecklistItem> items) {
        if (items != null) {
            this.items = items;
        }
    }

    public List<ChecklistItem> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == items.size()) {
            return TYPE_ADD;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist_add, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            ChecklistItem item = items.get(position);

            itemHolder.edtItem.setText(item.getText());
            itemHolder.checkbox.setImageResource(item.isChecked() ? R.drawable.ic_checkbox_checked_circle : R.drawable.ic_checkbox_unchecked_circle);
            itemHolder.checkbox.setAlpha(1.0f);

            if (item.isChecked()) {
                itemHolder.edtItem.setPaintFlags(itemHolder.edtItem.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                itemHolder.edtItem.setAlpha(0.6f);
            } else {
                itemHolder.edtItem.setPaintFlags(itemHolder.edtItem.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                itemHolder.edtItem.setAlpha(1.0f);
            }

            itemHolder.checkbox.setOnClickListener(v -> {
                item.setChecked(!item.isChecked());
                notifyItemChanged(holder.getBindingAdapterPosition());
            });

            itemHolder.imgDelete.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    items.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, items.size());
                }
            });

            // Use TextWatcher to update item text
            if (itemHolder.textWatcher != null) {
                itemHolder.edtItem.removeTextChangedListener(itemHolder.textWatcher);
            }
            itemHolder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setText(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };
            itemHolder.edtItem.addTextChangedListener(itemHolder.textWatcher);

        } else if (holder instanceof AddViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                items.add(new ChecklistItem("", false));
                notifyItemInserted(items.size() - 1);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDrag, checkbox, imgDelete;
        EditText edtItem;
        TextWatcher textWatcher;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDrag = itemView.findViewById(R.id.imgDrag);
            checkbox = itemView.findViewById(R.id.checkbox);
            edtItem = itemView.findViewById(R.id.edtItem);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }

    public static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
