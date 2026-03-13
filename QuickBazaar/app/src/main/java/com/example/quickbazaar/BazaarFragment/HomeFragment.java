package com.example.quickbazaar.BazaarFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarAdapter.CategoryAdapter;
import com.example.quickbazaar.BazaarAdapter.ProductAdapter;
import com.example.quickbazaar.BazaarModel.Category;
import com.example.quickbazaar.BazaarModel.Product;
import com.example.quickbazaar.BazaarModel.User;
import com.example.quickbazaar.BazaarViewModel.CartViewModel;
import com.example.quickbazaar.MessageService.FirebaseMessageService;
import com.example.quickbazaar.QuickActivity.AIChatActivity;
import com.example.quickbazaar.QuickActivity.ProductFullScreenActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.FragmentHomeBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private CartViewModel cartViewModel;

    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;

    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private ActivityResultLauncher<IntentSenderRequest> gpsLauncher;

    private static final String TAG = "HomeFragment";

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotificationBadge();
        }
    };

    public HomeFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                .RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
            
            if ((fineLocationGranted != null && fineLocationGranted) || 
                (coarseLocationGranted != null && coarseLocationGranted)) {
                checkGPSAndGetLocation();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gpsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        getLastLocation();
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "GPS is required for location", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        setupCategory();
        setupProduct();
        setupSearch();
        setupBanner();

        fetchUserData();
        FetchCategory();
        fetchProduct();
        fetchBannerFromFirestore();

        // Setup notification click and badge
        binding.imgNotification.setOnClickListener(v -> resetNotificationCount());
        updateNotificationBadge();

        // Setup Location Click
        binding.textUserLocation.setOnClickListener(v -> requestLocationPermission());

        // Setup AI Chat Click
        binding.imgAiChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AIChatActivity.class);
            startActivity(intent);
        });
    }

    private void requestLocationPermission() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void checkGPSAndGetLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(requireActivity(), locationSettingsResponse -> {
            getLastLocation();
        });

        task.addOnFailureListener(requireActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                    gpsLauncher.launch(intentSenderRequest);
                } catch (Exception sendEx) {
                    Log.e(TAG, "Error showing GPS dialog", sendEx);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (binding == null) return;
                    if (location != null) {
                        updateAddressUI(location.getLatitude(), location.getLongitude());
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateAddressUI(double lat, double lon) {
        if (!isAdded() || binding == null) return;
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty() && binding != null) {
                String city = addresses.get(0).getLocality();
                String area = addresses.get(0).getSubLocality();
                String fullAddress = (area != null ? area + ", " : "") + city;
                binding.textUserLocation.setText(fullAddress);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Location updated: " + fullAddress, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
        }
    }

    private void updateNotificationBadge() {
        if (getContext() == null || binding == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int count = prefs.getInt("notification_count", 0);

        if (count > 0) {
            binding.tvNotificationBadge.setText(String.valueOf(count));
            binding.tvNotificationBadge.setVisibility(View.VISIBLE);
        } else {
            binding.tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void resetNotificationCount() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("notification_count", 0).apply();
        updateNotificationBadge();
        if (getContext() != null) {
            Toast.makeText(getContext(), "Notifications Cleared", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                    notificationReceiver, new IntentFilter(FirebaseMessageService.ACTION_NOTIFICATION_RECEIVED));
        }
        updateNotificationBadge();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(notificationReceiver);
        }
    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            firestore.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (binding != null && documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                                binding.textUserGreetings.setText("Hi, " + user.getFullName() + " 👋");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user data", e));
        }
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (productAdapter != null) productAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBanner() {
        binding.cardBanner.setOnClickListener(v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Promotion Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBannerFromFirestore() {
        firestore.collection("Banners").limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null && !queryDocumentSnapshots.isEmpty()) {
                        String imageUrl = queryDocumentSnapshots.getDocuments().get(0).getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Find the ImageView inside the cardBanner frame
                            ImageView bannerImageView = null;
                            for (int i = 0; i < binding.cardBanner.getChildCount(); i++) {
                                View child = binding.cardBanner.getChildAt(i);
                                if (child instanceof ImageView) {
                                    bannerImageView = (ImageView) child;
                                    break;
                                }
                            }
                            
                            if (bannerImageView != null) {
                                Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_grocery_cart)
                                    .into(bannerImageView);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching banner", e));
    }

    private void setupCategory() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        binding.recycerviewCategory.setAdapter(categoryAdapter);
        binding.recycerviewCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupProduct() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(getContext(), ProductFullScreenActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }

            @Override
            public void onAddToCart(Product product) {
                cartViewModel.AddToCart(product);
                if (getContext() != null) {
                    Toast.makeText(getContext(), product.getName() + " added to bag", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.recycerviewPopularProducts.setAdapter(productAdapter);
        binding.recycerviewPopularProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void FetchCategory(){
        firestore.collection("Category").orderBy("priority").get()
                .addOnCompleteListener(task -> {
                    if (binding != null && task.isSuccessful()){
                        List<Category> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            newList.add(documentSnapshot.toObject(Category.class));
                        }
                        categoryList.clear();
                        categoryList.addAll(newList);
                        categoryAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void fetchProduct(){
        firestore.collection("products").whereEqualTo("isPopular", true).limit(20).get()
                .addOnCompleteListener(task -> {
                    if (binding != null && task.isSuccessful()){
                        List<Product> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Product product = documentSnapshot.toObject(Product.class);
                            if (product.getId() == null) product.setId(documentSnapshot.getId());
                            newList.add(product);
                        }
                        productList.clear();
                        productList.addAll(newList);
                        productAdapter.updateData(newList);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
