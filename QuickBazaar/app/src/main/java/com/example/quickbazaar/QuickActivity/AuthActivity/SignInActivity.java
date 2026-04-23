package com.example.quickbazaar.QuickActivity.AuthActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException;

import com.example.quickbazaar.BazaarModel.User;
import com.example.quickbazaar.QuickActivity.MainActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ActivitySignInBinding;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.Executor;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private ActivitySignInBinding binding;

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Redirect if already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
            return;
        }

        credentialManager = CredentialManager.create(this);

        binding.btnSignIn.setOnClickListener(v -> SignInWithEmailAndPassword());
        binding.btnGoogleSignIn.setOnClickListener(v -> SignInWithGoogle());
        binding.textForgotPassword.setOnClickListener(v -> ForgotPassword());
        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignupActivity.class));
        });
    }

    private void SignInWithEmailAndPassword() {
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            binding.editEmail.setError("Email is required");
            binding.editEmail.requestFocus();
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            binding.editEmail.setError("Please enter a valid @gmail.com address");
            binding.editEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editPass.setError("Password is Required");
            binding.editPass.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    navigateToMain("SignIn Successfully");
                }).addOnFailureListener(error -> {
                    resetUI();
                    Toast.makeText(this, "SignIn Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void SignInWithGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignIn.setEnabled(false);

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
        } else if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                Log.e(TAG, "Error parsing Google ID token", e);
                resetUI();
                Toast.makeText(this, "Error processing account information", Toast.LENGTH_SHORT).show();
            }
        } else {
            resetUI();
            Log.e(TAG, "Unknown credential type: " + credential.getType());
            Toast.makeText(this, "Unknown credential type", Toast.LENGTH_SHORT).show();
        }
    }

    private void FirebaseSignInWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    if (mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String email = mAuth.getCurrentUser().getEmail();
                        String name = mAuth.getCurrentUser().getDisplayName();
                        String profileImage = (mAuth.getCurrentUser().getPhotoUrl() != null) ? mAuth.getCurrentUser().getPhotoUrl().toString() : "";

                        // Enforce @gmail.com for Google Sign-In as well
                        if (email != null && !email.endsWith("@gmail.com")) {
                            mAuth.signOut();
                            resetUI();
                            Toast.makeText(this, "Only @gmail.com accounts are allowed.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        CheckUserAndNavigate(uid, name, email, profileImage);
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

    private void CheckUserAndNavigate(String uid, String name, String email, String profileImage) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && !document.exists()) {
                    Log.d(TAG, "New user detected. Saving record to Firestore.");
                    String createdAt = String.valueOf(System.currentTimeMillis());
                    User user = new User(uid, name, "", email, createdAt, profileImage);
                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> navigateToMain("Registration Successful!"))
                            .addOnFailureListener(e -> {
                                resetUI();
                                Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.d(TAG, "Existing user. Navigating to Home.");
                    navigateToMain("Welcome Back!");
                }
            } else {
                resetUI();
                Log.e(TAG, "Firestore data fetch failed", task.getException());
                Toast.makeText(this, "Error checking user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain(String message) {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void ForgotPassword() {
        String email = binding.editEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.editEmail.setError("Email is Required");
            binding.editEmail.requestFocus();
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            binding.editEmail.setError("Please enter a valid @gmail.com address");
            binding.editEmail.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(error -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSignIn.setEnabled(true);
        binding.btnGoogleSignIn.setEnabled(true);
    }
}
