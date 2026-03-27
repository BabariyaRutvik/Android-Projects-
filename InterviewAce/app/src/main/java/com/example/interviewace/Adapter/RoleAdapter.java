package com.example.interviewace.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewace.R;

import java.util.List;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.RoleViewHolder> {

    private List<String> roles;
    private OnRoleClickListener listener;

    public interface OnRoleClickListener {
        void onRoleClick(String role);
    }

    public RoleAdapter(List<String> roles, OnRoleClickListener listener) {
        this.roles = roles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role, parent, false);
        return new RoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, int position) {
        String role = roles.get(position);
        holder.tvRoleName.setText(role);
        holder.itemView.setOnClickListener(v -> listener.onRoleClick(role));
    }

    @Override
    public int getItemCount() {
        return roles != null ? roles.size() : 0;
    }

    static class RoleViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoleName;

        public RoleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoleName = itemView.findViewById(R.id.tv_role_name);
        }
    }
}
