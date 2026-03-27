package com.example.interviewace.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.interviewace.R;
import com.example.interviewace.databinding.ActivitySignInBinding;
import com.example.interviewace.model.User;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.Executor;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    ActivitySignInBinding binding;
    FirebaseFirestore firestore;
    CredentialManager credentialManager;
    FirebaseAuth auth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);


        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> SignInWithEmailPassword());
        binding.btnGoogle.setOnClickListener(v -> SignInWithGoogle());
        binding.textForgotPassword.setOnClickListener(v -> ForgotPassword());
        binding.textSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });

        binding.ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye);
        } else {
            binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye_off);
        }
        isPasswordVisible = !isPasswordVisible;
        binding.etPassword.setSelection(binding.etPassword.getText().length());
    }

    private void ForgotPassword() {
        String email = binding.etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Enter Your Email to reset your Password");
            binding.etEmail.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnLogin.setEnabled(true);
        binding.btnGoogle.setEnabled(true);
    }

    private void SignInWithGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogle.setEnabled(false);

        String nonce = UUID.randomUUID().toString();
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setNonce(nonce)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build();

        GetCredentialRequest getCredentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);
        credentialManager.getCredentialAsync(this, getCredentialRequest, null, executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        HandledGoogleSignIn(response);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Credential Manager Error: " + e.getMessage());
                        Toast.makeText(SignInActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                        resetUI();
                    }
                }
        );
    }

    private void HandledGoogleSignIn(GetCredentialResponse response) {
        Credential credential = response.getCredential();
        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
            FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            resetUI();
            Toast.makeText(this, "Unknown credential type", Toast.LENGTH_SHORT).show();
        }
    }

    private void FirebaseSignInWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    if (auth.getCurrentUser() != null) {
                        String uid = auth.getCurrentUser().getUid();
                        String email = auth.getCurrentUser().getEmail();
                        String name = auth.getCurrentUser().getDisplayName();
                        String profilePic = auth.getCurrentUser().getPhotoUrl() != null ? auth.getCurrentUser().getPhotoUrl().toString() : "";

                        CheckUserAndNavigate(uid, name, email, profilePic);
                    } else {
                        resetUI();
                        Toast.makeText(SignInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Auth failed", e);
                    resetUI();
                    Toast.makeText(SignInActivity.this, "Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void CheckUserAndNavigate(String uid, String name, String email, String profilePic) {
        firestore.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (!task.getResult().exists()) {
                    User user = new User(uid, name, email, profilePic);
                    firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> navigateToMain())
                            .addOnFailureListener(e -> {
                                resetUI();
                                Toast.makeText(SignInActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    navigateToMain();
                }
            } else {
                resetUI();
                Toast.makeText(this, "Error checking user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain() {
        Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SignInWithEmailPassword() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> navigateToMain())
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(SignInActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
