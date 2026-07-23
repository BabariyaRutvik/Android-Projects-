package com.example.calculator;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class CalculatorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Load and apply saved language
        SharedPreferences langPrefs = getSharedPreferences("language_prefs", MODE_PRIVATE);
        String savedLang = langPrefs.getString("selected_language", null);
        if (savedLang != null) {
            LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(savedLang);
            AppCompatDelegate.setApplicationLocales(appLocales);
        }
        
        SharedPreferences preferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int savedTheme = preferences.getInt("selected_theme", 2);

        if (savedTheme == 0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (savedTheme == 1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }
}
