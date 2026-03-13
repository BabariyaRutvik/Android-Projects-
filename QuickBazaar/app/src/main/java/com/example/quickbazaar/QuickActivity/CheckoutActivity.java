package com.example.quickbazaar.QuickActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quickbazaar.BazaarModel.CartItem;
import com.example.quickbazaar.BazaarModel.Order;
import com.example.quickbazaar.BazaarViewModel.CartViewModel;
import com.example.quickbazaar.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private CartViewModel cartViewModel;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<CartItem> orderItems;
    private double totalPayable = 0.0;
    private final double deliveryFee = 40.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        orderItems = new ArrayList<>();

        binding.btnBack.setOnClickListener(v -> finish());

        observeCart();

        binding.btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
    }

    private void observeCart() {
        cartViewModel.getCartItem().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                orderItems = items;
                double subtotal = 0;
                for (CartItem item : items) {
                    subtotal += item.getTotalPrice();
                }

                totalPayable = subtotal + deliveryFee;

                binding.tvSummarySubtotal.setText("₹" + String.format("%.2f", subtotal));
                binding.tvSummaryDelivery.setText("₹" + String.format("%.2f", deliveryFee));
                binding.tvSummaryTotal.setText("₹" + String.format("%.2f", totalPayable));
            } else {
                // If orderItems is already populated, we don't want to finish just because we cleared the cart after success
                if (orderItems == null || orderItems.isEmpty()) {
                   Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                   finish();
                }
            }
        });
    }

    private void validateAndPlaceOrder() {
        String name = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhoneNumber.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        placeOrder(name, phone, address, city);
    }

    private void placeOrder(String name, String phone, String address, String city) {
        binding.progressBarCheckout.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        String orderId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String userId = auth.getUid();
        String paymentMethod = binding.rbCod.isChecked() ? "Cash on Delivery" : "Online";

        Order order = new Order(
                orderId,
                userId,
                orderItems,
                totalPayable,
                name,
                phone,
                address,
                city,
                paymentMethod,
                System.currentTimeMillis(),
                "Pending"
        );

        firestore.collection("Orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    cartViewModel.ClearCart();
                    binding.progressBarCheckout.setVisibility(View.GONE);
                    
                    // Redirect to Success screen
                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarCheckout.setVisibility(View.GONE);
                    binding.btnPlaceOrder.setEnabled(true);
                    Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
