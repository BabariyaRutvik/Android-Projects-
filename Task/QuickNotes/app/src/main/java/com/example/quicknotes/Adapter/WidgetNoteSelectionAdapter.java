package com.example.quicknotes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.R;
import java.util.ArrayList;
import java.util.List;

public class WidgetNoteSelectionAdapter extends RecyclerView.Adapter<WidgetNoteSelectionAdapter.ViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteSelectedListener listener;

    public interface OnNoteSelectedListener {
        void onNoteSelected(Note note);
    }

    public WidgetNoteSelectionAdapter(OnNoteSelectedListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);
        
        String title = note.getTitle();
        if (title == null || title.isEmpty()) {
            holder.txtTitle.setText("Untitled");
        } else {
            holder.txtTitle.setText(title.replace("\n", " ").replace("\r", " "));
        }
        
        String desc = note.getDescription();
        if (desc != null) {
            desc = desc.replace("\n", " ").replace("\r", " ");
        }
        holder.txtDescription.setText(desc);

        holder.itemView.setOnClickListener(v -> listener.onNoteSelected(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription;

        ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
        }
    }
}