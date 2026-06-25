package com.example.quicknotes.Receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.quicknotes.Activity.ReminderActivity;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteDao;
import com.example.quicknotes.Database.NoteDatabase;
import com.example.quicknotes.Database.NoteRepository;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int noteId = intent.getIntExtra("note_id", -1);

        if (noteId == -1) return;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(noteId);

        if ("CHECK".equals(action)) {
            new Thread(() -> {
                NoteDao noteDao = NoteDatabase.getInstance(context).noteDao();
                Note note = noteDao.getNoteById(noteId);
                if (note != null) {
                    note.setCompleted(true);
                    noteDao.update(note);
                }
            }).start();
            Toast.makeText(context, "Marked as completed", Toast.LENGTH_SHORT).show();
        } else if ("SNOOZE".equals(action)) {
            Intent reminderIntent = new Intent(context, ReminderActivity.class);
            reminderIntent.putExtra("note_id", noteId);
            reminderIntent.putExtra("is_snooze", true);
            reminderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(reminderIntent);
        } else if ("IGNORE".equals(action)) {
            // Just dismissed, notification already cancelled
        }
    }
}
