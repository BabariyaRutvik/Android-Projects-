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
import androidx.credentials.exceptions.GetCredentialException;

import com.example.quickbazaar.BazaarModel.User;
import com.example.quickbazaar.QuickActivity.MainActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.UUID;
import java.util.concurrent.Executor;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private FirebaseFirestore db;

    private static final String TAG = "SignInActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }

        credentialManager = CredentialManager.create(this);

        binding.btnSignIn.setOnClickListener(v -> SignInWithEmailAndPassword());
        binding.btnGoogleSignIn.setOnClickListener(v -> SignInWithGoogle());
        binding.textForgotPassword.setOnClickListener(v -> ForgotPassword());
        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignupActivity.class));
        });
    }

    private void SignInWithEmailAndPassword(){
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPass.getText().toString();

        if (TextUtils.isEmpty(email)){
            binding.editEmail.setError("Email is required");
            binding.editEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)){
            binding.editPass.setError("Password is Required");
            binding.editPass.requestFocus();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignIn.setEnabled(false);
        
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(authResult ->{
                    binding.progressBar.setVisibility(View.GONE);
                    if (mAuth.getCurrentUser() != null) {
                        // We DON'T call SaveUserToFireStore here because it would overwrite
                        // the fullName from SignupActivity with an empty string.
                        
                        Toast.makeText(this, "SignIn Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        resetUI();
                        Toast.makeText(this, "User session error", Toast.LENGTH_SHORT).show();
                    }

                }).addOnFailureListener(error ->{
                    resetUI();
                    Toast.makeText(this, "SignIn Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void SignInWithGoogle(){
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignIn.setEnabled(false);
        
        String nonce = UUID.randomUUID().toString();

        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id)) 
                .setFilterByAuthorizedAccounts(false)
                .setNonce(nonce)
                .build();

        GetCredentialRequest getCredentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);
        
        credentialManager.getCredentialAsync(
                this,
                getCredentialRequest,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        HandledGoogleSignIn(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "getCredentialAsync error", e);
                        resetUI();
                        Toast.makeText(SignInActivity.this, "Google Sign-In Cancelled or Failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void HandledGoogleSignIn(GetCredentialResponse result){
        Credential credential = result.getCredential();

        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
            FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
        } else if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                Log.e(TAG, "Error parsing Google ID token credential", e);
                resetUI();
                Toast.makeText(this, "Sign-In Failed: Parsing error", Toast.LENGTH_SHORT).show();
            }
        } else {
            resetUI();
            Toast.makeText(this, "Sign-In Failed: Unexpected response type", Toast.LENGTH_SHORT).show();
        }
    }

    private void FirebaseSignInWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String name = mAuth.getCurrentUser().getDisplayName();
                        String email = mAuth.getCurrentUser().getEmail();

                        // For Google sign-in, we use Merge to avoid overwriting existing fields (like phone)
                        // but ensure new Google users have a record.
                        SaveUserToFireStoreMerge(uid, name, email);
                        
                        Toast.makeText(SignInActivity.this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        resetUI();
                        Toast.makeText(SignInActivity.this, "User session error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Auth failed", e);
                    resetUI();
                    Toast.makeText(SignInActivity.this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void SaveUserToFireStoreMerge(String uid, String name, String email) {
        String createdAt = String.valueOf(System.currentTimeMillis());
        // Using a Map or checking existence first is safer, but Merge is a good start.
        User user = new User(uid, name, "", email, createdAt);

        db.collection("users")
                .document(uid)
                .set(user, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user to Firestore", e));
    }

    private void ForgotPassword() {
       String email = binding.editEmail.getText().toString().trim();

       if (TextUtils.isEmpty(email)){
           binding.editEmail.setError("Email is Required");
           binding.editEmail.requestFocus();
           return;
       }
       binding.progressBar.setVisibility(View.VISIBLE);
       mAuth.sendPasswordResetEmail(email)
               .addOnSuccessListener(unused -> {
                   binding.progressBar.setVisibility(View.GONE);
                   Toast.makeText(this, "Reset Link Sent To Your Email", Toast.LENGTH_SHORT).show();
               })
               .addOnFailureListener(error ->{
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
