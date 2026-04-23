package com.example.quickbazaar.BazaarFragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.quickbazaar.BazaarModel.User;
import com.example.quickbazaar.QuickActivity.AuthActivity.SignInActivity;
import com.example.quickbazaar.QuickActivity.MyOrdersActivity;
import com.example.quickbazaar.R;
import com.example.quickbazaar.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

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

        // initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        FetchUserData();

        binding.menuLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });

        binding.menuMyOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MyOrdersActivity.class);
                startActivity(intent);
            }
        });
        binding.menuAddresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Address management coming soon", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void showLogoutDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnLogout = dialogView.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            Logout();
        });

        dialog.show();
    }

    private void FetchUserData(){
        String userId = auth.getUid();

        if (userId != null){
            firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (binding == null) return;
                        
                        User user = documentSnapshot.toObject(User.class);

                         if (documentSnapshot.exists() && user != null){
                             binding.tvProfileName.setText(user.getFullName());
                             binding.tvProfileEmail.setText(user.getEmail());

                             String profileImageUrl = user.getProfileImage();
                             if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                 Glide.with(this)
                                         .load(profileImageUrl)
                                         .placeholder(R.drawable.ic_person)
                                         .error(R.drawable.ic_person)
                                         .into(binding.imgProfileAvatar);
                             } else {
                                 Glide.with(this)
                                         .load(R.drawable.ic_person)
                                         .into(binding.imgProfileAvatar);
                             }
                         }
                    }).addOnFailureListener(error ->{
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void Logout(){
        auth.signOut();
        Intent intent = new Intent(getContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
