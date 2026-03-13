package com.example.bharatbuzz.NewsActivity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.bharatbuzz.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); 
            return insets;
        });

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            // Standard setup for UI syncing
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
            
            // Override the listener to disable state restoration.
            // This ensures that clicking a tab always takes you to the base fragment of that tab.
            bottomNavigationView.setOnItemSelectedListener(item -> {
                NavOptions options = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(false) // Disable restoring the "Sports" or other detail states
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), false, false)
                        .build();
                
                try {
                    navController.navigate(item.getItemId(), null, options);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            });
        }
    }
}
