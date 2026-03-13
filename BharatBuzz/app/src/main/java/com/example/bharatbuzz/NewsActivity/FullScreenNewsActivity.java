package com.example.bharatbuzz.NewsActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bharatbuzz.NewsModel.Article;
import com.example.bharatbuzz.databinding.ActivityFullScreenNewsBinding;

public class FullScreenNewsActivity extends AppCompatActivity {

    private ActivityFullScreenNewsBinding binding;
    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get Article object from Intent
        article = (Article) getIntent().getSerializableExtra("article");

        if (article != null) {
            displayNewsData();
        } else {
            Toast.makeText(this, "Error loading news details", Toast.LENGTH_SHORT).show();
            finish();
        }

        initListeners();
        setupWebView();
        setupBackPressHandler();
    }

    private void displayNewsData() {
        binding.txtTitle.setText(article.getTitle());
        
        String source = (article.getSource() != null) ? article.getSource().getName() : "Unknown";
        String author = (article.getAuthor() != null && !article.getAuthor().isEmpty()) ? article.getAuthor() : "Unknown Author";
        String time = (article.getPublishedAt() != null && article.getPublishedAt().length() >= 10) 
                ? article.getPublishedAt().substring(0, 10) : "";
        
        binding.txtAuthorSourceTime.setText("By " + author + " | " + source + " - " + time);
        
        String content = article.getContent();
        if (content == null || content.isEmpty()) {
            content = article.getDescription();
        }
        binding.txtContent.setText(content);

        Glide.with(this)
                .load(article.getUrlToImage())
                .into(binding.imgNews);
    }

    private void setupWebView() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(FullScreenNewsActivity.this, "Error loading page", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnReadFull.setOnClickListener(v -> {
            if (article.getUrl() != null && !article.getUrl().isEmpty()) {
                // Show WebView container and load URL
                binding.webViewContainer.setVisibility(View.VISIBLE);
                binding.webView.loadUrl(article.getUrl());
            } else {
                Toast.makeText(this, "URL not available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.webViewToolbar.setNavigationOnClickListener(v -> {
            // Hide WebView container
            binding.webViewContainer.setVisibility(View.GONE);
            binding.webView.stopLoading();
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webViewContainer.getVisibility() == View.VISIBLE) {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack();
                    } else {
                        binding.webViewContainer.setVisibility(View.GONE);
                    }
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
