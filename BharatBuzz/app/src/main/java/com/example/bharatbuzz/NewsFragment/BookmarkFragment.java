package com.example.bharatbuzz.NewsFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bharatbuzz.Database.ArticleDatabase;
import com.example.bharatbuzz.NewsAdapter.NewsAdapter;
import com.example.bharatbuzz.NewsModel.Article;
import com.example.bharatbuzz.databinding.FragmentBookmarkBinding;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {

    private FragmentBookmarkBinding binding;
    private NewsAdapter adapter;
    private ArrayList<Article> bookmarkList;
    private ArticleDatabase database;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = ArticleDatabase.getInstance(getContext());
        bookmarkList = new ArrayList<>();
        
        setupRecyclerView();
        loadBookmarks();
        setupSearch();
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(getContext(), bookmarkList);
        binding.rvBookmarks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookmarks.setAdapter(adapter);
    }

    private void loadBookmarks() {
        List<Article> list = database.articleDao().getAllBookmarks();
        bookmarkList.clear();
        if (list != null && !list.isEmpty()) {
            bookmarkList.addAll(list);
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.rvBookmarks.setVisibility(View.VISIBLE);
        } else {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.rvBookmarks.setVisibility(View.GONE);
        }
        adapter.updateData(bookmarkList);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                checkEmptyAfterFilter();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                checkEmptyAfterFilter();
                return false;
            }
        });
    }

    private void checkEmptyAfterFilter() {
        if (adapter.getItemCount() == 0) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
