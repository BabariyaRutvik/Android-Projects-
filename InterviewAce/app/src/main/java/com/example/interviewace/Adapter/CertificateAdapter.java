package com.example.interviewace.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewace.databinding.ItemCertificateSmallBinding;
import com.example.interviewace.model.SessionItem;

import java.util.ArrayList;
import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {

    private List<SessionItem> certificates = new ArrayList<>();

    public void setCertificates(List<SessionItem> certificates) {
        this.certificates = certificates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCertificateSmallBinding binding = ItemCertificateSmallBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionItem session = certificates.get(position);
        holder.binding.tvCertTitle.setText(session.getRoleName());
        holder.binding.tvCertScore.setText("Score: " + session.getScore() + "/100");
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCertificateSmallBinding binding;

        public ViewHolder(@NonNull ItemCertificateSmallBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
