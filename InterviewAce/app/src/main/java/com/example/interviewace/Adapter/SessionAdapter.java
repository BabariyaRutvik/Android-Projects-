package com.example.interviewace.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewace.R;
import com.example.interviewace.databinding.ItemSessionsBinding;
import com.example.interviewace.model.SessionItem;

import java.util.ArrayList;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<SessionItem> sessionList;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(int position);
    }

    public SessionAdapter() {
        this.sessionList = new ArrayList<>();
    }

    public SessionAdapter(OnSessionClickListener listener) {
        this.sessionList = new ArrayList<>();
        this.listener = listener;
    }

    public SessionAdapter(List<SessionItem> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    public void setSessionList(List<SessionItem> sessionList) {
        this.sessionList = sessionList;
        notifyDataSetChanged();
    }

    public List<SessionItem> getSessionList() {
        return sessionList;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSessionsBinding binding = ItemSessionsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SessionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        SessionItem item = sessionList.get(position);
        holder.binding.tvSessionRole.setText(item.getRoleName());
        holder.binding.tvSessionDate.setText(item.getDate());
        
        String scorePercent = item.getScore() + "%";
        holder.binding.tvSessionScore.setText(scorePercent);

        // Dynamic styling based on score
        if (item.getScore() >= 80) {
            holder.binding.tvSessionScore.setBackgroundResource(R.drawable.bg_success_green);
            holder.binding.tvSessionScore.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
        } else if (item.getScore() >= 50) {
            holder.binding.tvSessionScore.setBackgroundResource(R.drawable.bg_warning_orange);
            holder.binding.tvSessionScore.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning_orange));
        } else {
            holder.binding.tvSessionScore.setBackgroundResource(R.drawable.bg_error_red);
            holder.binding.tvSessionScore.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSessionClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessionList != null ? sessionList.size() : 0;
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        ItemSessionsBinding binding;

        public SessionViewHolder(ItemSessionsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
