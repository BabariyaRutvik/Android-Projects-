package com.example.interviewace.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.interviewace.R;
import com.example.interviewace.databinding.ActivitySessionDetailsBinding;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;

public class ActivitySessionDetails extends AppCompatActivity {

    private ActivitySessionDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySessionDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getting data from the intent
        String role = getIntent().getStringExtra("role");
        String date = getIntent().getStringExtra("date");
        int score = getIntent().getIntExtra("score", 0);
        int streak = getIntent().getIntExtra("streak", 0);
        ArrayList<String> skills = getIntent().getStringArrayListExtra("skills");

        // now setting up data to the UI
        binding.tvDetailRole.setText(role);
        binding.tvDetailDate.setText(date);
        binding.tvDetailScore.setText(String.valueOf(score));
        binding.pbScore.setProgress(score);

        String streakText = streak + " Days";
        binding.tvStatStreak.setText(streakText);

        // determine status and feedback
        setupFeedback(score);

        // setup skills chips
        setupSkills(skills);

        // setup click listeners
        setupListeners();
    }

    private void setupFeedback(int score) {
        if (score >= 80) {
            binding.tvStatStatus.setText("Excellent");
            binding.tvStatStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            binding.tvScoreFeedback.setText("Outstanding Performance! You Are Ready.");
            binding.tvScoreFeedback.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            binding.tvScoreFeedback.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green)).withAlpha(20));
        } else if (score >= 50) {
            binding.tvStatStatus.setText("Passed");
            int orangeColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
            binding.tvStatStatus.setTextColor(orangeColor);
            binding.tvScoreFeedback.setText("Good Work! Keep practicing to improve further.");
            binding.tvScoreFeedback.setTextColor(orangeColor);
            binding.tvScoreFeedback.setBackgroundTintList(ColorStateList.valueOf(orangeColor).withAlpha(20));
        } else {
            binding.tvStatStatus.setText("Needs Practice");
            int redColor = ContextCompat.getColor(this, R.color.error_red);
            binding.tvStatStatus.setTextColor(redColor);
            binding.tvScoreFeedback.setText("Keep going! Consistency is the key to mastery.");
            binding.tvScoreFeedback.setTextColor(redColor);
            binding.tvScoreFeedback.setBackgroundTintList(ColorStateList.valueOf(redColor).withAlpha(20));
        }
    }

    private void setupSkills(ArrayList<String> skills) {
        if (skills != null && !skills.isEmpty()) {
            binding.cgDetailSkills.removeAllViews();
            for (String skill : skills) {
                Chip chip = new Chip(this);
                chip.setText(skill);
                chip.setChipBackgroundColorResource(R.color.light_main_bg);
                chip.setTextColor(ContextCompat.getColor(this, R.color.black));
                chip.setChipStrokeWidth(0);
                binding.cgDetailSkills.addView(chip);
            }
        }
    }

    private void setupListeners() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnDone.setOnClickListener(v -> finish());

        binding.ivShare.setOnClickListener(v -> {
            String roleName = binding.tvDetailRole.getText().toString();
            String scorePercent = binding.tvDetailScore.getText().toString();
            
            String shareMessage = "I just completed a mock interview for " + roleName + 
                                 " on InterviewAce and scored " + scorePercent + "%! 🚀";
            
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "My Interview Result");
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(intent, "Share via"));
        });
    }
}
