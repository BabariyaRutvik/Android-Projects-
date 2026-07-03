package com.example.quicknotes.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;

import java.util.ArrayList;

public class EditCategoryAdapter extends RecyclerView.Adapter<EditCategoryAdapter.ViewHolder> {

    private final ArrayList<CategoryModel> categoryList;
    private OnStartDragListener mDragStartListener;

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public EditCategoryAdapter(Context context, ArrayList<CategoryModel> categoryList) {
        this.categoryList = categoryList;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.mDragStartListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel model = categoryList.get(position);

        holder.etCategoryName.setText(model.getCategoryName());

        // Set colors
        int color = model.getBackgroundColor();
        if (holder.viewColorCircle instanceof ImageView) {
            ((ImageView) holder.viewColorCircle).setImageTintList(android.content.res.ColorStateList.valueOf(color));
        } else {
            holder.viewColorCircle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        }
        
        holder.cardCategory.setCardBackgroundColor(model.getLightColor());

        if (holder.textWatcher != null) {
            holder.etCategoryName.removeTextChangedListener(holder.textWatcher);
        }

        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                model.setCategoryName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        holder.etCategoryName.addTextChangedListener(holder.textWatcher);

        holder.imgDragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
                if (mDragStartListener != null) {
                    mDragStartListener.onStartDrag(holder);
                }
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                java.util.Collections.swap(categoryList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                java.util.Collections.swap(categoryList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardCategory;
        View viewColorCircle;
        EditText etCategoryName;
        ImageView imgDragHandle;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.cardCategory);
            viewColorCircle = itemView.findViewById(R.id.viewColorCircle);
            etCategoryName = itemView.findViewById(R.id.etCategoryName);
            imgDragHandle = itemView.findViewById(R.id.imgDragHandle);
        }
    }
}
