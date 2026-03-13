package com.example.quickbazaar.QuickActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.quickbazaar.BazaarFragment.CartFragment;
import com.example.quickbazaar.BazaarFragment.CategoryFragment;
import com.example.quickbazaar.BazaarFragment.HomeFragment;
import com.example.quickbazaar.BazaarFragment.ProfileFragment;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private static final String TAG = "MainActivityFCM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // 1. AUTOMATIC TOPIC SUBSCRIPTION (Like News Apps)
        // This ensures the device receives any notification sent to the "general" topic
        FirebaseMessaging.getInstance().subscribeToTopic("general")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to 'general' topic automatically");
                    }
                });

        // 2. Request Notification Permission (Required for Android 13+)
        requestNotificationPermission();

        // 3. Keep token updated in Firestore for personalized notifications
        syncFCMToken();

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

    private void syncFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                saveTokenToFirestore(task.getResult());
            }
        });
    }

    private void saveTokenToFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("fcmToken", token);
            FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                    .set(data, com.google.firebase.firestore.SetOptions.merge());
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
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
    }
}
