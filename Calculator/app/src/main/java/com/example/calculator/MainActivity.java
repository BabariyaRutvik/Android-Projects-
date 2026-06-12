package com.example.calculator;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.ChangeBounds;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.example.calculator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean scientificMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Prevent system keyboard
        binding.textResult.setShowSoftInputOnFocus(false);

        binding.imageSwitch.setOnClickListener(v -> {
            scientificMode = !scientificMode;

            // Transition duration
            long duration = 600L;
            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

            // 1. Define the Transition
            // ChangeBounds handles the standard buttons moving "down to top"
            // Slide(TOP) handles scientific buttons moving "top to bottom"
            TransitionSet set = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds()) // Smoothly shifts standard buttons and resizes Results
                    .addTransition(new Slide(Gravity.TOP).addTarget(binding.layoutScientific)) 
                    .setDuration(duration)
                    .setInterpolator(interpolator);

            // 2. Begin transition on the keypad container
            TransitionManager.beginDelayedTransition(binding.keypadContainer, set);

            // 3. Update UI visibility
            // layout_weight in activity_main handles the responsive vertical shifting
            binding.layoutScientific.setVisibility(scientificMode ? View.VISIBLE : View.GONE);

            // 4. Update icon
            binding.imageSwitch.setImageResource(scientificMode ? R.drawable.ic_switch_standard : R.drawable.ic_switch_scientific);
        });
    }
}