package com.example.calculator.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.databinding.ActivityGstCalculatorBinding;

public class GSTCalculatorActivity extends AppCompatActivity {

    private ActivityGstCalculatorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGstCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ensure Status Bar icons are dark on light background
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }
}
