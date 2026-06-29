package com.example.quicknotes.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quicknotes.BottomSheet.DefaultFolderBottomSheet;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivitySettingsScreenBinding;

import java.util.ArrayList;

public class SettingsScreenActivity extends AppCompatActivity {

    private ActivitySettingsScreenBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_START_OF_WEEK = "start_of_week";
    private static final String KEY_NOTIFICATION_BAR = "notification_bar";
    private static final String SECURITY_PREFS = "security_prefs";

    private ActivityResultLauncher<android.content.Intent> patternLauncher;

    private String tempSelectedLang = "en";

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);

        super.attachBaseContext(finalContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initTheme();
        
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        binding = ActivitySettingsScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWindowInsets();
        initDarkModeSwitch();
        updateDefaultFolderIndicator();
        updateStartOfWeekUI();
        initNotificationSwitch();
        initSecuritySwitch();
        setupPatternLauncher();
        setupListeners();
        updateLanguageUI();
    }

    private void initTheme() {
        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void initDarkModeSwitch() {
        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        boolean isCurrentlyDark;
        if (themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isCurrentlyDark = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);
        } else {
            isCurrentlyDark = (themeMode == AppCompatDelegate.MODE_NIGHT_YES);
        }
        binding.switchDarkMode.setChecked(isCurrentlyDark);
    }

    private void updateDefaultFolderIndicator() {
        String defaultFolder = sharedPreferences.getString("default_folder", "All");
        int color = getCategoryColor(defaultFolder);
        binding.imgFolderIndicator.setImageTintList(ColorStateList.valueOf(color));
    }

    private int getCategoryColor(String categoryKey) {
        switch (categoryKey) {
            case "All": return ContextCompat.getColor(this, R.color.primary_blue);
            case "Personal": return ContextCompat.getColor(this, R.color.badge_personal_text);
            case "Work": return ContextCompat.getColor(this, R.color.badge_work_text);
            case "Others": return ContextCompat.getColor(this, R.color.badge_others_text);
            case "Untitled_Red": return ContextCompat.getColor(this, R.color.badge_untitled_red_text);
            case "Untitled_Orange": return ContextCompat.getColor(this, R.color.badge_untitled_orange_text);
            case "Untitled_Pink": return ContextCompat.getColor(this, R.color.badge_untitled_pink_text);
            case "Untitled_Purple": return ContextCompat.getColor(this, R.color.badge_untitled_purple_text);
            case "Untitled_DarkGray": return ContextCompat.getColor(this, R.color.badge_untitled_dark_gray_text);
            case "Untitled_Gray": return ContextCompat.getColor(this, R.color.badge_untitled_gray_text);
            default: return ContextCompat.getColor(this, R.color.primary_blue);
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                updateTheme(isChecked);
            }
        });

        binding.darkModeLayout.setOnClickListener(v -> {
            boolean newState = !binding.switchDarkMode.isChecked();
            binding.switchDarkMode.setChecked(newState);
            updateTheme(newState);
        });

        binding.defaultFolderLayout.setOnClickListener(v -> {
            DefaultFolderBottomSheet bottomSheet = new DefaultFolderBottomSheet();
            bottomSheet.setOnFolderSelectedListener(categoryKey -> updateDefaultFolderIndicator());
            bottomSheet.show(getSupportFragmentManager(), "DefaultFolderBottomSheet");
        });

        binding.languageOptionLayout.setOnClickListener(v -> {
            ShowLanguageSelectionDialog();
        });

        binding.fontSizeLayout.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, FontSizeActivity.class));
        });

        binding.startWeekLayout.setOnClickListener(v -> {
            ShowStartOfWeekDialog();
        });

        binding.shareLayout.setOnClickListener(v -> {
            shareApp();
        });

        binding.privacyPolicyLayout.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.privacy_policy), Toast.LENGTH_SHORT).show();
        });

       binding.setPasswordLayout.setOnClickListener(v ->{
           handleSecurityToggle(!binding.switchPassword.isChecked());

       });
       binding.switchPassword.setOnCheckedChangeListener(((buttonView, isChecked) -> {
           if (buttonView.isPressed()){
               handleSecurityToggle(isChecked);
           }
       }));
       binding.changePasswordLayout.setOnClickListener(v->{
           SharedPreferences preferences = getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE);
           if (!preferences.getBoolean("is_enabled", false)){
               Toast.makeText(this, R.string.set_password_first, Toast.LENGTH_SHORT).show();
           }
           else {
               Intent intent = new Intent(this, PatternLockActivity.class);
               intent.putExtra(PatternLockActivity.EXTRA_MODE,PatternLockActivity.MODE_CHANGE);
               patternLauncher.launch(intent);
           }
       });

        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (!isChecked) {
                    binding.switchNotification.setChecked(true);
                    ShowNotificationTurnOffDialog();
                } else {
                    updateNotificationPref(true);
                }
            }
        });

        binding.notificationBarLayout.setOnClickListener(v -> {
            if (binding.switchNotification.isChecked()) {
                ShowNotificationTurnOffDialog();
            } else {
                binding.switchNotification.setChecked(true);
                updateNotificationPref(true);
            }
        });

        binding.toolbarSettings.setNavigationOnClickListener(v -> finish());
    }

    private void updateLanguageUI() {
        String currentLan = LanguageHelper.getLanguage(this);
        if (currentLan.equals("hi")) {
            binding.txtCurrentLanguage.setText(R.string.hindi);
        } else if (currentLan.equals("gu")) {
            binding.txtCurrentLanguage.setText(R.string.gujarati);
        } else {
            binding.txtCurrentLanguage.setText(R.string.english);
        }
    }

    private void updateStartOfWeekUI() {
        String startDay = sharedPreferences.getString(KEY_START_OF_WEEK, "Sunday");
        if (startDay.equals("Monday")) {
            binding.txtCurrentStartDay.setText(R.string.monday);
        } else {
            binding.txtCurrentStartDay.setText(R.string.sunday);
        }
    }

    private void ShowStartOfWeekDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_start_of_week, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View layoutMonday = dialogView.findViewById(R.id.layoutMonday);
        View layoutSunday = dialogView.findViewById(R.id.layoutSunday);
        androidx.appcompat.widget.AppCompatRadioButton rbMonday = dialogView.findViewById(R.id.rbMonday);
        androidx.appcompat.widget.AppCompatRadioButton rbSunday = dialogView.findViewById(R.id.rbSunday);

        String currentStartDay = sharedPreferences.getString(KEY_START_OF_WEEK, "Sunday");
        
        rbMonday.setChecked(currentStartDay.equals("Monday"));
        rbSunday.setChecked(currentStartDay.equals("Sunday"));

        layoutMonday.setOnClickListener(v -> {
            sharedPreferences.edit().putString(KEY_START_OF_WEEK, "Monday").apply();
            updateStartOfWeekUI();
            Toast.makeText(this, "Monday is set as the Start of the Week", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        layoutSunday.setOnClickListener(v -> {
            sharedPreferences.edit().putString(KEY_START_OF_WEEK, "Sunday").apply();
            updateStartOfWeekUI();
            Toast.makeText(this, "Sunday is set as the Start of the Week", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            
            layoutParams.width = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            
            layoutParams.gravity = android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL;
            layoutParams.x = (int) (16 * getResources().getDisplayMetrics().density);

            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    private void ShowLanguageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_language_selection, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View layoutEnglish = dialogView.findViewById(R.id.layoutEnglish);
        View layoutHindi = dialogView.findViewById(R.id.layoutHindi);
        View layoutGujarati = dialogView.findViewById(R.id.layoutGujarati);

        ImageView imgEnglish = dialogView.findViewById(R.id.imgEnglish);
        ImageView imgHindi = dialogView.findViewById(R.id.imgHindi);
        ImageView imgGujarati = dialogView.findViewById(R.id.imgGujarati);

        String currentLang = LanguageHelper.getLanguage(this);
        tempSelectedLang = currentLang;

        updateSelectionUI(imgEnglish, imgHindi, imgGujarati);

        layoutEnglish.setOnClickListener(v -> {
            tempSelectedLang = "en";
            updateSelectionUI(imgEnglish, imgHindi, imgGujarati);
        });

        layoutHindi.setOnClickListener(v -> {
            tempSelectedLang = "hi";
            updateSelectionUI(imgEnglish, imgHindi, imgGujarati);
        });

        layoutGujarati.setOnClickListener(v -> {
            tempSelectedLang = "gu";
            updateSelectionUI(imgEnglish, imgHindi, imgGujarati);
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (!tempSelectedLang.equals(currentLang)) {
                LanguageHelper.setLocale(this, tempSelectedLang);
            }
            dialog.dismiss();
        });

        dialog.show();

        // Ensure dialog takes up enough width and text doesn't cut
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    private void updateSelectionUI(ImageView en, ImageView hi, ImageView gu) {
        en.setVisibility(tempSelectedLang.equals("en") ? View.VISIBLE : View.GONE);
        hi.setVisibility(tempSelectedLang.equals("hi") ? View.VISIBLE : View.GONE);
        gu.setVisibility(tempSelectedLang.equals("gu") ? View.VISIBLE : View.GONE);
    }

    private void updateTheme(boolean isDarkMode) {
        int newMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME_MODE, newMode);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    private void initNotificationSwitch() {
        boolean isEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATION_BAR, true);
        binding.switchNotification.setChecked(isEnabled);
    }

    private void ShowNotificationTurnOffDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tip, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            binding.switchNotification.setChecked(true);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            updateNotificationPref(false);
            binding.switchNotification.setChecked(false);
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.show();

        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    private void updateNotificationPref(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_BAR, isEnabled).apply();
    }

    private void initSecuritySwitch() {
        SharedPreferences secPrefs = getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE);
        binding.switchPassword.setChecked(secPrefs.getBoolean("is_enabled", false));
    }

    private void setupPatternLauncher() {
        patternLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                initSecuritySwitch();
            } else {
                initSecuritySwitch();
            }
        });
    }

    private void handleSecurityToggle(boolean isChecked) {
        android.content.Intent intent = new android.content.Intent(this, PatternLockActivity.class);
        if (isChecked) {
            intent.putExtra(PatternLockActivity.EXTRA_MODE, PatternLockActivity.MODE_SET);
        } else {
            intent.putExtra(PatternLockActivity.EXTRA_MODE, PatternLockActivity.MODE_REMOVE);
        }
        patternLauncher.launch(intent);
    }

    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out QuickNotes app: com.example.quicknotes");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Text");
        startActivity(shareIntent);
    }
}
