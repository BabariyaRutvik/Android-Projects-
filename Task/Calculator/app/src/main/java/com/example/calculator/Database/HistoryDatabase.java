package com.example.calculator.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {HistoryItem.class}, version = 1)
public abstract class HistoryDatabase extends RoomDatabase {
    private static HistoryDatabase instance;

    public abstract HistoryDao historyDao();

    public static synchronized HistoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            HistoryDatabase.class, "calculator_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
