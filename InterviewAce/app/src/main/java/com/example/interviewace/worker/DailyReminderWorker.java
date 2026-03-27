package com.example.interviewace.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.interviewace.R;
import com.example.interviewace.model.SessionItem;
import com.example.interviewace.ui.activity.MainActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class DailyReminderWorker extends Worker {

    private static final String CHANNEL_ID = "daily_reminder_channel";
    private static final int NOTIFICATION_ID = 1;

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getUid();

        // 1. Check if user is logged in
        if (userId == null) {
            return Result.success();
        }

        // 2. Get Today's Date String
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());

        try {
            // 3. Query Firestore for today's sessions
            QuerySnapshot querySnapshot = Tasks.await(FirebaseFirestore.getInstance()
                    .collection("sessions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", today)
                    .get());

            List<SessionItem> sessionItems = querySnapshot.toObjects(SessionItem.class);
            int count = sessionItems.size();

            // 4. If daily goal (3) is not met, trigger notification
            if (count < 3) {
                showNotification(count);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Result.retry();
        }

        return Result.success();
    }

    private void showNotification(int currentCount) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Get default notification sound URI
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Required for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Importance HIGH is required for Heads-up notifications (pop-ups)
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminders to complete your daily interview practice");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        // Create Intent to open MainActivity when notification is clicked
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                (int) System.currentTimeMillis(), // Unique ID per trigger
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String message;
        if (currentCount == 0) {
            message = "You haven't started your interview practice today. Keep your streak alive! 🔥";
        } else {
            message = "You've completed " + currentCount + " of 3 sessions. Just " + (3 - currentCount) + " more to reach your goal!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Interview Goal Reminder")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // For pre-Oreo popup
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setSound(defaultSoundUri)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
