package com.example.quicknotes.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes_table")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    // TEXT or CHECKLIST
    @ColumnInfo(name = "noteType")
    private String noteType;

    // All, Personal, Work, Other, Untitled
    @ColumnInfo(name = "category")
    private String category;

    // Creation Time
    @ColumnInfo(name = "createdTime")
    private long createdTime;

    // Last Modified Time
    @ColumnInfo(name = "modifiedTime")
    private long modifiedTime;

    // Reminder Time
    @ColumnInfo(name = "reminderTime")
    private long reminderTime;

    @ColumnInfo(name = "noteColor")
    private String noteColor;

    @ColumnInfo(name = "isPinned")
    private boolean isPinned;

    @ColumnInfo(name = "isCompleted")
    private boolean isCompleted;

    @ColumnInfo(name = "isArchived")
    private boolean isArchived;

    @ColumnInfo(name = "isDeleted")
    private boolean isDeleted;

    @ColumnInfo(name = "repeatType")
    private String repeatType;

    @ColumnInfo(name = "repeatInterval")
    private int repeatInterval;

    @ColumnInfo(name = "repeatUnit")
    private String repeatUnit;

    @ColumnInfo(name = "repeatDays")
    private String repeatDays;

    @ColumnInfo(name = "isReminderEnabled")
    private boolean isReminderEnabled;

    @ColumnInfo(name = "isLocked")
    private boolean isLocked;

    public Note(String title,
                      String description,
                      String noteType,
                      String category,
                      long createdTime,
                      long modifiedTime,
                      long reminderTime,
                      String noteColor,
                      boolean isPinned) {

        this.title = title;
        this.description = description;
        this.noteType = noteType;
        this.category = category;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.reminderTime = reminderTime;
        this.noteColor = noteColor;
        this.isPinned = isPinned;
        this.isCompleted = false;
        this.isArchived = false;
        this.isDeleted = false;
        this.repeatType = "None";
        this.repeatInterval = 1;
        this.repeatUnit = "Day";
        this.repeatDays = "";
        this.isReminderEnabled = false;
        this.isLocked = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getNoteColor() {
        return noteColor;
    }

    public void setNoteColor(String noteColor) {
        this.noteColor = noteColor;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public String getRepeatUnit() {
        return repeatUnit;
    }

    public void setRepeatUnit(String repeatUnit) {
        this.repeatUnit = repeatUnit;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public boolean isReminderEnabled() {
        return isReminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        isReminderEnabled = reminderEnabled;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
