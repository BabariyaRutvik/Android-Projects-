package com.example.quickbazaar.QuickActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.quickbazaar.R;
import com.example.quickbazaar.QuickActivity.AuthActivity.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent i;
                if (user != null) {
                    i = new Intent(SplashScreenActivity.this, MainActivity.class);
                } else {
                    i = new Intent(SplashScreenActivity.this, SignInActivity.class);
                }
                startActivity(i);
                finish();
            }
        }, 3000); // 3 seconds
    }
}
