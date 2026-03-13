package com.example.quickbazaar.BazaarAdapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarModel.Product;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ItemProductHorizontalBinding;

import java.util.List;

public class RelatedProductAdapter extends RecyclerView.Adapter<RelatedProductAdapter.ViewHolder> {

    private final Context context;
    private List<Product> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCart(Product product);
    }

    public RelatedProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    public void updateData(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductHorizontalBinding binding = ItemProductHorizontalBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.binding.tvProductNameList.setText(product.getName());
        holder.binding.tvProductPriceList.setText("₹" + (int) product.getPrice());
        holder.binding.tvProductPriceList.setPaintFlags(holder.binding.tvProductPriceList.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.binding.tvProductDiscountList.setText("₹" + (int) product.getDiscountPrice());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.ic_grocery_cart)
                    .into(holder.binding.imgProductList);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        
        holder.binding.btnAddToBagList.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductHorizontalBinding binding;
        public ViewHolder(ItemProductHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
