package com.example.quicknotes.Adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Database.Note;
import com.example.quicknotes.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    private List<Object> items = new ArrayList<>();
    private final OnTaskClickListener listener;
    private int selectedNoteId = -1;

    public interface OnTaskClickListener {
        void onTaskClick(Note note);
        void onTaskLongClick(Note note, View view);
        void onTaskStatusChanged(Note note, boolean isCompleted);
    }

    public void setSelectedNoteId(int id) {
        this.selectedNoteId = id;
        notifyDataSetChanged();
    }

    public int getSelectedNoteId() {
        return selectedNoteId;
    }

    public CalendarTaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String title = (String) items.get(position);
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.txtHeader.setText(title);
            
            if ("Done".equalsIgnoreCase(title)) {
                headerHolder.imgSort.setVisibility(View.GONE);
            } else {
                headerHolder.imgSort.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof TaskViewHolder) {
            Note note = (Note) items.get(position);
            TaskViewHolder taskHolder = (TaskViewHolder) holder;

            taskHolder.txtTitle.setText(note.getTitle());
            
            if (note.isLocked()) {
                taskHolder.imgLock.setVisibility(View.VISIBLE);
                if (note.getDescription() != null && !note.getDescription().isEmpty()) {
                    taskHolder.txtDesc.setVisibility(View.VISIBLE);
                    taskHolder.txtDesc.setText("****************");
                } else {
                    taskHolder.txtDesc.setVisibility(View.GONE);
                }
            } else {
                taskHolder.imgLock.setVisibility(View.GONE);
                if (note.getDescription() != null && !note.getDescription().isEmpty()) {
                    taskHolder.txtDesc.setVisibility(View.VISIBLE);
                    if ("CHECKLIST".equals(note.getNoteType())) {
                        taskHolder.txtDesc.setText(formatChecklistSummary(note.getDescription()));
                    } else {
                        taskHolder.txtDesc.setText(note.getDescription());
                    }
                } else {
                    taskHolder.txtDesc.setVisibility(View.GONE);
                }
            }

            // Category Indicator
            int stripColor = getCategoryStripColor(taskHolder.itemView.getContext(), note.getCategory());
            taskHolder.viewCategoryIndicator.setBackgroundColor(stripColor);
            if (note.getReminderTime() > 0) {
                taskHolder.viewCategoryIndicator.setVisibility(View.VISIBLE);
            } else {
                taskHolder.viewCategoryIndicator.setVisibility(View.INVISIBLE);
            }

            // Apply visual style for completed status
            if (note.isCompleted()) {
                taskHolder.checkbox.setChecked(true);
                taskHolder.txtTitle.setPaintFlags(taskHolder.txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskHolder.txtTitle.setAlpha(0.5f);
                taskHolder.txtDesc.setAlpha(0.5f);
            } else {
                taskHolder.checkbox.setChecked(false);
                taskHolder.txtTitle.setPaintFlags(taskHolder.txtTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                taskHolder.txtTitle.setAlpha(1.0f);
                taskHolder.txtDesc.setAlpha(1.0f);
            }

            // Reminder time and repeat info
            if (note.getReminderTime() > 0) {
                taskHolder.txtTime.setVisibility(View.VISIBLE);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
                taskHolder.txtTime.setText(sdf.format(new java.util.Date(note.getReminderTime())));
                
                if (note.getRepeatType() != null && !note.getRepeatType().equals("None")) {
                    taskHolder.imgRepeat.setVisibility(View.VISIBLE);
                } else {
                    taskHolder.imgRepeat.setVisibility(View.GONE);
                }
            } else {
                taskHolder.txtTime.setVisibility(View.GONE);
                taskHolder.imgRepeat.setVisibility(View.GONE);
            }

            taskHolder.itemView.setOnClickListener(v -> listener.onTaskClick(note));
            taskHolder.itemView.setOnLongClickListener(v -> {
                listener.onTaskLongClick(note, v);
                return true;
            });

            // Selection Highlight
            if (note.getId() == selectedNoteId) {
                taskHolder.itemView.setBackgroundColor(getCategoryLightColor(taskHolder.itemView.getContext(), note.getCategory()));
            } else {
                taskHolder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
            
            taskHolder.checkbox.setOnClickListener(v -> {
                boolean isChecked = taskHolder.checkbox.isChecked();
                listener.onTaskStatusChanged(note, isChecked);
            });
        }
    }

    private int getCategoryStripColor(android.content.Context context, String category) {
        if (category == null) return androidx.core.content.ContextCompat.getColor(context, R.color.primary_blue);
        switch (category) {
            case "Personal": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_personal_text);
            case "Work": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_work_text);
            case "Others": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_others_text);
            case "Untitled_Red": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_red_text);
            case "Untitled_Orange": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_orange_text);
            case "Untitled_Pink": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_pink_text);
            case "Untitled_Purple": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_purple_text);
            case "Untitled_DarkGray": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_text);
            case "Untitled_Gray": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_gray_text);
            default: return androidx.core.content.ContextCompat.getColor(context, R.color.primary_blue);
        }
    }

    private String formatChecklistSummary(String data) {
        if (data == null) return "";
        String[] lines = data.split("\n");
        if (lines.length > 0) {
            String[] parts = lines[0].split("\\|", 2);
            if (parts.length == 2) return parts[1];
        }
        return "";
    }

    private int getCategoryLightColor(android.content.Context context, String category) {
        if (category == null) return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_purple_bg);
        switch (category) {
            case "Personal": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_personal_bg);
            case "Work": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_work_bg);
            case "Others": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_others_bg);
            case "Untitled_Red": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_red_bg);
            case "Untitled_Orange": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_orange_bg);
            case "Untitled_Pink": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_pink_bg);
            case "Untitled_Purple": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_purple_bg);
            case "Untitled_DarkGray": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_dark_gray_bg);
            case "Untitled_Gray": return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_gray_bg);
            default: return androidx.core.content.ContextCompat.getColor(context, R.color.badge_untitled_purple_bg);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setTasks(List<Note> activeTasks, List<Note> doneTasks, String dateLabel) {
        items.clear();
        if (!activeTasks.isEmpty()) {
            items.add(dateLabel);
            items.addAll(activeTasks);
        }
        if (!doneTasks.isEmpty()) {
            items.add("Done");
            items.addAll(doneTasks);
        }
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView txtTitle, txtDesc, txtTime;
        ImageView imgRepeat, imgLock;
        View viewCategoryIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            txtTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtDesc = itemView.findViewById(R.id.txtTaskDesc);
            txtTime = itemView.findViewById(R.id.txtTaskTime);
            imgRepeat = itemView.findViewById(R.id.imgRepeat);
            imgLock = itemView.findViewById(R.id.imgLock);
            viewCategoryIndicator = itemView.findViewById(R.id.viewCategoryIndicator);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeader;
        ImageView imgSort;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHeader = itemView.findViewById(R.id.txtHeaderTitle);
            imgSort = itemView.findViewById(R.id.imgHeaderSort);
        }
    }
}