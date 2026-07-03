package com.example.calculator;

import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean scientificMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Prevent system keyboard
        binding.textResult.setShowSoftInputOnFocus(false);

        binding.imageSwitch.setOnClickListener(v -> {
            scientificMode = !scientificMode;

            // Transition duration
            long duration = 600L;
            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

            //  Define the Transition
            TransitionSet set = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds())
                    .addTransition(new Slide(Gravity.TOP).addTarget(binding.layoutScientific.getRoot())) 
                    .setDuration(duration)
                    .setInterpolator(interpolator);

            //  Begin transition on the keypad container
            TransitionManager.beginDelayedTransition(binding.keypadContainer, set);

            //  Update UI visibility
            binding.layoutScientific.getRoot().setVisibility(scientificMode ? View.VISIBLE : View.GONE);
            
            // Hide History icon in scientific mode as requested
            binding.imageHistory.setVisibility(scientificMode ? View.GONE : View.VISIBLE);
            
            // Keep Title and Category Add icon visible (removed visibility changes for them)

            // 4. Update icon
            binding.imageSwitch.setImageResource(scientificMode ? R.drawable.ic_standard : R.drawable.ic_scientific);
            
            updateResultFieldForMode();
        });
    }

    private void updateResultFieldForMode() {
        binding.textResult.requestFocus();
        
        if (scientificMode) {

            binding.textResult.setText("");
        } else {
            String currentText = binding.textResult.getText().toString();
            if (currentText.isEmpty()) {
                binding.textResult.setText("0");
            }
        }
        binding.textResult.setSelection(binding.textResult.getText().length());
    }
}
