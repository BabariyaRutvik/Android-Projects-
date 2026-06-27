package com.example.quicknotes.Receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Activity.ReminderActivity;
import com.example.quicknotes.R;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("note_id", -1);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String noteType = intent.getStringExtra("note_type");

        if (noteId == -1) return;

        createNotificationChannel(context);

        // Intent to open the correct activity when notification is clicked
        Intent clickIntent;
        if ("CHECKLIST".equals(noteType)) {
            clickIntent = new Intent(context, AddCheckListActivity.class);
        } else {
            clickIntent = new Intent(context, AddNoteActivity.class);
        }
        clickIntent.putExtra("note_id", noteId);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent clickPendingIntent = PendingIntent.getActivity(
                context, 
                noteId, 
                clickIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, ReminderActivity.class);
        snoozeIntent.putExtra("note_id", noteId);
        snoozeIntent.putExtra("is_snooze", true);
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent snoozePendingIntent = PendingIntent.getActivity(context, noteId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent checkIntent = new Intent(context, NotificationActionReceiver.class);
        checkIntent.setAction("CHECK");
        checkIntent.putExtra("note_id", noteId);
        PendingIntent checkPendingIntent = PendingIntent.getBroadcast(context, noteId + 1000, checkIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent ignoreIntent = new Intent(context, NotificationActionReceiver.class);
        ignoreIntent.setAction("IGNORE");
        ignoreIntent.putExtra("note_id", noteId);
        PendingIntent ignorePendingIntent = PendingIntent.getBroadcast(context, noteId + 2000, ignoreIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder_menu)
                .setColor(ContextCompat.getColor(context, R.color.sky_blue))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(clickPendingIntent);

        builder.addAction(R.drawable.ic_close, "Ignore", ignorePendingIntent);
        builder.addAction(R.drawable.ic_calender_outline, "Snooze", snoozePendingIntent);
        builder.addAction(R.drawable.ic_done, "Check", checkPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(noteId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Note Reminders";
            String description = "Channel for Note reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
