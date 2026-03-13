package com.example.quickbazaar.BazaarAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickbazaar.BazaarModel.Order;
import com.example.quickbazaar.databinding.ItemOrderBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        String displayId = order.getOrderId();
        if (displayId != null && displayId.length() > 8) {
            displayId = displayId.substring(0, 8).toUpperCase();
        } else if (displayId != null) {
            displayId = displayId.toUpperCase();
        }

        holder.binding.tvOrderId.setText("#" + displayId);
        holder.binding.tvTotalAmount.setText("₹" + String.format(Locale.getDefault(), "%.2f", order.getTotalAmount()));
        holder.binding.tvOrderStatus.setText(order.getStatus());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String dateString = sdf.format(new Date(order.getTimestamp()));
        holder.binding.tvOrderDate.setText(dateString);
        
        if (order.getStatus() != null && order.getStatus().equalsIgnoreCase("Delivered")) {
            holder.binding.tvOrderStatus.setBackgroundResource(com.example.quickbazaar.R.drawable.bg_order_status_delivered);
        } else {
            holder.binding.tvOrderStatus.setBackgroundResource(com.example.quickbazaar.R.drawable.bg_order_status_pending);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;
        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
