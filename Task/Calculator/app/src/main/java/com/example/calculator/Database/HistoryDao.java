package com.example.calculator.Database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao
{
    // for insertion
    @Insert
    void insert(HistoryItem item);

    // get all history
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    LiveData<List<HistoryItem>> getAllHistory();

    // delete single item
    @Delete
    void delete(HistoryItem item);

    // delete all
    @Query("DELETE FROM history")
    void deleteAll();

    // delete selected
    @Query("DELETE FROM history WHERE id IN (:ids)")
    void deleteSelected(List<Integer> ids);

    // prune history to limit
    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY timestamp DESC LIMIT :limit)")
    void prune(int limit);
}
