package com.example.quicknotes.Database;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public  class NoteViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);

        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();


    }
    // insert data
    public long insert(Note note) {
        return repository.insert(note);
    }
    // update data
    public void update(Note note){
        repository.update(note);
    }
    public void delete(Note note) {
        repository.delete(note);
    }
    // delete all data
    public void deleteAll() {
        repository.deleteAll();
    }
    // get all notes

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }
    // search the note
    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }
    // category filter
    public LiveData<List<Note>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    public LiveData<List<Note>> getNotesByType(String type) {
        return repository.getNotesByType(type);
    }

    public Note getNoteById(int id) {
        return repository.getNoteById(id);
    }

    public LiveData<List<Note>> sortByCategory() {
        return repository.sortByCategory();
    }

    // sorting


    // a - z
    public LiveData<List<Note>> sortNameAZ() {
        return repository.sortNameAZ();
    }
    // z - a
    public LiveData<List<Note>> sortNameZA() {
        return repository.sortNameZA();
    }
    // sort by reminder time
    public LiveData<List<Note>> sortReminderTime() {
        return repository.sortReminderTime();
    }
    // last modified new to old
    public LiveData<List<Note>> lastModifiedNewToOld() {
        return repository.lastModifiedNewToOld();
    }
    // old to new
    public LiveData<List<Note>> lastModifiedOldToNew() {
        return repository.lastModifiedOldToNew();
    }
    // creation time new to old
    public LiveData<List<Note>> creationNewToOld() {
        return repository.creationNewToOld();
    }
    // old to new
    public LiveData<List<Note>> creationOldToNew() {
        return repository.creationOldToNew();
    }

    public LiveData<List<Note>> sortNameAZFilter(String category) {
        return repository.sortNameAZFilter(category);
    }

    public LiveData<List<Note>> sortNameZAFilter(String category) {
        return repository.sortNameZAFilter(category);
    }

    public LiveData<List<Note>> sortReminderFilter(String category) {
        return repository.sortReminderFilter(category);
    }

    public LiveData<List<Note>> lastModifiedNewFilter(String category) {
        return repository.lastModifiedNewFilter(category);
    }

    public LiveData<List<Note>> lastModifiedOldFilter(String category) {
        return repository.lastModifiedOldFilter(category);
    }

    public LiveData<List<Note>> creationNewFilter(String category) {
        return repository.creationNewFilter(category);
    }

    public LiveData<List<Note>> creationOldFilter(String category) {
        return repository.creationOldFilter(category);
    }

    public LiveData<List<Note>> getNotesByDate(long start, long end) {
        return repository.getNotesByDate(start, end);
    }

    public LiveData<List<Long>> getAllNoteDates() {
        return repository.getAllNoteDates();
    }

    public LiveData<List<Note>> getArchivedNotes() {
        return repository.getArchivedNotes();
    }

    public LiveData<List<Note>> getDeletedNotes() {
        return repository.getDeletedNotes();
    }

    public LiveData<Integer> getArchivedCount() {
        return repository.getArchivedCount();
    }

    public LiveData<Integer> getDeletedCount() {
        return repository.getDeletedCount();
    }

    public void emptyRecycleBin() {
        repository.emptyRecycleBin();
    }

    public LiveData<List<Note>> getArchivedNotesByCategory(String category) {
        return repository.getArchivedNotesByCategory(category);
    }

    public LiveData<List<Note>> getArchivedNotesByType(String type) {
        return repository.getArchivedNotesByType(type);
    }

    public LiveData<List<Note>> searchArchivedNotes(String search) {
        return repository.searchArchivedNotes(search);
    }

    public LiveData<List<Note>> searchDeletedNotes(String search) {
        return repository.searchDeletedNotes(search);
    }

    public void moveToRecycleBin(Note note) {
        repository.moveToRecycleBin(note);
    }

    public void moveToArchive(Note note) {
        repository.moveToArchive(note);
    }

    public LiveData<List<Note>> sortArchivedNameAZ() {
        return repository.sortArchivedNameAZ();
    }

    public LiveData<List<Note>> sortArchivedByCategory() {
        return repository.sortArchivedByCategory();
    }

    public LiveData<List<Note>> sortArchivedByCategoryFilter(String category) {
        return repository.sortArchivedByCategoryFilter(category);
    }

    public LiveData<List<Note>> sortArchivedNameZA() {
        return repository.sortArchivedNameZA();
    }

    public LiveData<List<Note>> sortArchivedModifiedNew() {
        return repository.sortArchivedModifiedNew();
    }

    public LiveData<List<Note>> sortArchivedModifiedOld() {
        return repository.sortArchivedModifiedOld();
    }

    public LiveData<List<Note>> sortArchivedCreatedNew() {
        return repository.sortArchivedCreatedNew();
    }

    public LiveData<List<Note>> sortArchivedCreatedOld() {
        return repository.sortArchivedCreatedOld();
    }

    public LiveData<List<Note>> sortArchivedNameAZFilter(String category) {
        return repository.sortArchivedNameAZFilter(category);
    }

    public LiveData<List<Note>> sortArchivedNameZAFilter(String category) {
        return repository.sortArchivedNameZAFilter(category);
    }

    public LiveData<List<Note>> sortArchivedModifiedNewFilter(String category) {
        return repository.sortArchivedModifiedNewFilter(category);
    }

    public LiveData<List<Note>> sortArchivedModifiedOldFilter(String category) {
        return repository.sortArchivedModifiedOldFilter(category);
    }

    public LiveData<List<Note>> sortArchivedCreatedNewFilter(String category) {
        return repository.sortArchivedCreatedNewFilter(category);
    }

    public LiveData<List<Note>> sortArchivedCreatedOldFilter(String category) {
        return repository.sortArchivedCreatedOldFilter(category);
    }

    public LiveData<List<Note>> sortArchivedReminderTime() {
        return repository.sortArchivedReminderTime();
    }

    public LiveData<List<Note>> sortArchivedReminderFilter(String category) {
        return repository.sortArchivedReminderFilter(category);
    }
}
