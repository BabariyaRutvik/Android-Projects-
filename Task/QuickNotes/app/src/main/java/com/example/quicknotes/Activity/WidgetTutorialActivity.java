package com.example.quicknotes.Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quicknotes.databinding.ActivityWidgetTutorialBinding;

public class WidgetTutorialActivity extends AppCompatActivity {

    private ActivityWidgetTutorialBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWidgetTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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