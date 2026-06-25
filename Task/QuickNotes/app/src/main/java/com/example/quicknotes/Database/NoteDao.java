package com.example.quicknotes.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM notes_table")
    void deleteAll();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND title LIKE '%' || :search || '%' ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> searchNotes(String search);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getNotesByCategory(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND noteType = :type ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getNotesByType(String type);

    @Query("SELECT * FROM notes_table WHERE id = :id")
    Note getNoteById(int id);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, category ASC")
    LiveData<List<Note>> sortByCategory();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, title ASC")
    LiveData<List<Note>> sortNameAZ();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, title DESC")
    LiveData<List<Note>> sortNameZA();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, reminderTime DESC")
    LiveData<List<Note>> sortReminderTime();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> lastModifiedNewToOld();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, modifiedTime ASC")
    LiveData<List<Note>> lastModifiedOldToNew();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, createdTime DESC")
    LiveData<List<Note>> creationNewToOld();

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, createdTime ASC")
    LiveData<List<Note>> creationOldToNew();

    // Filtered sorting
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, title ASC")
    LiveData<List<Note>> sortNameAZFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, title DESC")
    LiveData<List<Note>> sortNameZAFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, reminderTime DESC")
    LiveData<List<Note>> sortReminderFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> lastModifiedNewFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, modifiedTime ASC")
    LiveData<List<Note>> lastModifiedOldFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, createdTime DESC")
    LiveData<List<Note>> creationNewFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, createdTime ASC")
    LiveData<List<Note>> creationOldFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isDeleted = 0 AND createdTime BETWEEN :start AND :end ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getNotesByDate(long start, long end);

    @Query("SELECT createdTime FROM notes_table WHERE isArchived = 0 AND isDeleted = 0")
    LiveData<List<Long>> getAllNoteDates();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getArchivedNotes();

    @Query("SELECT * FROM notes_table WHERE isDeleted = 1 ORDER BY modifiedTime DESC")
    LiveData<List<Note>> getDeletedNotes();

    @Query("SELECT COUNT(*) FROM notes_table WHERE isArchived = 1 AND isDeleted = 0")
    LiveData<Integer> getArchivedCount();

    @Query("SELECT COUNT(*) FROM notes_table WHERE isDeleted = 1")
    LiveData<Integer> getDeletedCount();

    @Query("DELETE FROM notes_table WHERE isDeleted = 1")
    void emptyRecycleBin();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getArchivedNotesByCategory(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND noteType = :type ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> getArchivedNotesByType(String type);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND title LIKE '%' || :search || '%' ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> searchArchivedNotes(String search);

    @Query("SELECT * FROM notes_table WHERE isDeleted = 1 AND title LIKE '%' || :search || '%' ORDER BY modifiedTime DESC")
    LiveData<List<Note>> searchDeletedNotes(String search);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, category ASC")
    LiveData<List<Note>> sortArchivedByCategory();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, category ASC")
    LiveData<List<Note>> sortArchivedByCategoryFilter(String category);

    // Archived Sorting
    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, title ASC")
    LiveData<List<Note>> sortArchivedNameAZ();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, title DESC")
    LiveData<List<Note>> sortArchivedNameZA();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> sortArchivedModifiedNew();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, modifiedTime ASC")
    LiveData<List<Note>> sortArchivedModifiedOld();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, createdTime DESC")
    LiveData<List<Note>> sortArchivedCreatedNew();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, createdTime ASC")
    LiveData<List<Note>> sortArchivedCreatedOld();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, reminderTime DESC")
    LiveData<List<Note>> sortArchivedReminderTime();

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, reminderTime DESC")
    LiveData<List<Note>> sortArchivedReminderFilter(String category);

    // Archived Filtered Sorting
    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, title ASC")
    LiveData<List<Note>> sortArchivedNameAZFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, title DESC")
    LiveData<List<Note>> sortArchivedNameZAFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, modifiedTime DESC")
    LiveData<List<Note>> sortArchivedModifiedNewFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, modifiedTime ASC")
    LiveData<List<Note>> sortArchivedModifiedOldFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, createdTime DESC")
    LiveData<List<Note>> sortArchivedCreatedNewFilter(String category);

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isDeleted = 0 AND category = :category ORDER BY isPinned DESC, createdTime ASC")
    LiveData<List<Note>> sortArchivedCreatedOldFilter(String category);
}
