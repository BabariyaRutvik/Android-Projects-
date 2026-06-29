package com.example.calculator;

import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.example.calculator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
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

        binding.tvResult.setShowSoftInputOnFocus(false);

        binding.btnMode.setOnClickListener(v -> {
            scientificMode = !scientificMode;

            TransitionSet transitionSet = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .setDuration(400L)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .addTransition(new ChangeBounds())
                    .addTransition(new Slide(Gravity.TOP).addTarget(binding.layoutScientific));

            TransitionManager.beginDelayedTransition(binding.keyPadContainer, transitionSet);

            if (scientificMode) {
                binding.layoutScientific.setVisibility(View.VISIBLE);
                binding.btnMode.setImageResource(R.drawable.ic_standard);
            } else {
                binding.layoutScientific.setVisibility(View.GONE);
                binding.btnMode.setImageResource(R.drawable.ic_scientific);
            }
        });
    }
}