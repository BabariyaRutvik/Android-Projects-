package com.example.quicknotes.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 6)
public abstract class NoteDatabase extends RoomDatabase {

    public static NoteDatabase instance;

    // dao
    public abstract NoteDao noteDao();

    // singleton instance
    public static synchronized NoteDatabase getInstance(Context context){

        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class,
                    "notes_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

}
