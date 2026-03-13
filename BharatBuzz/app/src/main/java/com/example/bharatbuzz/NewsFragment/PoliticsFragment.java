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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bharatbuzz.NewsAdapter.NewsAdapter;
import com.example.bharatbuzz.NewsAdapter.TopHeadlinesAdapter;
import com.example.bharatbuzz.NewsModel.Article;
import com.example.bharatbuzz.NewsModel.NewsResponse;
import com.example.bharatbuzz.Service.ApiClient;
import com.example.bharatbuzz.databinding.FragmentPoliticsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PoliticsFragment extends Fragment implements HomeFragment.SearchableFragment {

    private FragmentPoliticsBinding binding;
    private final ArrayList<Article> topNewsList = new ArrayList<>();
    private final ArrayList<Article> latestNewsList = new ArrayList<>();
    private final ArrayList<Article> allArticlesList = new ArrayList<>();
    private TopHeadlinesAdapter topAdapter;
    private NewsAdapter latestAdapter;

    private final String API_KEY = "93d3086ce4f64951ad4dd46ba05d0630";

    // Pagination Variables
    private int currentPage = 1;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    
    private final Handler autoSwipeHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSwipeRunnable;

    public PoliticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPoliticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpAdapters();
        setupPagination();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1; // Reset to first page
            isLastPage = false;
            fetchPoliticsNews(true);
        });

        binding.shimmerView.startShimmer();
        fetchPoliticsNews(true);
    }

    private void setUpAdapters() {
        topAdapter = new TopHeadlinesAdapter(requireContext(), topNewsList);
        binding.viewPagerTopPoliticsNews.setAdapter(topAdapter);

        latestAdapter = new NewsAdapter(requireContext(), latestNewsList);
        binding.rvPoliticsNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPoliticsNews.setAdapter(latestAdapter);
    }

    /**
     * Setup Pagination Scroll Listener
     * Note: Listener is attached to NestedScrollView (mainLayout) because RecyclerView is nested.
     */
    private void setupPagination() {
        binding.mainLayout.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Check if user has scrolled to the bottom of the NestedScrollView
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                if (!isLoading && !isLastPage) {
                    currentPage++;
                    fetchPoliticsNews(false); // Load next page
                }
            }
        });
    }

    private void fetchPoliticsNews(boolean isFirstPage) {
        isLoading = true;
        if (isFirstPage) {
            binding.swipeRefreshLayout.setRefreshing(true);
        }

        ApiClient.getApiInterface().getCategoryNews("us", "general", currentPage, pageSize, API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                        isLoading = false;
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                            binding.shimmerView.stopShimmer();
                            binding.shimmerView.setVisibility(View.GONE);
                            binding.mainLayout.setVisibility(View.VISIBLE);
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null && !articles.isEmpty()) {
                                if (isFirstPage) {
                                    allArticlesList.clear();
                                    updateUI(articles);
                                } else {
                                    latestNewsList.addAll(articles);
                                    latestAdapter.notifyDataSetChanged();
                                }
                                allArticlesList.addAll(articles);

                                // If fewer articles than pageSize are returned, it's the last page
                                if (articles.size() < pageSize) {
                                    isLastPage = true;
                                }

                                startAutoSwipe();
                            } else {
                                isLastPage = true;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                        isLoading = false;
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
                    int currentItem = binding.viewPagerTopPoliticsNews.getCurrentItem();
                    int nextItem = (currentItem + 1) % topNewsList.size();
                    binding.viewPagerTopPoliticsNews.setCurrentItem(nextItem, true);
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
            binding.textTopPoliticsHeadlines.setVisibility(View.VISIBLE);
            binding.viewPagerTopPoliticsNews.setVisibility(View.VISIBLE);
        } else {
            binding.textTopPoliticsHeadlines.setVisibility(View.GONE);
            binding.viewPagerTopPoliticsNews.setVisibility(View.GONE);
            
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
