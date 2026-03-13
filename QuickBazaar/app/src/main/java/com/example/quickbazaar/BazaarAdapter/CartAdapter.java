package com.example.quickbazaar.BazaarAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarModel.CartItem;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ItemCartBinding;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;
    private final OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemClickListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.binding.tvCartProductName.setText(item.getProduct().getName());
        holder.binding.tvCartProductUnit.setText(item.getProduct().getUnit());
        holder.binding.tvCartProductPrice.setText("₹" + (int) item.getTotalPrice());
        holder.binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()){
            Glide.with(context)
                    .load(item.getProduct().getImages().get(0))
                    .placeholder(R.drawable.ic_grocery_cart)
                    .into(holder.binding.imgCartProduct);
        }
        
        // increasing product quantity
        holder.binding.btnPlus.setOnClickListener(view -> 
                listener.onQuantityChanged(item, item.getQuantity() + 1)
        );

        // decreasing the product quantity
        holder.binding.btnMinus.setOnClickListener(view -> {
            if (item.getQuantity() > 1){
                listener.onQuantityChanged(item, item.getQuantity() - 1);
            }
            else {
                listener.onRemoveItem(item);
            }
        });

        // remove item click
        holder.binding.btnRemoveItem.setOnClickListener(v -> 
                listener.onRemoveItem(item)
        );

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;
        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
