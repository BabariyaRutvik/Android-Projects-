package com.example.quickbazaar;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class QuickBazaarApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Advanced Firestore Setup: Enable Offline Persistence & Unlimited Cache
        // Set settings globally before any Firestore instance is used
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build())
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (IllegalStateException e) {
            // Firestore was already initialized elsewhere, settings couldn't be applied.
            // This prevents the app from crashing.
            e.printStackTrace();
        }
    }
}
