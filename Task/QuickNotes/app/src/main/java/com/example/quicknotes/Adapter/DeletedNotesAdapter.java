package com.example.quicknotes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Database.Note;
import com.example.quicknotes.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DeletedNotesAdapter extends RecyclerView.Adapter<DeletedNotesAdapter.DeletedNoteViewHolder> {

    private List<Note> noteList = new ArrayList<>();
    private final Set<Integer> selectedNoteIds = new HashSet<>();
    private boolean isSelectionMode = false;
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
        void onSelectionChanged(int count);
    }

    public DeletedNotesAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeletedNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_design, parent, false);
        return new DeletedNoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeletedNoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        Context context = holder.itemView.getContext();

        holder.txtTitle.setText(note.getTitle());
        holder.txtDescription.setVisibility(View.GONE);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        holder.txtTime.setText(sdf.format(new Date(note.getModifiedTime())));

        int[] colors = getNoteColors(context, note);
        holder.cardNote.setCardBackgroundColor(colors[0]);
        holder.viewColor.setBackgroundColor(colors[1]);

        // Selection Highlight
        if (selectedNoteIds.contains(note.getId())) {
            holder.cardNote.setStrokeColor(ContextCompat.getColor(context, R.color.primary_blue));
            holder.cardNote.setStrokeWidth(4);
        } else {
            holder.cardNote.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(note.getId());
            } else {
                listener.onNoteClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(note.getId());
                listener.onNoteLongClick(note);
            }
            return true;
        });
    }

    private void toggleSelection(int id) {
        if (selectedNoteIds.contains(id)) {
            selectedNoteIds.remove(id);
        } else {
            selectedNoteIds.add(id);
        }
        
        if (selectedNoteIds.isEmpty()) {
            isSelectionMode = false;
        }
        
        notifyDataSetChanged();
        listener.onSelectionChanged(selectedNoteIds.size());
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

    public void clearSelection() {
        selectedNoteIds.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
        listener.onSelectionChanged(0);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedNoteIds.clear();
        }
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedNoteIds() {
        return new ArrayList<>(selectedNoteIds);
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setNotes(List<Note> notes) {
        if (notes == null) {
            this.noteList = new ArrayList<>();
        } else {
            this.noteList = new ArrayList<>(notes);
        }
        notifyDataSetChanged();
    }

    private int[] getNoteColors(Context context, Note note) {
        int bgColor;
        int stripColor;
        String category = note.getCategory() != null ? note.getCategory() : "Untitled";

        switch (category) {
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
            case "Untitled":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_red_bg);
                stripColor = ContextCompat.getColor(context, R.color.badge_untitled_red_text);
                break;
            case "Untiled_orange":
                bgColor = ContextCompat.getColor(context,R.color.badge_untitled_orange_bg);
                stripColor = ContextCompat.getColor(context,R.color.badge_untitled_orange_text);
                break;

            case "Untiled_pink":
                bgColor = ContextCompat.getColor(context,R.color.badge_untitled_pink_bg);
                stripColor = ContextCompat.getColor(context,R.color.badge_untitled_pink_text);
                break;
            case "Untiled_purple":
                bgColor = ContextCompat.getColor(context,R.color.badge_untitled_purple_bg);
                stripColor = ContextCompat.getColor(context,R.color.badge_untitled_purple_text);
                break;
            case "Untiled_dark_gray":
                bgColor = ContextCompat.getColor(context,R.color.badge_untitled_dark_gray_bg);
                stripColor = ContextCompat.getColor(context,R.color.badge_untitled_dark_gray_text);
                break;

            case "Untiled_gray":
                bgColor = ContextCompat.getColor(context,R.color.badge_untitled_gray_bg);
                stripColor = ContextCompat.getColor(context,R.color.badge_untitled_gray_text);
                break;

            default:
                bgColor = ContextCompat.getColor(context, R.color.light_sky_blue);
                stripColor = ContextCompat.getColor(context, R.color.primary_blue);
                break;
        }
        return new int[]{bgColor, stripColor};
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class DeletedNoteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNote;
        View viewColor;
        TextView txtTitle, txtDescription, txtTime;

        public DeletedNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNote = itemView.findViewById(R.id.cardNote);
            viewColor = itemView.findViewById(R.id.viewColor);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
