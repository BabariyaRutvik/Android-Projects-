package com.example.bharatbuzz.NewsFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.bharatbuzz.NewsActivity.MainActivity;
import com.example.bharatbuzz.NewsActivity.SignInActivity;
import com.example.bharatbuzz.Notification.WorkScheduler;
import com.example.bharatbuzz.NewsModel.User;
import com.example.bharatbuzz.Service.LocaleHelper;
import com.example.bharatbuzz.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Constants for local caching to prevent blinking
    private static final String KEY_USER_NAME = "cached_user_name";
    private static final String KEY_USER_EMAIL = "cached_user_email";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        // 1. Load cached data immediately (Prevents blinking during theme switch)
        applyCachedUserData();

        // 2. Fetch fresh data from Firestore in background
        loadUserDataFromFirestore();

        setupListeners();
        updateLanguageUI();
    }

    /**
     * Loads the name and email from SharedPreferences instantly.
     */
    private void applyCachedUserData() {
        String cachedName = sharedPreferences.getString(KEY_USER_NAME, "");
        String cachedEmail = sharedPreferences.getString(KEY_USER_EMAIL, "");

        if (!cachedName.isEmpty()) {
            binding.tvRealName.setText(cachedName);
        }
        if (!cachedEmail.isEmpty()) {
            binding.tvUserEmail.setText(cachedEmail);
        }
    }

    /**
     * Fetches data from Firebase and updates both the UI and the local cache.
     */
    private void loadUserDataFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (binding == null) return;

                        String finalName;
                        String finalEmail = currentUser.getEmail();

                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            finalName = (user != null) ? user.getFullName() : currentUser.getDisplayName();
                        } else {
                            finalName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "BharatBuzz User";
                        }

                        // Update UI
                        binding.tvRealName.setText(finalName);
                        binding.tvUserEmail.setText(finalEmail);

                        // Save to Cache to prevent blinking next time
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_USER_NAME, finalName);
                        editor.putString(KEY_USER_EMAIL, finalEmail);
                        editor.apply();
                    });
        }
    }

    private void setupListeners() {
        // Dark Mode Logic
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);

        binding.switchDarkMode.setOnCheckedChangeListener(null);
        binding.switchDarkMode.setChecked(isDarkMode);

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == sharedPreferences.getBoolean("DarkMode", false)) return;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DarkMode", isChecked);
            editor.apply();

            // When this is called, the activity restarts.
            // Because we used applyCachedUserData() in onViewCreated, the name won't blink.
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Notifications Logic
        boolean notificationsEnabled = sharedPreferences.getBoolean("Notifications", true);
        binding.switchNotifications.setChecked(notificationsEnabled);
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Notifications", isChecked);
            editor.apply();

            if (isChecked) {
                WorkScheduler.scheduleDailyNewsNotifications(requireContext());
                Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                WorkScheduler.cancelAllNotifications(requireContext());
                Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        binding.layoutLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.cardLogout.setOnClickListener(v -> showSignOutDialog());

        binding.layoutAbout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("About BharatBuzz")
                    .setMessage("BharatBuzz v1.0\n\nA modern news application delivering the latest updates in multiple languages.\n\nDeveloped by Rutvik Babariya.")
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void showSignOutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out from BharatBuzz?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    // Clear cache on sign out
                    sharedPreferences.edit().remove(KEY_USER_NAME).remove(KEY_USER_EMAIL).apply();

                    mAuth.signOut();
                    Intent intent = new Intent(requireActivity(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    Toast.makeText(getContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateLanguageUI() {
        String currentLangCode = sharedPreferences.getString("My_Lang", "en");
        String langName = "English";
        if (currentLangCode.equals("hi")) langName = "Hindi";
        else if (currentLangCode.equals("gu")) langName = "Gujarati";
        binding.tvCurrentLanguage.setText(langName);
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Hindi", "Gujarati"};
        String[] langCodes = {"en", "hi", "gu"};

        int checkedItem = 0;
        String currentCode = sharedPreferences.getString("My_Lang", "en");
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentCode)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLang = langCodes[which];
                    setLocale(selectedLang);
                    dialog.dismiss();
                })
                .create().show();
    }

    private void setLocale(String lang) {
        LocaleHelper.setLocale(requireContext(), lang);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("My_Lang", lang);
        editor.apply();

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}