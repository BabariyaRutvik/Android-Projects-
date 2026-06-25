package com.example.quicknotes.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.quicknotes.Activity.ArchievedNotesActivity;
import com.example.quicknotes.Activity.RecycleBinActivity;
import com.example.quicknotes.Activity.SettingsScreenActivity;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private NoteViewModel noteViewModel;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        setupClickListeners();
        observeCounts();
    }

    private void setupClickListeners() {
        // Launch Recycle Bin Activity
        binding.cardRecycleBin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RecycleBinActivity.class);
            startActivity(intent);
        });

        // Archive Card
        binding.cardArchive.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ArchievedNotesActivity.class);
            startActivity(intent);
        });

        // Settings List Options
        binding.layoutFeedback.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Feedback clicked", Toast.LENGTH_SHORT).show());
            

        binding.layoutSettingsDetail.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), SettingsScreenActivity.class);
            startActivity(intent);
        });


            
        binding.layoutWidget.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Widget clicked", Toast.LENGTH_SHORT).show());
    }

    private void observeCounts() {
        // Observe deleted notes count
        noteViewModel.getDeletedCount().observe(getViewLifecycleOwner(), count -> {
            binding.txtRecycleBinCount.setText(String.valueOf(count));
        });

        // Observe archived notes count
        noteViewModel.getArchivedCount().observe(getViewLifecycleOwner(), count -> {
            binding.txtArchiveCount.setText(String.valueOf(count));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
