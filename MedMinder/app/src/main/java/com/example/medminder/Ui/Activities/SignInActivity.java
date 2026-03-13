package com.example.medminder.Ui.Activities;

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
import androidx.credentials.exceptions.GetCredentialException;

import com.example.medminder.MainActivity;
import com.example.medminder.R;
import com.example.medminder.databinding.ActivitySignInBinding;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.Executor;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    CredentialManager credentialManager;
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing auth and firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }
        
        // now initializing the credential manager
        credentialManager = CredentialManager.create(this);

        // sign in with email and password
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInWithEmailAndPassword();
            }
        });

        // sign in with Google
        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInWithGoogle();
            }
        });

        // forgot Password Functionality
        binding.textForgotPassword.setOnClickListener(v ->{
            ForgotPassword();
        });

        // signup if user have not created account
        binding.tvSignup.setOnClickListener(v->{
            Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(i);
        });
    }

    private void ForgotPassword() {
        String email = binding.etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Enter your email to reset password");
            binding.etEmail.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnLogin.setEnabled(true);
        binding.btnGoogle.setEnabled(true);
    }

    // sign in with email and password
    private void SignInWithEmailAndPassword(){
        String email = binding.etEmail.getText().toString().trim();
        String Password = binding.etPassword.getText().toString().trim();

        // validation
        if (TextUtils.isEmpty(email)){
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(Password)) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }
        if (Password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnGoogle.setEnabled(false);

        // now login process begin
        auth.signInWithEmailAndPassword(email,Password)
                .addOnSuccessListener(authResult -> {
                   binding.progressBar.setVisibility(View.GONE);
                   if (auth.getCurrentUser() != null){
                       Toast.makeText(this,"Sign In Successful",Toast.LENGTH_SHORT).show();
                       Intent i = new Intent(SignInActivity.this, MainActivity.class);
                       i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(i);
                       finish();
                   }
                }).addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    private void SignInWithGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnGoogle.setEnabled(false);
        String nonce = UUID.randomUUID().toString();

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setNonce(nonce)
                .build();

        GetCredentialRequest getCredentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);

        credentialManager.getCredentialAsync(
                this,
                getCredentialRequest,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        HandleGoogleSignIn(getCredentialResponse);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG,"Error getting credential",e);
                        resetUI();
                        Toast.makeText(SignInActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void HandleGoogleSignIn(GetCredentialResponse response){
        Credential credential = response.getCredential();

        if (credential instanceof GoogleIdTokenCredential){
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
            FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
        }
        else if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)){
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
            }
            catch (Exception e){
                Log.e(TAG, "Error parsing Google ID token credential", e);
                resetUI();
                Toast.makeText(this, "Sign-In Failed: Parsing error", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            resetUI();
            Toast.makeText(this, "Unsupported credential type", Toast.LENGTH_SHORT).show();
        }
    }

    private void FirebaseSignInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Google Sign In Successful", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(SignInActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Firebase Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}