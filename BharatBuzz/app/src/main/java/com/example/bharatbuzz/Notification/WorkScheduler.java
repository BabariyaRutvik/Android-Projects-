package com.example.bharatbuzz.Notification;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkScheduler {

    public static void scheduleDailyNewsNotifications(Context context) {
        scheduleWork(context, "MORNING_UPDATES", 8, 0, "Good Morning!", "Start your day with the latest BharatBuzz headlines.");
        scheduleWork(context, "AFTERNOON_UPDATES", 12, 0, "Mid-day News Flash", "Stay updated with what's happening around you right now.");
        scheduleWork(context, "EVENING_UPDATES", 16, 0, "Evening Wrap-up", "Catch up on all the stories you missed today.");
    }

    public static void cancelAllNotifications(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelUniqueWork("MORNING_UPDATES");
        workManager.cancelUniqueWork("AFTERNOON_UPDATES");
        workManager.cancelUniqueWork("EVENING_UPDATES");
    }

    private static void scheduleWork(Context context, String tag, int hour, int minute, String title, String message) {
        long delay = calculateDelay(hour, minute);

        Data inputData = new Data.Builder()
                .putString("title", title)
                .putString("message", message)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NewsWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .setInputData(inputData)
                .build();

        // Use UPDATE here instead of KEEP just for testing so the new 4:30 time takes effect immediately
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
        );
    }

    private static long calculateDelay(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the calculated time is before or equal to now, schedule for the next day
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis() - now;
    }
}
