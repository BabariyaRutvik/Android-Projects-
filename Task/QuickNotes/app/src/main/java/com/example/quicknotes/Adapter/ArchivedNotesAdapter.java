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
import androidx.recyclerview.widget.DiffUtil;
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
import java.util.Objects;
import java.util.Set;

public class ArchivedNotesAdapter extends RecyclerView.Adapter<ArchivedNotesAdapter.ArchivedNotesViewHolder> {
    private List<Note>noteList = new ArrayList<>();
    private Set<Integer>selectedNoteIds = new HashSet<>();

    private boolean isSelectionMode = false;
    private ViewSelectionBottomSheet.ViewType viewType = ViewSelectionBottomSheet.ViewType.DETAILS;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
        void onSelectionChanged(int count);
    }
    // constructor
    public ArchivedNotesAdapter(OnNoteClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArchivedNotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_design,parent,false);
        return new ArchivedNotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchivedNotesViewHolder holder, int position) {
        Note note = noteList.get(position);
        Context context = holder.itemView.getContext();

        holder.textTitle.setText(note.getTitle());

        if (viewType == ViewSelectionBottomSheet.ViewType.LIST){
            holder.textDescription.setVisibility(View.GONE);
            holder.textTime.setVisibility(View.GONE);

        }
        else {
            if (TextUtils.isEmpty(note.getDescription())){
                holder.textDescription.setVisibility(View.GONE);
            }
            else {
                holder.textDescription.setVisibility(View.VISIBLE);
                if ("CHECKLIST".equals(note.getNoteType())){
                    holder.textDescription.setText(formatCheckList(note.getDescription()));

                }
                else {
                    holder.textDescription.setText(note.getDescription());

                }
            }
            holder.textTime.setVisibility(View.VISIBLE);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            holder.textTime.setText(simpleDateFormat.format(new Date(note.getModifiedTime())));
        }
        int[] color = getNotesColor(context,note);
        holder.cardNote.setCardBackgroundColor(color[0]);
        holder.viewColor.setBackgroundColor(color[1]);

        // for selection note border width
        if (selectedNoteIds.contains(note.getId())){
            holder.cardNote.setStrokeWidth(4);
            holder.cardNote.setStrokeColor(ContextCompat.getColor(context,R.color.primary_blue));
        }
        else {
            holder.cardNote.setStrokeWidth(0);
        }
        // for not pin
        if (note.isPinned()){
            holder.imgPin.setVisibility(View.VISIBLE);
            holder.imgPin.setColorFilter(color[1]);
        }
        else {
            holder.imgPin.setVisibility(View.GONE);
        }
        // click event
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode){
                    toggleSelection(note);
                }
                else {
                    listener.onNoteClick(note);
                }
            }
        });
        // on long click for unarchived , lock and delete
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isSelectionMode){
                    isSelectionMode = true;
                    toggleSelection(note);
                    listener.onNoteLongClick(note);
                }
                return true;
            }
        });
    }
    private void toggleSelection(Note note){
        if (selectedNoteIds.contains(note.getId())){
            selectedNoteIds.remove(note.getId());
        }
        else {
            selectedNoteIds.add(note.getId());
        }
        if (selectedNoteIds.isEmpty()){
            isSelectionMode=false;

        }
        notifyDataSetChanged();
        listener.onSelectionChanged(selectedNoteIds.size());
    }
    public List<Integer>getSelectedNoteIds(){
        return new ArrayList<>(selectedNoteIds);
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
    private int[] getNotesColor(Context context ,Note note){
        int bgColor;
        int StripeColor;
        String category = note.getCategory() != null ? note.getCategory() : "Untitled";

        switch (category) {
            case "All":
                bgColor = ContextCompat.getColor(context, R.color.light_sky_blue);
                StripeColor = ContextCompat.getColor(context, R.color.primary_blue);
                break;

            case "Personal":
                bgColor = ContextCompat.getColor(context, R.color.badge_personal_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_personal_text);
                break;

            case "Work":
                bgColor = ContextCompat.getColor(context, R.color.badge_work_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_work_text);
                break;

            case "Others":
                bgColor = ContextCompat.getColor(context, R.color.badge_others_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_others_text);
                break;

            case "Untitled_Red":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_red_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_untitled_red_text);
                break;

            case "Untitled_Orange":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_orange_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_untitled_orange_text);
                break;


            case "Untitled_Pink":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_pink_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_untitled_pink_text);
                break;

            case "Untitled_Purple":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_purple_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_untitled_purple_text);
                break;

            case "Untitled_DarkGray":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_text);
                break;

            case "Untitled_Gray":
                bgColor = ContextCompat.getColor(context, R.color.badge_untitled_gray_bg);
                StripeColor = ContextCompat.getColor(context, R.color.badge_untitled_gray_text);
                break;

            default:
                try {
                    bgColor = Color.parseColor(note.getNoteColor());
                    StripeColor = bgColor;

                }catch (Exception e){
                    bgColor = ContextCompat.getColor(context, R.color.surface_bg);
                    StripeColor = ContextCompat.getColor(context, R.color.gray_icon);
                }
                break;
        }
        return  new int[] {bgColor,StripeColor};


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
        this.viewType = viewType;
        notifyDataSetChanged();
    }
    private String formatCheckList(String data){
        if (data == null || data.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = data.split("\n");

        for (int i = 0; i< Math.min(lines.length, 3) ; i++){
            String[] parts = lines[i].split("\\|",2);

            if (parts.length == 2){
                stringBuilder.append(parts[0].equals("1") ? "☑ " : "☐ ").append(parts[1]).append("\n");

            }
            if (lines.length > 3){
               stringBuilder.append("...");
            }

        }
        return stringBuilder.toString().trim();

    }


    static class ArchivedNotesViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNote;
        View viewColor;
        TextView textTitle, textDescription, textTime;

        ImageView imgPin;


        public ArchivedNotesViewHolder(@NonNull View itemView) {
            super(itemView);

            cardNote = itemView.findViewById(R.id.cardNote);
            viewColor = itemView.findViewById(R.id.viewColor);
            textTitle = itemView.findViewById(R.id.txtTitle);
            textDescription = itemView.findViewById(R.id.txtDescription);
            textTime = itemView.findViewById(R.id.txtTime);
            imgPin = itemView.findViewById(R.id.imgPin);
        }
    }

}


