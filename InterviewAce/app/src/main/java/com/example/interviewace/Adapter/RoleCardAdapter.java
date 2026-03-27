package com.example.interviewace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewace.R;
import com.example.interviewace.model.RoleItem;

import java.util.List;

public class RoleCardAdapter extends RecyclerView.Adapter<RoleCardAdapter.RoleCardViewHolder> {

    private List<RoleItem> roleItems;
    private OnRoleCardClickListener listener;

    public interface OnRoleCardClickListener {
        void onRoleCardClick(RoleItem roleItem);
    }

    public RoleCardAdapter(List<RoleItem> roleItems, OnRoleCardClickListener listener) {
        this.roleItems = roleItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoleCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role_card_dash, parent, false);
        return new RoleCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleCardViewHolder holder, int position) {
        RoleItem roleItem = roleItems.get(position);
        Context context = holder.itemView.getContext();

        // 1. Set Text Data
        holder.textRoleName.setText(roleItem.getRoleName());
        holder.textQuestionCount.setText(roleItem.getQuestionCount());

        // 2. Load Icon dynamically
        if (roleItem.getIconName() != null) {
            int iconResId = context.getResources().getIdentifier(roleItem.getIconName(), "drawable", context.getPackageName());

            if (iconResId != 0) {
                holder.imageRoleIcon.setImageResource(iconResId);
            } else {
                // Default icon if not found
                holder.imageRoleIcon.setImageResource(R.drawable.ic_android);
            }
        }

        // 3. Load Background dynamically (e.g., bg_soft_green)
        if (roleItem.getBgColor() != null) {
            int bgResId = context.getResources().getIdentifier(
                    roleItem.getBgColor(), "drawable", context.getPackageName());

            if (bgResId != 0) {
                holder.relativeIconBg.setBackgroundResource(bgResId);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoleCardClick(roleItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roleItems != null ? roleItems.size() : 0;
    }

    static class RoleCardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRoleIcon;
        TextView textRoleName;
        TextView textQuestionCount;
        RelativeLayout relativeIconBg;

        public RoleCardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRoleIcon = itemView.findViewById(R.id.iv_role_icon);
            textRoleName = itemView.findViewById(R.id.tv_role_name);
            textQuestionCount = itemView.findViewById(R.id.tv_question_count);
            relativeIconBg = itemView.findViewById(R.id.relative_icon_bg);
        }
    }
}