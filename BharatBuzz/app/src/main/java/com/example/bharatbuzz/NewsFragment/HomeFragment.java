package com.example.bharatbuzz.NewsFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bharatbuzz.NewsAdapter.HomeFragmentPagerAdapter;
import com.example.bharatbuzz.R;
import com.example.bharatbuzz.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final String[] tabTitles = {"Sports", "Technology", "Health", "Business", "Entertainment", "Politics"};
    private final int[] tabColors = {
            R.color.sports,
            R.color.technology,
            R.color.health,
            R.color.business,
            R.color.entertainment,
            R.color.politics
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HomeFragmentPagerAdapter adapter = new HomeFragmentPagerAdapter(this);
        binding.viewPagerCategories.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPagerCategories,
                (tab, position) -> {
                }
        ).attach();

        setCustomTabs();
        setupSearch();
    }

    private void setupSearch() {
        binding.searchView.setOnSearchClickListener(v -> binding.txtAppName.setVisibility(View.GONE));
        binding.searchView.setOnCloseListener(() -> {
            binding.txtAppName.setVisibility(View.VISIBLE);
            return false;
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void performSearch(String query) {
        // Get current fragment from ViewPager2
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + binding.viewPagerCategories.getCurrentItem());
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).onSearchQuery(query);
        }
    }

    private void setCustomTabs() {
        for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
            if (tab != null) {
                View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
                TextView tabText = customView.findViewById(R.id.tabText);
                tabText.setText(tabTitles[i]);
                tabText.getBackground().setTint(ContextCompat.getColor(requireContext(), tabColors[i]));
                tab.setCustomView(customView);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface SearchableFragment {
        void onSearchQuery(String query);
    }
}
