package com.example.quicknotes.Adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final CategoryPrefs prefs;

    public CategorySpinnerAdapter(@NonNull Context context, @NonNull List<String> categories) {
        super(context, 0, categories);
        inflater = LayoutInflater.from(context);
        prefs = new CategoryPrefs(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.layout_spinner_selected);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.item_spinner_category);
    }

    private View createView(int position, View convertView, ViewGroup parent, int layoutRes) {
        if (convertView == null) {
            convertView = inflater.inflate(layoutRes, parent, false);
        }

        String categoryKey = getItem(position);
        View viewDot = convertView.findViewById(R.id.viewDot);
        TextView textCategory = convertView.findViewById(R.id.textCategory);

        if (categoryKey != null) {
            String defaultName = categoryKey.contains("Untitled_") ? "Untitled" : categoryKey;
            textCategory.setText(prefs.getCategoryName(categoryKey, defaultName));
            int dotColor = getCategoryDotColor(categoryKey);
            
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(dotColor);
            viewDot.setBackground(drawable);
            
            if (layoutRes == R.layout.layout_spinner_selected) {
                textCategory.setTextColor(dotColor);
            }
        }

        return convertView;
    }

    private int getCategoryDotColor(String category) {
        int colorRes;
        switch (category) {
            case "All": colorRes = R.color.primary_blue; break;
            case "Personal": colorRes = R.color.badge_personal_text; break;
            case "Work": colorRes = R.color.badge_work_text; break;
            case "Others": colorRes = R.color.badge_others_text; break;
            case "Untitled_Red": colorRes = R.color.badge_untitled_red_text; break;
            case "Untitled_Orange": colorRes = R.color.badge_untitled_orange_text; break;
            case "Untitled_Pink": colorRes = R.color.badge_untitled_pink_text; break;
            case "Untitled_Purple": colorRes = R.color.badge_untitled_purple_text; break;
            case "Untitled_DarkGray": colorRes = R.color.badge_untitled_dark_gray_text; break;
            case "Untitled_Gray": colorRes = R.color.badge_untitled_gray_text; break;
            default: colorRes = R.color.badge_untitled_purple_text; break;
        }
        return ContextCompat.getColor(getContext(), colorRes);
    }
}