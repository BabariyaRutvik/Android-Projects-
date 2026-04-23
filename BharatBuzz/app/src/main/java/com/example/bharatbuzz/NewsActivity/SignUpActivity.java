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
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.bharatbuzz.R;
import com.example.bharatbuzz.databinding.ActivitySignUpBinding;
import com.example.bharatbuzz.NewsModel.User;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Handles user registration via Email/Password and Google Sign-In.
 */
public class SignUpActivity extends BaseActivity {

    private ActivitySignUpBinding binding;
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
            return;
        }

        initListeners();
    }

    private void initListeners() {
        binding.btnSignUp.setOnClickListener(v -> handleSignUp());
        binding.btnGoogleSignUp.setOnClickListener(v -> signInWithGoogle());
        binding.tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        });
    }

    private void resetUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSignUp.setEnabled(true);
        binding.btnGoogleSignUp.setEnabled(true);
    }

    /**
     * Validates input and creates a new user account with Firebase Auth.
     */
    private void handleSignUp() {
        String fullName = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            binding.etName.setError("Full Name is Required");
            binding.etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etPhone.setError("Phone Number is Required");
            binding.etPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is Required");
            binding.etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is Required");
            binding.etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, fullName, phone, email);
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Saves user profile information to Firestore after successful registration.
     */
    private void saveUserToFirestore(String uid, String fullName, String phone, String email) {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        User user = new User(uid, fullName, phone, email, createdAt);

        firestore.collection("users").document(uid).set(user)
                .addOnSuccessListener(unused -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Account Created. Please Sign In.", Toast.LENGTH_LONG).show();
                    // Sign out to force fresh login and profile verification
                    mAuth.signOut(); 
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Starts the Google Sign-In flow using Credential Manager.
     */
    private void signInWithGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignUp.setEnabled(false);

        String nonce = UUID.randomUUID().toString();
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setNonce(nonce)
                .setAutoSelectEnabled(false)
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
                        resetUI();
                        if (!(e instanceof GetCredentialCancellationException)) {
                            Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String email = mAuth.getCurrentUser().getEmail();
                        String name = mAuth.getCurrentUser().getDisplayName();

                        if (authResult.getAdditionalUserInfo() != null && authResult.getAdditionalUserInfo().isNewUser()) {
                            saveGoogleUserToFirestore(uid, email, name);
                        } else {
                            Toast.makeText(this, "Welcome back, " + name, Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveGoogleUserToFirestore(String uid, String email, String name) {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        User user = new User(uid, name, "", email, createdAt);
        firestore.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Google Sign-Up Successful!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
