package com.example.bharatbuzz.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bharatbuzz.NewsModel.Article;

import java.util.List;

@Dao
public interface ArticleDao {
    @Query("SELECT * FROM articles")
    List<Article> getAllBookmarks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmark(Article article);

    @Delete
    void deleteBookmark(Article article);

    @Query("SELECT EXISTS(SELECT * FROM articles WHERE url = :url)")
    boolean isBookmarked(String url);

    @Query("SELECT * FROM articles WHERE title LIKE :query")
    List<Article> searchBookmarks(String query);
}
