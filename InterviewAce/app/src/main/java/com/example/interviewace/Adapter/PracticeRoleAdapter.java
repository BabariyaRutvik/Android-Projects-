package com.example.interviewace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewace.R;
import com.example.interviewace.databinding.ItemRolesBinding;
import com.example.interviewace.model.RoleItem;

import java.util.List;

public class PracticeRoleAdapter extends RecyclerView.Adapter<PracticeRoleAdapter.PracticeView>  {

    private Context context;
    private List<RoleItem> roleItems;
    private int selectedPosition = -1;
    private OnRolePracticeClickListener listener;

    @NonNull
    @Override
    public PracticeView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRolesBinding binding = ItemRolesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PracticeView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PracticeView holder, int position) {
        RoleItem roleItem = roleItems.get(position);

        holder.binding.tvRoleName.setText(roleItem.getRoleName());
        holder.binding.tvQuestionCount.setText(roleItem.getQuestionCount() + "+ Questions");

        // Dynamically load icon
        if (roleItem.getIconName() != null) {
            int resId = context.getResources().getIdentifier(roleItem.getIconName(), "drawable", context.getPackageName());
            if (resId != 0) {
                holder.binding.ivRoleIcon.setImageResource(resId);
            } else {
                holder.binding.ivRoleIcon.setImageResource(R.drawable.ic_android); // Default
            }
        }

        // Selection UI
        if (selectedPosition == position){
            holder.binding.getRoot().setStrokeWidth(3);
            holder.binding.getRoot().setStrokeColor(ContextCompat.getColor(context, R.color.accent_blue));
        } else {
            holder.binding.getRoot().setStrokeWidth(1);
            holder.binding.getRoot().setStrokeColor(ContextCompat.getColor(context, R.color.white));
        }

        // Click event
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onRoleCardClick(roleItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roleItems != null ? roleItems.size() : 0;
    }

    public interface OnRolePracticeClickListener {
        void onRoleCardClick(RoleItem roleItem);
    }

    public PracticeRoleAdapter(Context context, List<RoleItem> roleItems, OnRolePracticeClickListener listener) {
        this.context = context;
        this.roleItems = roleItems;
        this.listener = listener;
    }

    static class PracticeView extends RecyclerView.ViewHolder {
        ItemRolesBinding binding;

        public PracticeView(@NonNull ItemRolesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
