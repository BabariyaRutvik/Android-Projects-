package com.example.quickbazaar.QuickActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quickbazaar.BazaarAdapter.OrderAdapter;
import com.example.quickbazaar.BazaarModel.Order;
import com.example.quickbazaar.databinding.ActivityMyOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private ActivityMyOrdersBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private static final String TAG = "MyOrdersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.btnBack.setOnClickListener(view -> finish());

        SetUpRecyerview();
        FetchOrderData();
    }

    private void SetUpRecyerview() {
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, orderList);
        binding.recyclerviewOrder.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerviewOrder.setAdapter(adapter);
    }

    private void FetchOrderData() {
        String userId = auth.getUid();

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarOrder.setVisibility(View.VISIBLE);
        binding.layoutNoOrders.setVisibility(View.GONE);

        firestore.collection("Orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBarOrder.setVisibility(View.GONE);
                    orderList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Order order = documentSnapshot.toObject(Order.class);
                        // Ensure ID is set if it's not in the object fields
                        if (order.getOrderId() == null) {
                            order.setOrderId(documentSnapshot.getId());
                        }
                        orderList.add(order);
                    }

                    // Sort locally by timestamp (Descending)
                    Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        binding.layoutNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutNoOrders.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBarOrder.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching orders: ", e);
                    Toast.makeText(MyOrdersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // If it's an index error, the message will contain a link
                });
    }
}
