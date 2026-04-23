package com.example.quickbazaar.QuickActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.quickbazaar.BazaarFragment.CartFragment;
import com.example.quickbazaar.BazaarFragment.CategoryFragment;
import com.example.quickbazaar.BazaarFragment.HomeFragment;
import com.example.quickbazaar.BazaarFragment.ProfileFragment;
import com.example.quickbazaar.BazaarModel.User;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private static final String TAG = "MainActivity";
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);

        // Fetch user name for toolbar/menu display
        fetchUserName();

        // Request Notification Permission (Required for Android 13+ and WorkManager notifications)
        requestNotificationPermission();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Quick Bazaar");
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "Quick Bazaar";
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Quick Bazaar";
            } else if (itemId == R.id.nav_categories) {
                selectedFragment = new CategoryFragment();
                title = "Categories";
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
                title = "My Cart";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Profile";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
                return true;
            }
            return false;
        });
    }

    private void fetchUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (binding == null) return;
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                                try {
                                    String firstName = user.getFullName().split(" ")[0];
                                    binding.toolbar.setTitle("Hi, " + firstName);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing name", e);
                                    binding.toolbar.setTitle("Quick Bazaar");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user name", e));
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void loadFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) {
            // If it's the home fragment, we might want to keep the "Hi, User" title
            if (fragment instanceof HomeFragment && mAuth.getCurrentUser() != null) {
                fetchUserName(); // Refresh name
            } else {
                getSupportActionBar().setTitle(title);
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
    }
}
