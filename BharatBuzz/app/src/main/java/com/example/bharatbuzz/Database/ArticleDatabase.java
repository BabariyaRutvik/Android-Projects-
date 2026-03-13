package com.example.bharatbuzz.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.bharatbuzz.NewsModel.Article;

@Database(entities = {Article.class}, version = 1, exportSchema = false)
public abstract class ArticleDatabase extends RoomDatabase {
    private static ArticleDatabase instance;

    public abstract ArticleDao articleDao();

    public static synchronized ArticleDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ArticleDatabase.class, "article_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Simple implementation for this exercise
                    .build();
        }
        return instance;
    }
}
