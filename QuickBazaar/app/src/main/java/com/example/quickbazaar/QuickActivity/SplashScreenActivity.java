package com.example.quickbazaar.QuickActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.quickbazaar.R;
import com.example.quickbazaar.QuickActivity.AuthActivity.SignInActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        LottieAnimationView animationView = findViewById(R.id.lottie_anim_);
        animationView.playAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start your app main activity
                Intent i = new Intent(SplashScreenActivity.this, SignInActivity.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, 3000); // 3 seconds
    }
}
