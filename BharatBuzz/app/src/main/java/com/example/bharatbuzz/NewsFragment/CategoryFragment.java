package com.example.bharatbuzz.NewsFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.bharatbuzz.R;
import com.example.bharatbuzz.databinding.FragmentCategoryBinding;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Click listeners for each category card
        binding.cardSports.setOnClickListener(v -> 
                Navigation.findNavController(requireView()).navigate(R.id.nav_sports));

        binding.cardTechnology.setOnClickListener(v -> 
                Navigation.findNavController(requireView()).navigate(R.id.nav_technology));

        binding.cardBusiness.setOnClickListener(v -> 
                Navigation.findNavController(requireView()).navigate(R.id.nav_business));

        binding.cardHealth.setOnClickListener(v -> 
                Navigation.findNavController(requireView()).navigate(R.id.nav_health));

        binding.cardMovies.setOnClickListener(v -> 
                Navigation.findNavController(requireView()).navigate(R.id.nav_entertainment));

        binding.cardPolitics.setOnClickListener(v -> 
                Navigation.findNavController(requireView()).navigate(R.id.nav_politics));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
