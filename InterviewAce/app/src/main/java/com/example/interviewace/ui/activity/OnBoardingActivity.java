package com.example.interviewace.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.interviewace.R;
import com.example.interviewace.adapter.OnBoardingAdapter;
import com.example.interviewace.databinding.ActivityOnBoardingBinding;
import com.example.interviewace.model.OnBoardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnBoardingActivity extends AppCompatActivity {

    private ActivityOnBoardingBinding binding;
    private OnBoardingAdapter onBoardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setUpButtons();
    }

    private void setupViewPager() {
        List<OnBoardingItem> items = new ArrayList<>();

        // Slide 1
        items.add(new OnBoardingItem(
                R.drawable.ic_microphone,
                "Practice Like a Real Interview",
                "AI asks you questions just like a real interviewer would"
        ));

        // Slide 2
        items.add(new OnBoardingItem(
                R.drawable.ic_feedback,
                "Get Instant AI Feedback",
                "Know exactly what you did right and what needs improvement"
        ));

        // Slide 3
        items.add(new OnBoardingItem(
                R.drawable.ic_certificate,
                "Earn Skill Certificates",
                "Share on LinkedIn and stand out from other candidates"
        ));

        onBoardingAdapter = new OnBoardingAdapter(items);
        binding.viewPager.setAdapter(onBoardingAdapter);

        // Connecting dots indicator
        binding.dotsIndicator.attachTo(binding.viewPager);

        // Page change listener
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == items.size() - 1) {
                    binding.btnNext.setText("GET STARTED");
                    binding.textSkip.setVisibility(View.GONE);
                } else {
                    binding.btnNext.setText("NEXT");
                    binding.textSkip.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setUpButtons() {
        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < onBoardingAdapter.getItemCount() - 1) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                FinishOnBoarding();
            }
        });

        binding.textSkip.setOnClickListener(view -> FinishOnBoarding());
    }

    private void FinishOnBoarding() {
        SharedPreferences sharedPreferences = getSharedPreferences("onBoarding", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("finished", true).apply();
        
        Intent i = new Intent(OnBoardingActivity.this, SignInActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
