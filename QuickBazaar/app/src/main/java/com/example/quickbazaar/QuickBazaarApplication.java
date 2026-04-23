package com.example.quickbazaar;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.quickbazaar.MessageService.NotificationWorker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class QuickBazaarApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Advanced Firestore Setup
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build())
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        // Schedule Daily Notifications
        createNotificationChannel();
        scheduleDailyNotifications();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "daily_reminders",
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Used for daily store reminders");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleDailyNotifications() {
        scheduleNotificationAt(10, 0, "NOTIF_10_00_AM");
        scheduleNotificationAt(14, 0, "NOTIF_02_PM");
        scheduleNotificationAt(18, 0, "NOTIF_06_PM");
        scheduleNotificationAt(22, 0, "NOTIF_10_PM");
    }

    private void scheduleNotificationAt(int hour, int minute, String tag) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = calendar.getTimeInMillis() - now;

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }
}
