package com.example.calculator.Activity;

import android.os.Bundle;

import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Adapter.LanguageSelectionAdapter;
import com.example.calculator.Model.LanguageSelection;
import com.example.calculator.R;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionActivity extends AppCompatActivity {

    private LanguageSelectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_language_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> applyLanguage());

        setupRecyclerView();
    }

    private void applyLanguage() {
        if (adapter != null) {
            String selectedLang = adapter.getSelectedLanguageCode();
            if (selectedLang != null) {
                LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(selectedLang);
                AppCompatDelegate.setApplicationLocales(appLocales);
                finish();
            }
        }
    }

    private void setupRecyclerView() {
        List<LanguageSelection> languages = new ArrayList<>();
        String currentLang = "en";
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (!currentLocales.isEmpty()) {
            currentLang = currentLocales.get(0).getLanguage();
        }

        languages.add(new LanguageSelection(getString(R.string.lang_english), "en", currentLang.equals("en")));
        languages.add(new LanguageSelection(getString(R.string.lang_spanish), "es", currentLang.equals("es")));
        languages.add(new LanguageSelection(getString(R.string.lang_arabic), "ar", currentLang.equals("ar")));
        languages.add(new LanguageSelection(getString(R.string.lang_french), "fr", currentLang.equals("fr")));
        languages.add(new LanguageSelection(getString(R.string.lang_portuguese), "pt", currentLang.equals("pt")));
        languages.add(new LanguageSelection(getString(R.string.lang_russian), "ru", currentLang.equals("ru")));
        languages.add(new LanguageSelection(getString(R.string.lang_japanese), "ja", currentLang.equals("ja")));
        languages.add(new LanguageSelection(getString(R.string.lang_chinese), "zh", currentLang.equals("zh")));
        languages.add(new LanguageSelection(getString(R.string.lang_german), "de", currentLang.equals("de")));
        languages.add(new LanguageSelection(getString(R.string.lang_italian), "it", currentLang.equals("it")));
        languages.add(new LanguageSelection(getString(R.string.lang_korean), "ko", currentLang.equals("ko")));
        languages.add(new LanguageSelection(getString(R.string.lang_swedish), "sv", currentLang.equals("sv")));
        languages.add(new LanguageSelection(getString(R.string.lang_dutch), "nl", currentLang.equals("nl")));
        languages.add(new LanguageSelection(getString(R.string.lang_afrikaans), "af", currentLang.equals("af")));
        languages.add(new LanguageSelection(getString(R.string.lang_turkish), "tr", currentLang.equals("tr")));
        languages.add(new LanguageSelection(getString(R.string.lang_catalan), "ca", currentLang.equals("ca")));
        languages.add(new LanguageSelection(getString(R.string.lang_filipino), "fil", currentLang.equals("fil")));
        languages.add(new LanguageSelection(getString(R.string.lang_bahasa), "id", currentLang.equals("id")));
        languages.add(new LanguageSelection(getString(R.string.lang_bengali), "bn", currentLang.equals("bn")));
        languages.add(new LanguageSelection(getString(R.string.lang_hindi), "hi", currentLang.equals("hi")));
        languages.add(new LanguageSelection(getString(R.string.lang_sinhala), "si", currentLang.equals("si")));
        languages.add(new LanguageSelection(getString(R.string.lang_gujarati), "gu", currentLang.equals("gu")));

        RecyclerView rvLanguages = findViewById(R.id.rvLanguages);
        adapter = new LanguageSelectionAdapter(languages);
        rvLanguages.setLayoutManager(new LinearLayoutManager(this));
        rvLanguages.setAdapter(adapter);
    }
}