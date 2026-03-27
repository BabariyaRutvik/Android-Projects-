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
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.interviewace.R;
import com.example.interviewace.databinding.ActivitySignUpBinding;
import com.example.interviewace.model.User;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.Executor;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    private static final String TAG = "SignUpActivity";
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    CredentialManager credentialManager;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // initializing firebase Auth and firebase Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        // initializing credential manager
        credentialManager = CredentialManager.create(this);

        // checking the if user has already logged in or not
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
            return;
        }

        // signup to the user
        binding.btnSignUp.setOnClickListener(v-> {
            SignUp();
        });
        // google sign up

        binding.btnGoogleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInWithGoogle();
            }
        });

        // now if user have already accounted then open the login activity

        binding.textLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(i);
                finish();
            }
        });

        // Password visibility toggles
        binding.ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());
        binding.ivConfirmPasswordToggle.setOnClickListener(v -> toggleConfirmPasswordVisibility());

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

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            binding.etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye);
        } else {
            binding.etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_off);
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.getText().length());
    }

    private void resetUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSignUp.setEnabled(true);
        binding.btnGoogleSignUp.setEnabled(true);
        binding.btnGoogleSignUp.setClickable(true);
    }

    // now siggnup to the user
    private void SignUp(){
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String college = binding.etCollege.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();


        // validating tto the fields

        if (TextUtils.isEmpty(fullName)){
            binding.etFullName.setError("Full Name is Required");
            binding.etFullName.requestFocus();
            return;

        }
        if (TextUtils.isEmpty(email)){
            binding.etEmail.setError("Email is Required");
            binding.etEmail.requestFocus();
            return;

        }
        if (TextUtils.isEmpty(college)){
            binding.etCollege.setError("College is Required");
            binding.etCollege.requestFocus();
            return;

        }
        if (TextUtils.isEmpty(password)){
            binding.etPassword.setError("Password is Required");
            binding.etPassword.requestFocus();
            return;

        }
        if (TextUtils.isEmpty(confirmPassword)){
            binding.etConfirmPassword.setError("Confirm Password is Required");
            binding.etConfirmPassword.requestFocus();
            return;

        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Password does not match");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        // password validation if user has must enter 6 chaaracter
        if (password.length() < 6) {
            binding.etPassword.setError("Password must be 6 characters long");
            binding.etPassword.requestFocus();
            return;

        }
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignUp.setEnabled(false);


        // now signup process start
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    // Passing empty string for targetRole as it is no longer required during signup
                    User user = new User(uid, fullName, email, college, "");

                    firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                goToMain();

                            })
                            .addOnFailureListener(e -> {
                                resetUI();
                                Toast.makeText(SignUpActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            });

                })
                .addOnFailureListener(e->{
                    resetUI();
                    Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });




    }
    private void SignInWithGoogle(){
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignUp.setEnabled(false);

        // for Security Point of View
        String nonce = UUID.randomUUID().toString();

        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setNonce(nonce)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();



        Executor executor = ContextCompat.getMainExecutor(this);

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        HandleGoogleSignUp(response);

                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Error getting credential", e);
                        resetUI();

                        if (!(e instanceof GetCredentialCancellationException)){
                            Toast.makeText(SignUpActivity.this, "Error getting credential", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );
    }
    // handling google signup
    private void HandleGoogleSignUp(GetCredentialResponse response){
        Credential credential = response.getCredential();

        if (credential instanceof GoogleIdTokenCredential){
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
           FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());

        }
        else if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)){
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                FirebaseSignInWithGoogle(googleIdTokenCredential.getIdToken());
            }catch (Exception e){
                Log.e(TAG, "Error getting credential", e);
                resetUI();
                Toast.makeText(SignUpActivity.this, "Error getting credential", Toast.LENGTH_SHORT).show();

            }
        }
        else {
            Log.e(TAG, "Unknown credential type: " + credential.getType());
            resetUI();
        }
    }
    private void FirebaseSignInWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (mAuth.getCurrentUser() != null){
                        String uid = mAuth.getCurrentUser().getUid();
                        String email = mAuth.getCurrentUser().getEmail();
                        String name = mAuth.getCurrentUser().getDisplayName();


                        // checking if new user
                        boolean isNewUser = authResult.getAdditionalUserInfo().isNewUser();


                        if (isNewUser){
                            SaveGoogleUserToFirestore(uid, email, name);
                        }
                        else {
                            Toast.makeText(SignUpActivity.this, "Welcome back " + name, Toast.LENGTH_SHORT).show();

                            goToMain();
                        }
                    }
                })
                .addOnFailureListener(e ->{
                    resetUI();
                    Toast.makeText(SignUpActivity.this, "Firebase Auth Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    // now saving the google sinup user to firestore
    private void SaveGoogleUserToFirestore(String uid, String email, String name){
        String profilePic =
                mAuth.getCurrentUser()
                        .getPhotoUrl() != null
                         ? mAuth.getCurrentUser().getPhotoUrl().toString()
                        : "";


        User user = new User(uid, name,email,profilePic);
        firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUpActivity.this, "Google Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    goToMain();
                })
                .addOnFailureListener(e->{
                    resetUI();
                    Toast.makeText(SignUpActivity.this, "Error saving user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });

    }
    private void goToMain() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
