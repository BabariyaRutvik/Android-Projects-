package com.example.bharatbuzz.NewsActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.bharatbuzz.NewsModel.Article;
import com.example.bharatbuzz.R;
import com.example.bharatbuzz.databinding.ActivityFullScreenNewsBinding;

public class FullScreenNewsActivity extends BaseActivity {

    private ActivityFullScreenNewsBinding binding;
    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity edge-to-edge for a better full-screen experience
        makeStatusBarTransparent();
        
        binding = ActivityFullScreenNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve article data passed from the previous screen
        article = (Article) getIntent().getSerializableExtra("article");

        if (article != null) {
            displayNewsData();
        } else {
            Toast.makeText(this, "Error: News details not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        initListeners();
        setupWebView();
        setupBackPressHandler();
    }

    /**
     * Populates the UI elements with article information
     */
    private void displayNewsData() {
        binding.txtTitle.setText(article.getTitle());
        
        String source = (article.getSource() != null) ? article.getSource().getName() : "Unknown Source";
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
                .placeholder(R.color.light_gray)
                .into(binding.imgNews);
    }

    /**
     * Configures the WebView for reading the full article
     */
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
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                
                // Suppress non-critical errors (trackers, favicons) to avoid annoying toasts
                if (request.isForMainFrame()) {
                    Toast.makeText(FullScreenNewsActivity.this, "Error loading article", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnReadFull.setOnClickListener(v -> {
            if (article != null && article.getUrl() != null && !article.getUrl().isEmpty()) {
                binding.webViewContainer.setVisibility(View.VISIBLE);
                binding.webView.loadUrl(article.getUrl());
            } else {
                Toast.makeText(this, "Link not available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.webViewToolbar.setNavigationOnClickListener(v -> {
            binding.webViewContainer.setVisibility(View.GONE);
            binding.webView.stopLoading();
        });
    }

    /**
     * Handles back navigation when the WebView is visible
     */
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

    /**
     * Makes the status bar transparent to let the image show behind it.
     */
    private void makeStatusBarTransparent() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);

        // Ensure status bar icons are visible based on theme
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isDarkMode);
    }
}
