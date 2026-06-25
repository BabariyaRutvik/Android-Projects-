package com.example.quicknotes.Database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteRepository
{
    private final NoteDao noteDao;
    private final LiveData<List<Note>>allNotes;

    // constructor
    public NoteRepository(Application application){
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    // insert data
    public long insert(Note note) {
        return noteDao.insert(note);
    }

    // update
    public void update(Note note) {
        noteDao.update(note);
    }

    // delete
    public void delete(Note note) {
        noteDao.delete(note);
    }

    // delete all
    public void deleteAll() {
        noteDao.deleteAll();
    }

    // get all notes
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    // search notes
    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    // category filter
    public LiveData<List<Note>> getNotesByCategory(String category) {
        return noteDao.getNotesByCategory(category);
    }

    public LiveData<List<Note>> getNotesByType(String type) {
        return noteDao.getNotesByType(type);
    }

    public LiveData<List<Note>> sortByCategory() {
        return noteDao.sortByCategory();
    }

    // get note by id
    public Note getNoteById(int id) {
        return noteDao.getNoteById(id);
    }

    public LiveData<List<Note>> sortNameAZ() {
        return noteDao.sortNameAZ();
    }

    public LiveData<List<Note>> sortNameZA() {
        return noteDao.sortNameZA();
    }

    public LiveData<List<Note>> sortReminderTime() {
        return noteDao.sortReminderTime();
    }

    public LiveData<List<Note>> lastModifiedNewToOld() {
        return noteDao.lastModifiedNewToOld();
    }

    public LiveData<List<Note>> lastModifiedOldToNew() {
        return noteDao.lastModifiedOldToNew();
    }

    public LiveData<List<Note>> creationNewToOld() {
        return noteDao.creationNewToOld();
    }

    public LiveData<List<Note>> creationOldToNew() {
        return noteDao.creationOldToNew();
    }

    public LiveData<List<Note>> sortNameAZFilter(String category) {
        return noteDao.sortNameAZFilter(category);
    }

    public LiveData<List<Note>> sortNameZAFilter(String category) {
        return noteDao.sortNameZAFilter(category);
    }

    public LiveData<List<Note>> sortReminderFilter(String category) {
        return noteDao.sortReminderFilter(category);
    }

    public LiveData<List<Note>> lastModifiedNewFilter(String category) {
        return noteDao.lastModifiedNewFilter(category);
    }

    public LiveData<List<Note>> lastModifiedOldFilter(String category) {
        return noteDao.lastModifiedOldFilter(category);
    }

    public LiveData<List<Note>> creationNewFilter(String category) {
        return noteDao.creationNewFilter(category);
    }

    public LiveData<List<Note>> creationOldFilter(String category) {
        return noteDao.creationOldFilter(category);
    }

    public LiveData<List<Note>> getNotesByDate(long start, long end) {
        return noteDao.getNotesByDate(start, end);
    }

    public LiveData<List<Long>> getAllNoteDates() {
        return noteDao.getAllNoteDates();
    }

    public LiveData<List<Note>> getArchivedNotes() {
        return noteDao.getArchivedNotes();
    }

    public LiveData<List<Note>> getDeletedNotes() {
        return noteDao.getDeletedNotes();
    }

    public LiveData<Integer> getArchivedCount() {
        return noteDao.getArchivedCount();
    }

    public LiveData<Integer> getDeletedCount() {
        return noteDao.getDeletedCount();
    }

    public void emptyRecycleBin() {
        NoteDatabase.instance.getQueryExecutor().execute(() -> {
            noteDao.emptyRecycleBin();
        });
    }

    public LiveData<List<Note>> getArchivedNotesByCategory(String category) {
        return noteDao.getArchivedNotesByCategory(category);
    }

    public LiveData<List<Note>> getArchivedNotesByType(String type) {
        return noteDao.getArchivedNotesByType(type);
    }

    public LiveData<List<Note>> searchArchivedNotes(String search) {
        return noteDao.searchArchivedNotes(search);
    }

    public LiveData<List<Note>> searchDeletedNotes(String search) {
        return noteDao.searchDeletedNotes(search);
    }

    public void moveToRecycleBin(Note note) {
        note.setDeleted(true);
        update(note);
    }

    public void moveToArchive(Note note) {
        note.setArchived(true);
        update(note);
    }

    public LiveData<List<Note>> sortArchivedNameAZ() {
        return noteDao.sortArchivedNameAZ();
    }

    public LiveData<List<Note>> sortArchivedByCategory() {
        return noteDao.sortArchivedByCategory();
    }

    public LiveData<List<Note>> sortArchivedByCategoryFilter(String category) {
        return noteDao.sortArchivedByCategoryFilter(category);
    }

    public LiveData<List<Note>> sortArchivedNameZA() {
        return noteDao.sortArchivedNameZA();
    }

    public LiveData<List<Note>> sortArchivedModifiedNew() {
        return noteDao.sortArchivedModifiedNew();
    }

    public LiveData<List<Note>> sortArchivedModifiedOld() {
        return noteDao.sortArchivedModifiedOld();
    }

    public LiveData<List<Note>> sortArchivedCreatedNew() {
        return noteDao.sortArchivedCreatedNew();
    }

    public LiveData<List<Note>> sortArchivedCreatedOld() {
        return noteDao.sortArchivedCreatedOld();
    }

    public LiveData<List<Note>> sortArchivedNameAZFilter(String category) {
        return noteDao.sortArchivedNameAZFilter(category);
    }

    public LiveData<List<Note>> sortArchivedNameZAFilter(String category) {
        return noteDao.sortArchivedNameZAFilter(category);
    }

    public LiveData<List<Note>> sortArchivedModifiedNewFilter(String category) {
        return noteDao.sortArchivedModifiedNewFilter(category);
    }

    public LiveData<List<Note>> sortArchivedModifiedOldFilter(String category) {
        return noteDao.sortArchivedModifiedOldFilter(category);
    }

    public LiveData<List<Note>> sortArchivedCreatedNewFilter(String category) {
        return noteDao.sortArchivedCreatedNewFilter(category);
    }

    public LiveData<List<Note>> sortArchivedCreatedOldFilter(String category) {
        return noteDao.sortArchivedCreatedOldFilter(category);
    }

    public LiveData<List<Note>> sortArchivedReminderTime() {
        return noteDao.sortArchivedReminderTime();
    }

    public LiveData<List<Note>> sortArchivedReminderFilter(String category) {
        return noteDao.sortArchivedReminderFilter(category);
    }
}
