package com.example.quicknotes.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.quicknotes.Utils.FontSizeHelper;

import java.util.Locale;

public class LanguageHelper {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_LANGUAGE = "app_language";

    public static Context onAttach(Context context) {
        String language = getLanguage(context);
        Context langContext = updateResources(context, language);
        return FontSizeHelper.onAttach(langContext);
    }

    public static String getLanguage(Context context) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty()) {
            return locales.get(0).getLanguage();
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, Locale.getDefault().getLanguage());
    }

    public static void setLocale(Context context, String language) {
        persist(context, language);
        
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(language);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private static void persist(Context context, String language) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        
        config.setLayoutDirection(locale);

        return context.createConfigurationContext(config);
    }
}
