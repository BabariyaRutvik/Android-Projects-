package com.example.quickbazaar.BazaarFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quickbazaar.BazaarAdapter.CartAdapter;
import com.example.quickbazaar.BazaarModel.CartItem;
import com.example.quickbazaar.BazaarViewModel.CartViewModel;
import com.example.quickbazaar.QuickActivity.CheckoutActivity;
import com.example.quickbazaar.QuickActivity.MainActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.FragmentCartBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;
    private List<CartItem> cartItems;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CartViewModel.class);
        setupRecyclerView();
        observeViewModel();

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                Intent intent = new Intent(getContext(), CheckoutActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnStartShopping.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                BottomNavigationView bottomNav = mainActivity.findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }
        });
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        adapter = new CartAdapter(getContext(), cartItems, new CartAdapter.OnCartItemClickListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                viewModel.UpdateQuantity(item.getProduct().getId(), newQuantity);
            }

            @Override
            public void onRemoveItem(CartItem item) {
                viewModel.UpdateQuantity(item.getProduct().getId(), 0);
            }
        });

        binding.recyclerviewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewCart.setAdapter(adapter);
    }

    private void observeViewModel() {
        binding.progressBarCart.setVisibility(View.VISIBLE);
        viewModel.getCartItem().observe(getViewLifecycleOwner(), items -> {
            binding.progressBarCart.setVisibility(View.GONE);
            cartItems.clear();
            if (items != null) {
                cartItems.addAll(items);
                viewModel.CalculateTotalList(items);
            }
            adapter.notifyDataSetChanged();
            updateUI(items);
        });

        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                binding.tvTotalPrice.setText("₹" + String.format("%.2f", total));
            }
        });
    }

    private void updateUI(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.recyclerviewCart.setVisibility(View.GONE);
            binding.layoutBottomCheckout.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.recyclerviewCart.setVisibility(View.VISIBLE);
            binding.layoutBottomCheckout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
