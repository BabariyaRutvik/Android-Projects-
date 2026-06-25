package com.example.quicknotes.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.quicknotes.Fragments.CalendarFragment;
import com.example.quicknotes.Fragments.HomeFragment;
import com.example.quicknotes.Fragments.SettingsFragment;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before inflating layout
        SharedPreferences sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Status bar padding on the root so content starts below the status bar
            v.setPadding(0, systemBars.top, 0, 0);
            // System nav bar padding inside bottomNavigation so icons aren't hidden
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });


        // Load HomeFragment by default
        loadFragment(new HomeFragment());
        setSelectedIcon(R.id.nav_home);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                setSelectedIcon(R.id.nav_home);
                return true;
            } else if (id == R.id.nav_calender) {
                loadFragment(new CalendarFragment());
                setSelectedIcon(R.id.nav_calender);
                return true;
            } else if (id == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                setSelectedIcon(R.id.nav_settings);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);

        super.attachBaseContext(finalContext);
    }

    // change the icon when user will click on that particular fragment
    private void setSelectedIcon(int selectedItem){
        // home
        binding.bottomNavigation.getMenu().findItem(R.id.nav_home).setIcon(R.drawable.ic_home_outline);

        // calender
        binding.bottomNavigation.getMenu().findItem(R.id.nav_calender).setIcon(R.drawable.ic_calender_outline);

        // settings
        binding.bottomNavigation.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.ic_menu_outline);


        // selected items

        // home
        if (selectedItem == R.id
                .nav_home){

            binding.bottomNavigation.getMenu().findItem(R.id.nav_home).setIcon(R.drawable.ic_home_filled);

        }
        // calender
        else if (selectedItem == R.id.nav_calender) {
            binding.bottomNavigation.getMenu().findItem(R.id.nav_calender).setIcon(R.drawable.ic_caldender_fill);
        }
        else if (selectedItem == R.id.nav_settings){
            binding.bottomNavigation.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.ic_menu_filled);
        }


    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fram_container, fragment);
        fragmentTransaction.commit();
    }
}
