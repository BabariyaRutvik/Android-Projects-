package com.example.quicknotes.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();
    private List<Note> fullNoteList = new ArrayList<>();
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

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_design, parent, false);
            return new NoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderViewHolder) holder).txtHeaderTitle.setText((String) items.get(position));
        } else {
            Note note = (Note) items.get(position);
            NoteViewHolder noteHolder = (NoteViewHolder) holder;
            Context context = holder.itemView.getContext();

            // Title
            String title = note.getTitle();
            if (TextUtils.isEmpty(title)) {
                noteHolder.txtTitle.setText("Untitled");
            } else {
                noteHolder.txtTitle.setText(title.replace("\n", " ").replace("\r", " "));
            }
            if (note.isCompleted()) {
                noteHolder.txtTitle.setPaintFlags(noteHolder.txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                noteHolder.txtTitle.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
            } else {
                noteHolder.txtTitle.setPaintFlags(noteHolder.txtTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                noteHolder.txtTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            }

            // View Type adjustment
            if (currentViewType == ViewSelectionBottomSheet.ViewType.LIST) {
                noteHolder.txtDescription.setVisibility(View.GONE);
                noteHolder.txtTime.setVisibility(View.GONE);
            } else {
                // Description
                if (note.isLocked()) {
                    noteHolder.txtDescription.setVisibility(View.VISIBLE);
                    noteHolder.txtDescription.setText("****************");
                } else if (TextUtils.isEmpty(note.getDescription())) {
                    noteHolder.txtDescription.setVisibility(View.GONE);
                } else {
                    noteHolder.txtDescription.setVisibility(View.VISIBLE);
                    String desc;
                    if ("CHECKLIST".equals(note.getNoteType())) {
                        desc = formatChecklist(note.getDescription());
                    } else {
                        desc = note.getDescription();
                    }
                    if (desc != null) {
                        desc = desc.replace("\n", " ").replace("\r", " ");
                    }
                    noteHolder.txtDescription.setText(desc);
                }

                // Date
                noteHolder.txtTime.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, dd MMM", Locale.getDefault());
                noteHolder.txtTime.setText(sdf.format(new Date(note.getModifiedTime())));
            }

            // Image Thumbnail
            if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
                noteHolder.cardThumb.setVisibility(View.VISIBLE);
                noteHolder.imgThumb.setImageURI(android.net.Uri.fromFile(new java.io.File(note.getImagePath())));
            } else {
                noteHolder.cardThumb.setVisibility(View.GONE);
            }

            // Colors mapping
            int[] colors = getNoteColors(context, note);
            noteHolder.cardNote.setCardBackgroundColor(colors[0]);
            noteHolder.viewColor.setBackgroundColor(colors[1]);

            // Selection Border
            if (selectedNoteIds.contains(note.getId())) {
                noteHolder.cardNote.setStrokeColor(ContextCompat.getColor(context, R.color.primary_blue));
                noteHolder.cardNote.setStrokeWidth(4);
            } else {
                noteHolder.cardNote.setStrokeWidth(0);
            }

            // Pin icon
            if (note.isPinned()) {
                noteHolder.imgPin.setVisibility(View.VISIBLE);
                noteHolder.imgPin.setColorFilter(colors[1]);
            } else {
                noteHolder.imgPin.setVisibility(View.GONE);
            }

            // Lock icon
            if (note.isLocked()) {
                noteHolder.imgLock.setVisibility(View.VISIBLE);
            } else {
                noteHolder.imgLock.setVisibility(View.GONE);
            }

            // Click
            noteHolder.itemView.setOnClickListener(v -> {
                if (isSelectionMode) {
                    toggleSelection(note);
                } else {
                    listener.onNoteClick(note);
                }
            });

            noteHolder.itemView.setOnLongClickListener(v -> {
                if (!isSelectionMode) {
                    isSelectionMode = true;
                    toggleSelection(note);
                    listener.onNoteLongClick(note);
                }
                return true;
            });
        }
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
        for (Note note : fullNoteList) {
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

        if (note.isCompleted()) {
            bgColor = ContextCompat.getColor(context, R.color.light_red); // Example for completed
            stripColor = ContextCompat.getColor(context, R.color.badge_untitled_red_text);
            return new int[]{bgColor, stripColor};
        }

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
                try {
                    bgColor = Color.parseColor(note.getNoteColor());
                    stripColor = bgColor;
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
        return items.size();
    }

    public void setNotes(List<Note> notes) {
        this.fullNoteList = notes;
        updateItems(notes);
    }

    private void updateItems(List<Note> notes) {
        items.clear();
        List<Note> active = new ArrayList<>();
        List<Note> completed = new ArrayList<>();
        
        for (Note note : notes) {
            if (note.isCompleted()) {
                completed.add(note);
            } else {
                active.add(note);
            }
        }
        
        if (!active.isEmpty()) {
            items.add("Notes");
            items.addAll(active);
        }
        
        if (!completed.isEmpty()) {
            items.add("Done");
            items.addAll(completed);
        }
        
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

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeaderTitle;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHeaderTitle = itemView.findViewById(R.id.txtHeaderTitle);
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNote, cardThumb;
        View viewColor;
        TextView txtTitle, txtDescription, txtTime;
        ImageView imgPin, imgLock, imgThumb;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNote = itemView.findViewById(R.id.cardNote);
            viewColor = itemView.findViewById(R.id.viewColor);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgPin = itemView.findViewById(R.id.imgPin);
            imgLock = itemView.findViewById(R.id.imgLock);
            cardThumb = itemView.findViewById(R.id.cardThumb);
            imgThumb = itemView.findViewById(R.id.imgThumb);
        }
    }
}

