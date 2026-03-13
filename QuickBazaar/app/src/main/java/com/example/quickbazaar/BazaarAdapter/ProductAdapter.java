package com.example.quickbazaar.BazaarAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarModel.Product;
import com.example.quickbazaar.QuickActivity.ProductFullScreenActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ItemProductBinding;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private List<Product> productList;
    private List<Product> filteredList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCart(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.filteredList = new ArrayList<>(productList);
        this.listener = listener;
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<Product> newList) {
        this.productList = newList;
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredList.get(position);

        holder.binding.tvProductName.setText(product.getName());
        holder.binding.tvProductUnit.setText(product.getUnit());
        holder.binding.tvProductDiscount.setText("₹" + (int)product.getDiscountPrice());
        holder.binding.tvProductPrice.setText("₹" + (int)product.getPrice());

        // Rating functionality
        holder.binding.productRatingBar.setRating((float) product.getRating());
        holder.binding.tvProductRatingText.setText(String.valueOf(product.getRating()));

        // Strikethrough for original price
        if (product.getDiscountPrice() < product.getPrice()) {
            holder.binding.tvProductPrice.setVisibility(View.VISIBLE);
            holder.binding.tvProductPrice.setPaintFlags(holder.binding.tvProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            
            // Calculate and show discount percentage
            int discountPercent = (int) (((product.getPrice() - product.getDiscountPrice()) / product.getPrice()) * 100);
            if (discountPercent > 0) {
                holder.binding.tvDiscountBadge.setVisibility(View.VISIBLE);
                holder.binding.tvDiscountBadge.setText(discountPercent + "% OFF");
            } else {
                holder.binding.tvDiscountBadge.setVisibility(View.GONE);
            }
        } else {
            holder.binding.tvProductPrice.setVisibility(View.GONE);
            holder.binding.tvDiscountBadge.setVisibility(View.GONE);
        }

        if (product.isPopular()) {
            holder.binding.tvPopularBadge.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvPopularBadge.setVisibility(View.GONE);
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.ic_grocery_cart)
                    .centerCrop()
                    .into(holder.binding.imgProduct);
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onProductClick(product);
        });

        holder.binding.btnAddToBag.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;
        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
