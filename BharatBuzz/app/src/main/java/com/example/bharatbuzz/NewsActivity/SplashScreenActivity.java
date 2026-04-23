package com.example.bharatbuzz.NewsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.bharatbuzz.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Delay for 3 seconds, then check login status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // User is logged in
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            } else {
                // User is NOT logged in
                startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
            }
            finish();
        }, 3000);
    }
}
