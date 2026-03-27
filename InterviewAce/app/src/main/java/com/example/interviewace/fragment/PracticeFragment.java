package com.example.interviewace.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.interviewace.R;
import com.example.interviewace.adapter.PracticeRoleAdapter;
import com.example.interviewace.databinding.FragmentPracticeBinding;
import com.example.interviewace.model.RoleItem;
import com.example.interviewace.ui.activity.Pre_interview_Activity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PracticeFragment extends Fragment {

    private FragmentPracticeBinding binding;
    private PracticeRoleAdapter practiceRoleAdapter;
    private List<RoleItem> roleItems = new ArrayList<>();
    private FirebaseFirestore firestore;

    private RoleItem selectedRole = null;
    private String selectedDifficulty = "Medium";

    public PracticeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        setUpRecyclerView();
        setUpButtonState();
        disableStateButton();
        setUpChipSelection();
        loadRolesFromFirestore();

        // Back button functionality
        binding.imageBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_home);
        });
    }

    private void setUpRecyclerView() {
        practiceRoleAdapter = new PracticeRoleAdapter(getContext(), roleItems, roleItem -> {
            selectedRole = roleItem;
            enableStateButton();
        });

        binding.rvRoles.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvRoles.setAdapter(practiceRoleAdapter);
    }

    private void loadRolesFromFirestore() {
        firestore.collection("Role")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    roleItems.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RoleItem item = document.toObject(RoleItem.class);
                        roleItems.add(item);
                    }
                    practiceRoleAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading roles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setUpButtonState() {
        binding.btnStartInterview.setOnClickListener(view -> {
            if (selectedRole != null) {
                Intent intent = new Intent(getActivity(), Pre_interview_Activity.class);
                intent.putExtra("roleName", selectedRole.getRoleName());
                intent.putExtra("difficulty", selectedDifficulty);
                intent.putExtra("iconName", selectedRole.getIconName());
                startActivity(intent);
            }
        });
    }

    private void enableStateButton() {
        binding.btnStartInterview.setEnabled(true);
        binding.btnStartInterview.setAlpha(1f);
        binding.btnStartInterview.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_bg));
    }

    private void disableStateButton() {
        binding.btnStartInterview.setEnabled(false);
        binding.btnStartInterview.setAlpha(0.5f);
        binding.btnStartInterview.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_button_interview_disabled));
    }

    private void setUpChipSelection() {
        // Set default difficulty to match checked chip in XML
        selectedDifficulty = "Medium";

        binding.chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int selectedId = checkedIds.get(0);
            if (selectedId == R.id.chip_easy) {
                selectedDifficulty = "Easy";
            } else if (selectedId == R.id.chip_medium) {
                selectedDifficulty = "Medium";
            } else if (selectedId == R.id.chip_hard) {
                selectedDifficulty = "Hard";
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
