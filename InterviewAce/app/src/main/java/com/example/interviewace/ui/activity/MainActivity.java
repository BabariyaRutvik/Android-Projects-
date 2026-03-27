package com.example.interviewace.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.interviewace.R;
import com.example.interviewace.databinding.ActivityMainBinding;
import com.example.interviewace.util.NetworkUtils;
import com.example.interviewace.worker.DailyReminderWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ConnectivityManager.NetworkCallback networkCallback;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                    scheduleAllReminders();
                } else {
                    Toast.makeText(this, "Please enable notifications to get interview reminders", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerNetworkCallback();
        checkNetworkConnection();
        checkNotificationPermission();

        // Initialize NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Setup BottomNavigationView with NavController
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
            
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                updateNoInternetUI(destination.getId());
                checkNetworkConnection();
            });
        }

        binding.layoutNoInternet.btnRetry.setOnClickListener(v -> checkNetworkConnection());

        // Initial schedule attempt
        scheduleAllReminders();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void scheduleAllReminders() {
        // scheduling daily reminders to the notification 10.am 3.pm and 9.pm stted
        scheduleReminder(10, "MorningReminder_10AM");
        scheduleReminder(15, "AfternoonReminder_3PM");
        scheduleReminder(21, "EveningReminder_9PM");
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    runOnUiThread(() -> binding.layoutNoInternet.getRoot().setVisibility(View.GONE));
                }

                @Override
                public void onLost(@NonNull Network network) {
                    runOnUiThread(() -> checkNetworkConnection());
                }
            };
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
        }
    }

    private void checkNetworkConnection() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            binding.layoutNoInternet.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.layoutNoInternet.getRoot().setVisibility(View.GONE);
        }
    }

    private void updateNoInternetUI(int destinationId) {
        if (destinationId == R.id.nav_home) {
            binding.layoutNoInternet.tvNoInternetTitle.setText("Home Offline");
            binding.layoutNoInternet.tvNoInternetDesc.setText("We can't load your personalized dashboard without internet.");
            binding.layoutNoInternet.ivNoInternet.setImageResource(R.drawable.ic_nav_home);
        } else if (destinationId == R.id.nav_practice) {
            binding.layoutNoInternet.tvNoInternetTitle.setText("Practice Offline");
            binding.layoutNoInternet.tvNoInternetDesc.setText("AI interviews require an active connection to generate questions.");
            binding.layoutNoInternet.ivNoInternet.setImageResource(R.drawable.ic_nav_practice);
        } else if (destinationId == R.id.nav_certificate) {
            binding.layoutNoInternet.tvNoInternetTitle.setText("Certificates Offline");
            binding.layoutNoInternet.tvNoInternetDesc.setText("Connect to view and download your earned certificates.");
            binding.layoutNoInternet.ivNoInternet.setImageResource(R.drawable.ic_nav_certificate);
        } else if (destinationId == R.id.nav_profile) {
            binding.layoutNoInternet.tvNoInternetTitle.setText("Profile Offline");
            binding.layoutNoInternet.tvNoInternetDesc.setText("You need internet to view or update your profile information.");
            binding.layoutNoInternet.ivNoInternet.setImageResource(R.drawable.ic_nav_profile);
        } else if (destinationId == R.id.nav_process) {
            binding.layoutNoInternet.tvNoInternetTitle.setText("Progress Offline");
            binding.layoutNoInternet.tvNoInternetDesc.setText("Connect to sync your latest practice stats.");
            binding.layoutNoInternet.ivNoInternet.setImageResource(R.drawable.ic_nav_progress);
        }
    }

    private void scheduleReminder(int hour, String uniqueWorkName) {
        Calendar calendar = Calendar.getInstance();
        long nowMillis = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long initialDelay;
        
        if (calendar.getTimeInMillis() <= nowMillis) {
            // If it's within 15 minutes past the target time, trigger soon (grace period)
            if (nowMillis - calendar.getTimeInMillis() < 15 * 60 * 1000) {
                initialDelay = 1 * 60 * 1000; // 1 minute delay
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                initialDelay = calendar.getTimeInMillis() - nowMillis;
            }
        } else {
            initialDelay = calendar.getTimeInMillis() - nowMillis;
        }

        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(uniqueWorkName)
                .build();

        // Using REPLACE to ensure the new grace-period logic is applied immediately
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
        }
    }
}
