package com.example.quicknotes.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Receiver.ReminderReceiver;

import java.util.Calendar;

public class ReminderManager {

    private static final String TAG = "ReminderManager";

    public static void scheduleReminder(Context context, Note note) {
        if (note.getReminderTime() <= System.currentTimeMillis()) {
            Log.d(TAG, "Not scheduling: Reminder time is in the past.");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("note_id", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                note.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, note.getReminderTime(), pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, note.getReminderTime(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, note.getReminderTime(), pendingIntent);
            }
            Log.d(TAG, "Scheduled alarm for note ID: " + note.getId() + " at " + note.getReminderTime());
        }
    }

    public static void cancelReminder(Context context, int noteId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Cancelled alarm for note ID: " + noteId);
        }
    }
}
