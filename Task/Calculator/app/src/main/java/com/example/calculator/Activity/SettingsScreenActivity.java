package com.example.calculator.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculator.BottomSheet.HistoryCapacityBottomSheet;
import com.example.calculator.BottomSheet.ThemeBottomSheetDialog;
import com.example.calculator.R;
import com.example.calculator.databinding.ActivitySettingsScreenBinding;

public class SettingsScreenActivity extends AppCompatActivity implements ThemeBottomSheetDialog.OnThemeChangeListener, HistoryCapacityBottomSheet.OnCapacityChangeListener {

    private ActivitySettingsScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int savedTheme = preferences.getInt("selected_theme", 2);

        if (savedTheme == 0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (savedTheme == 1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        binding = ActivitySettingsScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // for vibration toggle off
        SharedPreferences sharedPreferences = getSharedPreferences("vibration_prefs", MODE_PRIVATE);
        boolean isVibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true); // true by default
        binding.switchVibration.setChecked(isVibrationEnabled);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.layoutTheme.setOnClickListener(v -> {
            ThemeBottomSheetDialog themeBottomSheetDialog = new ThemeBottomSheetDialog();
            themeBottomSheetDialog.show(getSupportFragmentManager(), "ThemeBottomSheetDialog");
        });

        binding.layoutLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageSelectionActivity.class);
            startActivity(intent);
        });

        // vibration off
        binding.switchVibration.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("vibration_enabled", isChecked);
            editor.apply();
        }));

        // scientific mode
        //Get current scientific mode setting and set the switch position
        SharedPreferences sharedPrefsSci = getSharedPreferences("scientific_prefs", MODE_PRIVATE);
        boolean isSciModeEnabled = sharedPrefsSci.getBoolean("scientific_enabled", false); // false by default
        binding.switchScientific.setChecked(isSciModeEnabled);

        binding.switchScientific.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPrefsSci.edit();
            editor.putBoolean("scientific_enabled", isChecked);
            editor.apply();
        }));

        // result to keep history
        SharedPreferences historyPrefs = getSharedPreferences("history_prefs", MODE_PRIVATE);
        int currentCapacity = historyPrefs.getInt("history_capacity", -1);
        updateHistoryCapacityText(currentCapacity);

        binding.layoutHistoryLimit.setOnClickListener(v -> {
            HistoryCapacityBottomSheet historyCapacityBottomSheet = new HistoryCapacityBottomSheet();
            historyCapacityBottomSheet.show(getSupportFragmentManager(), "HistoryCapacityBottomSheet");
        });

        // Share App
        binding.layoutShare.setOnClickListener(v -> shareApp());

        // Rate App, Privacy Policy, More Apps (Toasts for now)
        binding.layoutRate.setOnClickListener(v -> showToast(getString(R.string.rate_app)));
        binding.layoutPrivacy.setOnClickListener(v -> showToast(getString(R.string.privacy_policy)));
        binding.layoutMoreApps.setOnClickListener(v -> showToast(getString(R.string.more_apps)));
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = getString(R.string.share_message, getPackageName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose One"));
        } catch (Exception e) {
            showToast("Error sharing app");
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void updateHistoryCapacityText(int capacity) {
        if (capacity == -1) {
            binding.textHistoryValue.setText(R.string.infinity);
        } else {
            binding.textHistoryValue.setText(String.valueOf(capacity));
        }
    }

    @Override
    public void onCapacityChanged(int capacity) {
        updateHistoryCapacityText(capacity);
    }





    @Override
    public void onThemeChanged(int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);
        recreate();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


}
