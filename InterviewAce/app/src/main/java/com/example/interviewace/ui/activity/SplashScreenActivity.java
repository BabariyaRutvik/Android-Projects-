package com.example.interviewace.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.interviewace.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 1. Check if User is already logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            } 
            // 2. Check if OnBoarding is finished
            else if (isOnBoardingFinished()) {
                startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
            } 
            // 3. Otherwise show OnBoarding
            else {
                startActivity(new Intent(SplashScreenActivity.this, OnBoardingActivity.class));
            }
            finish();
        }, 3000);
    }

    private boolean isOnBoardingFinished() {
        SharedPreferences sharedPreferences = getSharedPreferences("onBoarding", MODE_PRIVATE);
        return sharedPreferences.getBoolean("finished", false);
    }
}
