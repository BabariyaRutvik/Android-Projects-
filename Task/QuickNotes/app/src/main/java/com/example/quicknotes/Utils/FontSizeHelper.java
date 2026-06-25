package com.example.quicknotes.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class FontSizeHelper {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_FONT_SCALE = "font_scale";

    public static Context onAttach(Context context) {
        float scale = getFontScale(context);
        return applyFontScale(context, scale);
    }

    public static float getFontScale(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getFloat(KEY_FONT_SCALE, 1.0f); // Default scale is 1.0
    }

    public static void setFontScale(Context context, float scale) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putFloat(KEY_FONT_SCALE, scale).apply();
    }

    public static Context applyFontScale(Context context, float scale) {
        Resources res = context.getResources();
        Configuration configuration = new Configuration(res.getConfiguration());
        configuration.fontScale = scale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(configuration);
        } else {
            res.updateConfiguration(configuration, res.getDisplayMetrics());
            return context;
        }
    }
}
