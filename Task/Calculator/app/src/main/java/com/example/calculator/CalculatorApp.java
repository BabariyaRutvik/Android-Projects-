package com.example.calculator;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class CalculatorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        SharedPreferences preferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int savedTheme = preferences.getInt("selected_theme", 2);

        if (savedTheme == 0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (savedTheme == 1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
