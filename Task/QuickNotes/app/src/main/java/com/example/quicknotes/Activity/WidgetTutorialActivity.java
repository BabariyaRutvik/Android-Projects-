package com.example.quicknotes.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quicknotes.databinding.ActivityWidgetTutorialBinding;

public class WidgetTutorialActivity extends AppCompatActivity {

    private ActivityWidgetTutorialBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityWidgetTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        boolean fromSelection = getIntent().getBooleanExtra("from_selection",false);

        binding.toolbarTutorial.setNavigationOnClickListener(v-> finish());

        binding.btnGotIt.setOnClickListener(v ->{
            if (fromSelection){
                setResult(RESULT_OK);
            }
            finish();
        });
    }
}