package com.example.quickbazaar.MessageService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.quickbazaar.QuickActivity.MainActivity;
import com.example.quickbazaar.QuickActivity.ProductFullScreenActivity;
import com.example.quickbazaar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    public static final String ACTION_NOTIFICATION_RECEIVED = "com.example.quickbazaar.NOTIFICATION_RECEIVED";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateTokenInFirestore(token);
    }

    private void updateTokenInFirestore(String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> data = new HashMap<>();
            data.put("fcmToken", token);
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .set(data, com.google.firebase.firestore.SetOptions.merge());
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        
        incrementNotificationCount();

        Intent intent = new Intent(ACTION_NOTIFICATION_RECEIVED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (message.getNotification() != null){
            // Extract productId from data payload if available
            String productId = message.getData().get("productId");
            ShowNotification(message.getNotification().getTitle(), message.getNotification().getBody(), productId);
        }
    }

    private void incrementNotificationCount() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int currentCount = prefs.getInt("notification_count", 0);
        prefs.edit().putInt("notification_count", currentCount + 1).apply();
    }

    private void ShowNotification(String title, String body, String productId){
        String channelId = getString(R.string.default_notification_channel_id);
        
        Intent intent;
        if (productId != null && !productId.isEmpty()) {
            // If productId is present, we go to ProductFullScreenActivity
            // Note: Since ProductFullScreenActivity expects a Product object via Serializable,
            // for a real deep link, you would usually fetch the product from Firestore in that Activity using the ID.
            // For now, we'll pass the ID. You might need to update ProductFullScreenActivity to handle just an ID.
            intent = new Intent(this, ProductFullScreenActivity.class);
            intent.putExtra("productId", productId); 
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId, "Notifications", NotificationManager.IMPORTANCE_HIGH);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        if (manager != null) manager.notify(0, builder.build());
    }
}
