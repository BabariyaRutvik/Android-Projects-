package com.example.quicknotes.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Database.Note;
import com.example.quicknotes.R;
import com.example.quicknotes.BottomSheet.ViewSelectionBottomSheet;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> noteList = new ArrayList<>();
    private Set<Integer> selectedNoteIds = new HashSet<>();
    private boolean isSelectionMode = false;
    private ViewSelectionBottomSheet.ViewType currentViewType = ViewSelectionBottomSheet.ViewType.DETAILS;
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        default void onNoteLongClick(Note note) {}
        default void onSelectionChanged(int count) {}
    }

    public NotesAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_design, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        Context context = holder.itemView.getContext();

        // Title
        holder.txtTitle.setText(note.getTitle());

        // View Type adjustment
        if (currentViewType == ViewSelectionBottomSheet.ViewType.LIST) {
            holder.txtDescription.setVisibility(View.GONE);
            holder.txtTime.setVisibility(View.GONE); // Or keep it small?
        } else {
            // Description
            if (TextUtils.isEmpty(note.getDescription())) {
                holder.txtDescription.setVisibility(View.GONE);
            } else {
                holder.txtDescription.setVisibility(View.VISIBLE);
                if ("CHECKLIST".equals(note.getNoteType())) {
                    holder.txtDescription.setText(formatChecklist(note.getDescription()));
                } else {
                    holder.txtDescription.setText(note.getDescription());
                }
            }

            // Date
            holder.txtTime.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            holder.txtTime.setText(sdf.format(new Date(note.getModifiedTime())));
        }

        // Colors mapping: {background, strip}
        int[] colors = getNoteColors(context, note);
        holder.cardNote.setCardBackgroundColor(colors[0]);
        holder.viewColor.setBackgroundColor(colors[1]);

        // Selection Border
        if (selectedNoteIds.contains(note.getId())) {
            holder.cardNote.setStrokeColor(ContextCompat.getColor(context, R.color.primary_blue));
            holder.cardNote.setStrokeWidth(4);
        } else {
            holder.cardNote.setStrokeWidth(0);
        }

        // Pin icon
        if (note.isPinned()) {
            holder.imgPin.setVisibility(View.VISIBLE);
            holder.imgPin.setColorFilter(colors[1]); // Match the pin color with the strip color
        } else {
            holder.imgPin.setVisibility(View.GONE);
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(note);
            } else {
                listener.onNoteClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(note);
                listener.onNoteLongClick(note);
            }
            return true;
        });
    }

    private void toggleSelection(Note note) {
        if (selectedNoteIds.contains(note.getId())) {
            selectedNoteIds.remove(note.getId());
        } else {
            selectedNoteIds.add(note.getId());
        }
        
        if (selectedNoteIds.isEmpty()) {
            isSelectionMode = false;
        }
        
        notifyDataSetChanged();
        listener.onSelectionChanged(selectedNoteIds.size());
    }

    public List<Integer> getSelectedNoteIds() {
        return new ArrayList<>(selectedNoteIds);
    }

    public void clearSelection() {
        isSelectionMode = false;
        selectedNoteIds.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(0);
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedNoteIds.clear();
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedNoteIds.clear();
        for (Note note : noteList) {
            selectedNoteIds.add(note.getId());
        }
        isSelectionMode = true;
        notifyDataSetChanged();
        listener.onSelectionChanged(selectedNoteIds.size());
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    private int[] getNoteColors(Context context, Note note) {
        int bgColor;
        int stripColor;
        String category = note.getCategory() != null ? note.getCategory() : "Untitled";

        switch (category) {
            case "All":
                bgColor = ContextCompat.getColor(context, R.color.light_sky_blue);
                stripColor = ContextCompat.getColor(context, R.color.primary_blue);
                break;
            case "Personal":
                bgColor = ContextCompat.getColor(context, R.color.badge_personal_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_personal_text);
                break;
            case "Work":
                bgColor = ContextCompat.getColor(context, R.color.badge_work_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_work_text);
                break;
            case "Others":
                bgColor = ContextCompat.getColor(context, R.color.badge_others_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_others_text);
                break;
            case "Untitled_Red":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_red_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_red_text);
                break;
            case "Untitled_Orange":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_orange_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_orange_text);
                break;
            case "Untitled_Pink":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_pink_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_pink_text);
                break;
            case "Untitled_Purple":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_purple_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_purple_text);
                break;
            case "Untitled_DarkGray":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_text);
                break;
            case "Untitled_Gray":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_gray_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_gray_text);
                break;
            default:
                // Fallback to saved color if exists, else white
                try {
                    bgColor = Color.parseColor(note.getNoteColor());
                    stripColor = bgColor; // If we don't have a category match, we use the same color
                } catch (Exception e) {
                    bgColor = ContextCompat.getColor(context, R.color.surface_bg);
                    stripColor = ContextCompat.getColor(context, R.color.gray_icon);
                }
                break;
        }
        return new int[]{bgColor, stripColor};
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void setNotes(List<Note> notes) {
        this.noteList = notes;
        notifyDataSetChanged();
    }

    public void setViewType(ViewSelectionBottomSheet.ViewType viewType) {
        this.currentViewType = viewType;
        notifyDataSetChanged();
    }

    private String formatChecklist(String data) {
        if (data == null || data.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] lines = data.split("\n");
        for (int i = 0; i < Math.min(lines.length, 3); i++) {
            String[] parts = lines[i].split("\\|", 2);
            if (parts.length == 2) {
                sb.append(parts[0].equals("1") ? "☑ " : "☐ ").append(parts[1]).append("\n");
            }
        }
        if (lines.length > 3) sb.append("...");
        return sb.toString().trim();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNote;
        View viewColor;
        TextView txtTitle, txtDescription, txtTime;
        ImageView imgPin;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNote = itemView.findViewById(R.id.cardNote);
            viewColor = itemView.findViewById(R.id.viewColor);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgPin = itemView.findViewById(R.id.imgPin);
        }
    }
}
