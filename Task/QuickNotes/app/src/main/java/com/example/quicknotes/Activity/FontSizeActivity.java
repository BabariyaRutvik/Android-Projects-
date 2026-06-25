package com.example.quicknotes.Activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityFontSizeBinding;

public class FontSizeActivity extends AppCompatActivity {

    private ActivityFontSizeBinding binding;
    private float currentScale = 1.0f;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);

        super.attachBaseContext(finalContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFontSizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbarFontSize.setNavigationOnClickListener(v -> finish());

        initSeekBar();
    }

    private void initSeekBar() {
        currentScale = FontSizeHelper.getFontScale(this);
        int progress = 1;
        if (currentScale < 1.0f) progress = 0;
        else if (currentScale > 1.0f) progress = 2;

        binding.seekBarFontSize.setProgress(progress);
        updatePreview(currentScale);

        binding.seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newScale = 1.0f;
                if (progress == 0) newScale = 0.85f;
                else if (progress == 2) newScale = 1.15f;
                
                if (newScale != currentScale) {
                    currentScale = newScale;
                    FontSizeHelper.setFontScale(FontSizeActivity.this, currentScale);
                    updatePreview(currentScale);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updatePreview(float scale) {
        // Heading 1 base 28sp
        binding.txtHeading1.setTextSize(28 * scale);
        // Heading 2 base 22sp
        binding.txtHeading2.setTextSize(22 * scale);
        // Sample base 18sp
        binding.txtSample.setTextSize(18 * scale);
    }
}
