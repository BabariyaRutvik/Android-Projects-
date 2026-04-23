package com.example.bharatbuzz.NewsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.bharatbuzz.R;
import com.example.bharatbuzz.databinding.ActivitySignInBinding;
import com.example.bharatbuzz.NewsModel.User;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Handles user login via Email/Password and Google Sign-In.
 */
public class SignInActivity extends BaseActivity {

    private static final String TAG = "SignInActivity";
    private ActivitySignInBinding binding;
    private FirebaseFirestore firestore;
    private CredentialManager credentialManager;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);

        // Redirect to main if already logged in
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
            return;
        }

        initListeners();
    }

    private void initListeners() {
        binding.btnSignIn.setOnClickListener(v -> signInWithEmailPassword());
        binding.btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        binding.tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
    }

    /**
     * Sends a password reset email to the user.
     */
    private void handleForgotPassword() {
        String email = binding.etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Enter your email to reset password");
            binding.etEmail.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSignIn.setEnabled(true);
        binding.btnGoogleSignIn.setEnabled(true);
    }

    /**
     * Initiates Google Sign-In flow.
     */
    private void signInWithGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignIn.setEnabled(false);

        String nonce = UUID.randomUUID().toString();
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setNonce(nonce)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);
        credentialManager.getCredentialAsync(this, request, null, executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        handleGoogleResponse(response);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Google Sign-In Error", e);
                        Toast.makeText(SignInActivity.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                        resetUI();
                    }
                }
        );
    }

    private void handleGoogleResponse(GetCredentialResponse response) {
        Credential credential = response.getCredential();
        
        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                Log.e(TAG, "Error parsing Google ID Token", e);
                resetUI();
                Toast.makeText(this, "Credential Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            resetUI();
            Log.e(TAG, "Unexpected credential type: " + credential.getType());
            Toast.makeText(this, "Unexpected credential type", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (auth.getCurrentUser() != null) {
                        String uid = auth.getCurrentUser().getUid();
                        String email = auth.getCurrentUser().getEmail();
                        String name = auth.getCurrentUser().getDisplayName();
                        checkUserInFirestore(uid, name, email);
                    } else {
                        resetUI();
                        Toast.makeText(this, "Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Checks if a Google user exists in Firestore; creates entry if missing.
     */
    private void checkUserInFirestore(String uid, String name, String email) {
        firestore.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (!task.getResult().exists()) {
                    String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    User user = new User(uid, name, "", email, createdAt);
                    firestore.collection("users").document(uid).set(user, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> navigateToMain())
                            .addOnFailureListener(e -> {
                                resetUI();
                                Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Traditional email and password login.
     */
    private void signInWithEmailPassword() {
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
                    Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
