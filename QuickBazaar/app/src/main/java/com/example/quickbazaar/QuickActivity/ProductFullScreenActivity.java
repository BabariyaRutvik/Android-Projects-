package com.example.quickbazaar.QuickActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarAdapter.RelatedProductAdapter;
import com.example.quickbazaar.BazaarModel.Product;
import com.example.quickbazaar.BazaarViewModel.CartViewModel;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ActivityProductFullScreenBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductFullScreenActivity extends AppCompatActivity {

    ActivityProductFullScreenBinding binding;
    private Product product;
    private FirebaseFirestore firestore;
    private RelatedProductAdapter relatedProductAdapter;
    private List<Product> relatedProductList;
    private CartViewModel cartViewModel;

    private int currentImageIndex = 0;
    private float x1, x2;
    private static final int MIN_DISTANCE = 150;

    private static final String TAG = "ProductFullScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductFullScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddToCartFull.setOnClickListener(v -> {
            if (product != null) {
                cartViewModel.AddToCart(product);
                Toast.makeText(ProductFullScreenActivity.this, product.getName() + " added to bag", Toast.LENGTH_SHORT).show();
            }
        });

        // 1. Check if product object was passed
        product = (Product) getIntent().getSerializableExtra("product");
        
        // 2. Check if only productId was passed (from Notification)
        String productIdFromNotification = getIntent().getStringExtra("productId");

        if (product != null) {
            initializeProductUI();
        } else if (productIdFromNotification != null && !productIdFromNotification.isEmpty()) {
            fetchProductById(productIdFromNotification);
        } else {
            Toast.makeText(this, "Product Data missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchProductById(String productId) {
        // Show a loading indicator if you have one
        firestore.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            if (product.getId() == null) product.setId(documentSnapshot.getId());
                            initializeProductUI();
                        }
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching product", e);
                    Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initializeProductUI() {
        DeclareProductDetails();
        setupSwipeListener();
        SetupRelatedProducts();
        fetchRelatedProducts();
    }

    private void DeclareProductDetails() {
        binding.tvFullProductName.setText(product.getName());
        binding.tvFullProductUnit.setText(product.getUnit());
        binding.tvFullProductPrice.setText("₹" + (int) product.getDiscountPrice());
        binding.tvFullDescription.setText(product.getDescription());
        binding.tvFullCategory.setText(product.getCategoryId());
        
        // Rating functionality
        binding.ratingBarFull.setRating((float) product.getRating());
        binding.tvFullRating.setText(String.valueOf(product.getRating()));

        // Set a beautiful border to the main image card
        binding.cardMainImg.setStrokeWidth(3);
        binding.cardMainImg.setStrokeColor(ContextCompat.getColor(this, R.color.basket_green));

        updateImages();
    }

    private void updateImages() {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // Main Image
            Glide.with(this)
                    .load(product.getImages().get(currentImageIndex))
                    .placeholder(R.drawable.ic_grocery_cart)
                    .into(binding.imgMainProduct);

            // Thumbnails
            Glide.with(this).load(product.getImages().get(0)).into(binding.imgThumb1);
            if (product.getImages().size() > 1) {
                binding.cardThumb2.setVisibility(View.VISIBLE);
                Glide.with(this).load(product.getImages().get(1)).into(binding.imgThumb2);
            } else {
                binding.cardThumb2.setVisibility(View.GONE);
            }

            // Highlighting active thumbnail
            updateThumbnailBorders();

            binding.cardThumb1.setOnClickListener(v -> {
                currentImageIndex = 0;
                updateImages();
            });
            binding.cardThumb2.setOnClickListener(v -> {
                currentImageIndex = 1;
                updateImages();
            });
        }
    }

    private void updateThumbnailBorders() {
        int activeColor = ContextCompat.getColor(this, R.color.basket_green);
        if (currentImageIndex == 0) {
            binding.cardThumb1.setStrokeWidth(6);
            binding.cardThumb1.setStrokeColor(activeColor);
            binding.cardThumb2.setStrokeWidth(0);
        } else {
            if (product.getImages() != null && product.getImages().size() > 1) {
                binding.cardThumb2.setStrokeWidth(6);
                binding.cardThumb2.setStrokeColor(activeColor);
                binding.cardThumb1.setStrokeWidth(0);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListener() {
        binding.imgMainProduct.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Prevent parent scroll from stealing touch events while swiping
                v.getParent().requestDisallowInterceptTouchEvent(true);
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            if (x2 < x1) { // Left swipe (Next Image)
                                if (product.getImages() != null && currentImageIndex < product.getImages().size() - 1) {
                                    currentImageIndex++;
                                    updateImages();
                                }
                            } else { // Right swipe (Previous Image)
                                if (currentImageIndex > 0) {
                                    currentImageIndex--;
                                    updateImages();
                                }
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void SetupRelatedProducts() {
        relatedProductList = new ArrayList<>();
        relatedProductAdapter = new RelatedProductAdapter(this, relatedProductList, new RelatedProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(ProductFullScreenActivity.this, ProductFullScreenActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAddToCart(Product product) {
                cartViewModel.AddToCart(product);
                Toast.makeText(ProductFullScreenActivity.this, product.getName() + " added to bag", Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerRelatedProducts.setAdapter(relatedProductAdapter);
        binding.recyclerRelatedProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchRelatedProducts() {
        if (product != null && product.getCategoryId() != null) {
            firestore.collection("products")
                    .whereEqualTo("categoryId", product.getCategoryId())
                    .limit(4) // Limit set to 4 so that after filtering current product, we get up to 3
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Product> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Product p = documentSnapshot.toObject(Product.class);
                            if (p.getId() == null) {
                                p.setId(documentSnapshot.getId());
                            }
                            
                            String currentId = product.getId();
                            if (currentId == null) currentId = ""; 

                            if (!p.getId().equals(currentId)) {
                                newList.add(p);
                                if (newList.size() == 3) break; // Stop exactly at 3
                            }
                        }
                        relatedProductAdapter.updateData(newList);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching related products", e));
        }
    }
}
