package com.example.quickbazaar.QuickActivity.AuthActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quickbazaar.BazaarModel.User;
import com.example.quickbazaar.QuickActivity.MainActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }

        // Signup to the user
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpUser();
            }
        });

        // Already have an account - go to Sign In
        binding.layoutGotoSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        
        binding.tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutGotoSignin.performClick();
            }
        });
    }

    // signup to the user
    private void SignUpUser(){
        String fullName = binding.editFullName.getText().toString().trim();
        String phoneNumber = binding.editPhone.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();

        // validation
        if (TextUtils.isEmpty(fullName)){
            binding.editFullName.setError("Full Name is Required");
            binding.editFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)){
            binding.editPhone.setError("Phone Number is Required");
            binding.editPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)){
            binding.editEmail.setError("Email is Required");
            binding.editEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)){
            binding.editPassword.setError("Password is Required");
            binding.editPassword.requestFocus();
            return;
        }
        if (phoneNumber.length() < 10) {
            binding.editPhone.setError("Enter valid 10-digit phone number");
            binding.editPhone.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.editPassword.setError("Password must be at least 6 characters");
            binding.editPassword.requestFocus();
            return;
        }

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignUp.setEnabled(false);

        // ========== FIREBASE AUTH CREATE USER ==========
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    String createdAt = String.valueOf(System.currentTimeMillis());

                    // Create User model object
                    User user = new User(userId, fullName, phoneNumber, email, createdAt);

                    // Save user to Firestore
                    firestore.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(SignupActivity.this, "Account Created Successfully. Please Login.", Toast.LENGTH_SHORT).show();
                                
                                // Sign out the user immediately so they have to login
                                firebaseAuth.signOut();
                                
                                // Redirect to SignInActivity as requested
                                Intent intent = new Intent(SignupActivity.this, SignInActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.btnSignUp.setEnabled(true);
                                Toast.makeText(SignupActivity.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSignUp.setEnabled(true);
                    Toast.makeText(SignupActivity.this, "Auth Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
