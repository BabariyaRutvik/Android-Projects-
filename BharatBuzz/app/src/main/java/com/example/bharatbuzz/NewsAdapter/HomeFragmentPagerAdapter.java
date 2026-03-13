package com.example.bharatbuzz.NewsAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.bharatbuzz.NewsFragment.BusinessFragment;
import com.example.bharatbuzz.NewsFragment.EntertainmentFragment;
import com.example.bharatbuzz.NewsFragment.HealthFragment;
import com.example.bharatbuzz.NewsFragment.PoliticsFragment;
import com.example.bharatbuzz.NewsFragment.SportsFragment;
import com.example.bharatbuzz.NewsFragment.TechnologyFragment;

public class HomeFragmentPagerAdapter extends FragmentStateAdapter {

    public HomeFragmentPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SportsFragment();
            case 1:
                return new TechnologyFragment();
            case 2:
                return new HealthFragment();
            case 3:
                return new BusinessFragment();
            case 4:
                return new EntertainmentFragment();
            case 5:
                return new PoliticsFragment();
            default:
                return new SportsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
