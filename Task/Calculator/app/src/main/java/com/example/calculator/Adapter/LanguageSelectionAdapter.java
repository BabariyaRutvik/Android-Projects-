package com.example.calculator.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Model.LanguageSelection;
import com.example.calculator.R;

import java.util.List;

public class LanguageSelectionAdapter extends RecyclerView.Adapter<LanguageSelectionAdapter.LanguageHolder> {

    private final List<LanguageSelection> languageSelectionList;
    private int selectedPosition = -1;

    public LanguageSelectionAdapter(List<LanguageSelection> languageSelectionList) {
        this.languageSelectionList = languageSelectionList;
        for (int i = 0; i < languageSelectionList.size(); i++) {
            if (languageSelectionList.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public LanguageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_selection, parent, false);
        return new LanguageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageHolder holder, int position) {
        LanguageSelection selection = languageSelectionList.get(position);
        holder.textLanguage.setText(selection.getName());

        // update selection state
        holder.imageSelection.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition != selectedPosition) {
                int previousPos = selectedPosition;
                selectedPosition = currentPosition;

                if (previousPos != -1) {
                    languageSelectionList.get(previousPos).setSelected(false);
                    notifyItemChanged(previousPos);
                }

                languageSelectionList.get(selectedPosition).setSelected(true);
                notifyItemChanged(selectedPosition);
            }
        });
    }

    public String getSelectedLanguageCode() {
        if (selectedPosition != -1) {
            return languageSelectionList.get(selectedPosition).getLanguageCode();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return languageSelectionList.size();
    }

    public static class LanguageHolder extends RecyclerView.ViewHolder {
        TextView textLanguage;
        ImageView imageSelection;

        public LanguageHolder(@NonNull View itemView) {
            super(itemView);
            textLanguage = itemView.findViewById(R.id.tvLanguageName);
            imageSelection = itemView.findViewById(R.id.img_language_selection);
        }
    }
}