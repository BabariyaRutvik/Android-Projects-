package com.example.interviewace.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.interviewace.R;
import com.example.interviewace.ViewModel.InterviewViewModel;
import com.example.interviewace.adapter.CertificateAdapter;
import com.example.interviewace.databinding.FragmentProfileBinding;
import com.example.interviewace.model.SessionItem;
import com.example.interviewace.model.User;
import com.example.interviewace.ui.activity.SignInActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private InterviewViewModel viewModel;
    private CertificateAdapter certificateAdapter;
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initializing viewmodel
        viewModel = new ViewModelProvider(this).get(InterviewViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupRecyclerView();
        LoadUserData();
        LoadStats();
        SetUpClickEvents();
    }

    private void setupRecyclerView() {
        certificateAdapter = new CertificateAdapter();
        binding.rvCertificates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCertificates.setAdapter(certificateAdapter);
    }

    private void LoadUserData() {
        FirebaseUser authUser = mAuth.getCurrentUser();
        if (authUser != null) {
            String uid = authUser.getUid();
            
            // Fetch detailed user data from Firestore
            firestore.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            UpdateProfileUI(currentUser);
                        } else {
                            // Fallback to Auth data if Firestore doc doesn't exist yet
                            binding.tvUserName.setText(authUser.getDisplayName() != null ? authUser.getDisplayName() : "User");
                            binding.tvUserEmail.setText(authUser.getEmail() != null ? authUser.getEmail() : "No Email");
                            setInitials(authUser.getDisplayName() != null ? authUser.getDisplayName() : "User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void UpdateProfileUI(User user) {
        binding.tvUserName.setText(user.getFullName());
        binding.tvUserEmail.setText(user.getEmail());
        
        // Handle empty college with "Not set" fallback
        String college = user.getCollege() != null && !user.getCollege().isEmpty() ? user.getCollege() : "Not set";
        binding.tvUserUniversity.setText(college);
        binding.tvInfoUniversity.setText(college);

        binding.tvGraduationYear.setText(user.getGraduationYear() != null && !user.getGraduationYear().isEmpty() ? user.getGraduationYear() : "Not set");
        binding.tvUserRole.setText(user.getTargetRole() != null && !user.getTargetRole().isEmpty() ? user.getTargetRole() : "Not set");
        
        setInitials(user.getFullName());
    }

    private void setInitials(String name) {
        if (name != null && !name.isEmpty()) {
            String[] parts = name.split(" ");
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) {
                    initials.append(parts[i].charAt(0));
                }
            }
            binding.tvProfileInitials.setText(initials.toString().toUpperCase());
        }
    }

    private void LoadStats() {
        String userId = mAuth.getUid();
        if (userId != null) {
            viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
                if (sessions != null) {
                    UpdateStatsUI(sessions);
                }
            });
            viewModel.loadSessions(userId);
        }
    }

    private void UpdateStatsUI(List<SessionItem> sessions) {
        int sessionsCount = sessions.size();
        binding.tvSessionsCount.setText(String.valueOf(sessionsCount));

        double totalScore = 0;
        List<SessionItem> certSessions = new ArrayList<>();

        for (SessionItem session : sessions) {
            totalScore += session.getScore();
            // A session qualifies for a certificate if the score is 75 or more
            if (session.getScore() >= 75) {
                certSessions.add(session);
            }
        }

        int avgScore = sessionsCount > 0 ? (int) (totalScore / sessionsCount) : 0;
        binding.tvAvgScore.setText(avgScore + "%");
        binding.tvCertificatesCount.setText(String.valueOf(certSessions.size()));
        
        // Update the horizontal RecyclerView with qualifying sessions
        certificateAdapter.setCertificates(certSessions);
    }

    private void SetUpClickEvents() {
        binding.rlLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.rlRate.setOnClickListener(v -> rateApp());
        binding.rlShare.setOnClickListener(v -> shareApp());
        binding.rlLogout.setOnClickListener(v -> logout());

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.rlNotifications.setOnClickListener(v -> Toast.makeText(getContext(), "Notifications settings", Toast.LENGTH_SHORT).show());
        binding.rlAbout.setOnClickListener(v -> Toast.makeText(getContext(), "InterviewAce v1.0", Toast.LENGTH_SHORT).show());
    }

    private void showEditProfileDialog() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Profile data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        
        TextInputEditText etName = view.findViewById(R.id.etEditName);
        TextInputEditText etUniversity = view.findViewById(R.id.etEditUniversity);
        TextInputEditText etYear = view.findViewById(R.id.etEditYear);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        etName.setText(currentUser.getFullName());
        etUniversity.setText(currentUser.getCollege());
        etYear.setText(currentUser.getGraduationYear());

        AlertDialog dialog = builder.setView(view).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newUniversity = etUniversity.getText().toString().trim();
            String newYear = etYear.getText().toString().trim();

            if (newName.isEmpty()) {
                etName.setError("Name cannot be empty");
                return;
            }

            dialog.dismiss();
            binding.btnEditProfile.setEnabled(false);
            
            // Update Firestore
            currentUser.setFullName(newName);
            currentUser.setCollege(newUniversity);
            currentUser.setGraduationYear(newYear);

            firestore.collection("users").document(currentUser.getUserId())
                    .set(currentUser)
                    .addOnSuccessListener(unused -> {
                        binding.btnEditProfile.setEnabled(true);
                        UpdateProfileUI(currentUser);
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnEditProfile.setEnabled(true);
                        Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "ગુજરાતી (Gujarati)", "हिन्दी (Hindi)"};
        String[] languageCodes = {"en", "gu", "hi"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Language");
        builder.setItems(languages, (dialogInterface, i) -> {
            setAppLocale(languageCodes[i]);
            Toast.makeText(getContext(), "Language changed to " + languages[i], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void setAppLocale(String languageCode) {
        LocaleListCompat localeList = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(localeList);
    }

    private void rateApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getPackageName()));
            startActivity(intent);
        }
    }

    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out InterviewAce! Download now: https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setIcon(R.drawable.ic_logout)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialogInterface, i) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
