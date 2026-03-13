package com.example.quickbazaar.QuickActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.quickbazaar.BazaarAdapter.ProductAdapter;
import com.example.quickbazaar.BazaarModel.Product;
import com.example.quickbazaar.BazaarViewModel.CartViewModel;
import com.example.quickbazaar.databinding.ActivityProductListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private ActivityProductListBinding binding;
    private FirebaseFirestore firestore;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private String categoryId, categoryName;
    private CartViewModel cartViewModel;

    private static final String TAG = "ProductListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");
        
        if (categoryName != null) {
            binding.textCategoryNameHeader.setText(categoryName);
        }

        binding.imageBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        setupSearchView();
        fetchProducts();
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(ProductListActivity.this, ProductFullScreenActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }

            @Override
            public void onAddToCart(Product product) {
                cartViewModel.AddToCart(product);
                Toast.makeText(ProductListActivity.this, product.getName() + " added to bag", Toast.LENGTH_SHORT).show();
            }
        });
        binding.recycleerviewProductsList.setAdapter(productAdapter);
        binding.recycleerviewProductsList.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupSearchView() {
        binding.searchViewProducts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productAdapter.filter(query);
                checkEmptyState();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.filter(newText);
                checkEmptyState();
                return false;
            }
        });
    }

    private void checkEmptyState() {
        if (productAdapter.getItemCount() == 0) {
            binding.layoutNoData.setVisibility(View.VISIBLE);
        } else {
            binding.layoutNoData.setVisibility(View.GONE);
        }
    }

    private void fetchProducts() {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            binding.progressBarList.setVisibility(View.GONE);
            binding.layoutNoData.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressBarList.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);

        firestore.collection("products")
                .whereEqualTo("categoryId", categoryId.trim())
                .limit(50)
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBarList.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        List<Product> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Product product = documentSnapshot.toObject(Product.class);
                            if (product.getId() == null) {
                                product.setId(documentSnapshot.getId());
                            }
                            newList.add(product);
                        }
                        productAdapter.updateData(newList);
                        checkEmptyState();
                    } else {
                        Log.e(TAG, "Error fetching products: ", task.getException());
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
