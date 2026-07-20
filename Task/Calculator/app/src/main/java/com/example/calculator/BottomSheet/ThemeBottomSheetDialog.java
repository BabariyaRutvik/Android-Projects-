package com.example.calculator.BottomSheet;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.calculator.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemeBottomSheetDialog extends BottomSheetDialogFragment {

    private ConstraintLayout layoutLight, layoutDark, layoutSystem;
    private ImageView imgLight, imgDark, imgSystem;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    public interface OnThemeChangeListener {
        void onThemeChanged(int nightMode);
    }

    private OnThemeChangeListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnThemeChangeListener) {
            listener = (OnThemeChangeListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_theme_selection, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme()) {
            @Override
            protected void onStart() {
                super.onStart();
                FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                }
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        layoutLight = view.findViewById(R.id.layout_light_theme);
        layoutDark = view.findViewById(R.id.layout_dark_theme);
        layoutSystem = view.findViewById(R.id.layout_system_theme);

        imgLight = view.findViewById(R.id.img_light_check);
        imgDark = view.findViewById(R.id.img_dark_check);
        imgSystem = view.findViewById(R.id.img_system_check);

        int currentTheme = sharedPreferences.getInt(KEY_THEME, THEME_SYSTEM);
        updateUISelection(currentTheme);

        layoutLight.setOnClickListener(v -> applyThemeMode(THEME_LIGHT, AppCompatDelegate.MODE_NIGHT_NO));
        layoutDark.setOnClickListener(v -> applyThemeMode(THEME_DARK, AppCompatDelegate.MODE_NIGHT_YES));
        layoutSystem.setOnClickListener(v -> applyThemeMode(THEME_SYSTEM, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
    }

    private void applyThemeMode(int themeConstant, int nightMode) {
        sharedPreferences.edit().putInt(KEY_THEME, themeConstant).apply();
        if (listener != null) listener.onThemeChanged(nightMode);
        dismiss();
    }

    private void updateUISelection(int currentTheme) {
        imgLight.setImageResource(currentTheme == THEME_LIGHT ? R.drawable.select_all_history : R.drawable.select_historry);
        imgDark.setImageResource(currentTheme == THEME_DARK ? R.drawable.select_all_history : R.drawable.select_historry);
        imgSystem.setImageResource(currentTheme == THEME_SYSTEM ? R.drawable.select_all_history : R.drawable.select_historry);
    }
}