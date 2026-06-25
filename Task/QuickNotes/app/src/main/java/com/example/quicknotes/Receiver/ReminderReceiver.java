package com.example.quicknotes.Receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.quicknotes.Activity.ReminderActivity;
import com.example.quicknotes.R;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("note_id", -1);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        if (noteId == -1) return;

        createNotificationChannel(context);

        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("SNOOZE");
        snoozeIntent.putExtra("note_id", noteId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, noteId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
                .setColor(context.getResources().getColor(R.color.sky_blue, null))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(0, "Ignore", ignorePendingIntent)
                .addAction(0, "Snooze", snoozePendingIntent)
                .addAction(0, "Check", checkPendingIntent);

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
