package com.example.bharatbuzz.NewsFragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bharatbuzz.NewsAdapter.NewsAdapter;
import com.example.bharatbuzz.NewsAdapter.TopHeadlinesAdapter;
import com.example.bharatbuzz.NewsModel.Article;
import com.example.bharatbuzz.NewsModel.NewsResponse;
import com.example.bharatbuzz.Service.ApiClient;
import com.example.bharatbuzz.databinding.FragmentHealthBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthFragment extends Fragment implements HomeFragment.SearchableFragment {

    private FragmentHealthBinding binding;
    private final ArrayList<Article> topNewsList = new ArrayList<>();
    private final ArrayList<Article> latestNewsList = new ArrayList<>();
    private final ArrayList<Article> allArticlesList = new ArrayList<>();
    private TopHeadlinesAdapter topAdapter;
    private NewsAdapter latestAdapter;

    private final String API_KEY = "93d3086ce4f64951ad4dd46ba05d0630";
    
    private final Handler autoSwipeHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSwipeRunnable;

    public HealthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHealthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpAdapters();
        
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchHealthNews);
        
        binding.shimmerView.startShimmer();
        fetchHealthNews();
    }

    private void setUpAdapters() {
        topAdapter = new TopHeadlinesAdapter(requireContext(), topNewsList);
        binding.viewPagerTopHealthNews.setAdapter(topAdapter);

        latestAdapter = new NewsAdapter(requireContext(), latestNewsList);
        binding.rvHealthNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHealthNews.setAdapter(latestAdapter);
    }

    private void fetchHealthNews() {
        ApiClient.getApiInterface().getCategoryNews("us", "health", 1, 50, API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                            binding.shimmerView.stopShimmer();
                            binding.shimmerView.setVisibility(View.GONE);
                            binding.mainLayout.setVisibility(View.VISIBLE);
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null && !articles.isEmpty()) {
                                allArticlesList.clear();
                                allArticlesList.addAll(articles);
                                
                                updateUI(articles);
                                
                                startAutoSwipe();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable throwable) {
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                            binding.shimmerView.stopShimmer();
                            binding.shimmerView.setVisibility(View.GONE);
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching news", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(List<Article> articles) {
        topNewsList.clear();
        latestNewsList.clear();
        
        int limit = Math.min(articles.size(), 5);
        for (int i = 0; i < limit; i++) {
            topNewsList.add(articles.get(i));
        }
        for (int i = limit; i < articles.size(); i++) {
            latestNewsList.add(articles.get(i));
        }
        
        topAdapter.notifyDataSetChanged();
        latestAdapter.updateData(latestNewsList);
    }

    private void startAutoSwipe() {
        stopAutoSwipe();
        autoSwipeRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && topNewsList.size() > 0) {
                    int currentItem = binding.viewPagerTopHealthNews.getCurrentItem();
                    int nextItem = (currentItem + 1) % topNewsList.size();
                    binding.viewPagerTopHealthNews.setCurrentItem(nextItem, true);
                    autoSwipeHandler.postDelayed(this, 4000);
                }
            }
        };
        autoSwipeHandler.postDelayed(autoSwipeRunnable, 4000);
    }

    private void stopAutoSwipe() {
        if (autoSwipeRunnable != null) {
            autoSwipeHandler.removeCallbacks(autoSwipeRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (topNewsList.size() > 0) {
            startAutoSwipe();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoSwipe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSwipe();
        binding = null;
    }

    @Override
    public void onSearchQuery(String query) {
        if (query == null || query.isEmpty()) {
            updateUI(allArticlesList);
            binding.textTopHealthHeadlines.setVisibility(View.VISIBLE);
            binding.viewPagerTopHealthNews.setVisibility(View.VISIBLE);
        } else {
            binding.textTopHealthHeadlines.setVisibility(View.GONE);
            binding.viewPagerTopHealthNews.setVisibility(View.GONE);
            
            ArrayList<Article> filteredList = new ArrayList<>();
            String filterPattern = query.toLowerCase().trim();
            for (Article item : allArticlesList) {
                if (item.getTitle() != null && item.getTitle().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
            latestAdapter.updateData(filteredList);
        }
    }
}
