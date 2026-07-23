package com.example.calculator.Activity;

import android.content.SharedPreferences;
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
                // Persist the language choice
                SharedPreferences prefs = getSharedPreferences("language_prefs", MODE_PRIVATE);
                prefs.edit().putString("selected_language", selectedLang).apply();

                LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(selectedLang);
                AppCompatDelegate.setApplicationLocales(appLocales);
                finish();
            }
        }
    }

    private void setupRecyclerView() {
        List<LanguageSelection> languages = new ArrayList<>();
        
        // Read persisted language choice
        SharedPreferences prefs = getSharedPreferences("language_prefs", MODE_PRIVATE);
        String currentLang = prefs.getString("selected_language", "en");
        
        // Fallback to current app locale if no preference exists
        if (!prefs.contains("selected_language")) {
            LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
            if (!currentLocales.isEmpty()) {
                currentLang = currentLocales.get(0).getLanguage();
            }
        }

        languages.add(new LanguageSelection("English (US)", "en", currentLang.equals("en")));
        languages.add(new LanguageSelection("Spanish (Español)", "es", currentLang.equals("es")));
        languages.add(new LanguageSelection("Arabic (العربية)", "ar", currentLang.equals("ar")));
        languages.add(new LanguageSelection("French (Français)", "fr", currentLang.equals("fr")));
        languages.add(new LanguageSelection("Portuguese (Portugal)", "pt", currentLang.equals("pt")));
        languages.add(new LanguageSelection("Russian (Русский)", "ru", currentLang.equals("ru")));
        languages.add(new LanguageSelection("Japanese (日本語)", "ja", currentLang.equals("ja")));
        languages.add(new LanguageSelection("Chinese (中文)", "zh", currentLang.equals("zh")));
        languages.add(new LanguageSelection("German (Deutsch)", "de", currentLang.equals("de")));
        languages.add(new LanguageSelection("Italian (Italiano)", "it", currentLang.equals("it")));
        languages.add(new LanguageSelection("Korean (한국어)", "ko", currentLang.equals("ko")));
        languages.add(new LanguageSelection("Swedish (Svenska)", "sv", currentLang.equals("sv")));
        languages.add(new LanguageSelection("Dutch (Nederlands)", "nl", currentLang.equals("nl")));
        languages.add(new LanguageSelection("Afrikaans", "af", currentLang.equals("af")));
        languages.add(new LanguageSelection("Turkish (Türkçe)", "tr", currentLang.equals("tr")));
        languages.add(new LanguageSelection("Catalan (Català)", "ca", currentLang.equals("ca")));
        languages.add(new LanguageSelection("Filipino", "fil", currentLang.equals("fil")));
        languages.add(new LanguageSelection("Indonesian (Bahasa)", "id", currentLang.equals("id")));
        languages.add(new LanguageSelection("Bengali (বাংলা)", "bn", currentLang.equals("bn")));
        languages.add(new LanguageSelection("Hindi (हिन्दी)", "hi", currentLang.equals("hi")));
        languages.add(new LanguageSelection("Sinhala (සිංහල)", "si", currentLang.equals("si")));
        languages.add(new LanguageSelection("Gujarati", "gu", currentLang.equals("gu")));

        RecyclerView rvLanguages = findViewById(R.id.rvLanguages);
        adapter = new LanguageSelectionAdapter(languages);
        rvLanguages.setLayoutManager(new LinearLayoutManager(this));
        rvLanguages.setAdapter(adapter);
    }
}