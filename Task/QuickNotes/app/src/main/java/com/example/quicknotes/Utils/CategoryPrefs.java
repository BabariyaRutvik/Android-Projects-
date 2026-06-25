package com.example.quicknotes.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class CategoryPrefs {
    private static final String PREF_NAME = "category_prefs";
    private static final String KEY_CATEGORY_PREFIX = "category_";
    
    private final SharedPreferences prefs;

    public CategoryPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveCategoryName(String key, String name) {
        prefs.edit().putString(KEY_CATEGORY_PREFIX + key, name).apply();
    }

    public String getCategoryName(String key, String defaultName) {
        return prefs.getString(KEY_CATEGORY_PREFIX + key, defaultName);
    }
    
    public void saveCategories(List<String> keys, List<String> names) {
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < keys.size(); i++) {
            editor.putString(KEY_CATEGORY_PREFIX + keys.get(i), names.get(i));
        }
        editor.apply();
    }
}
