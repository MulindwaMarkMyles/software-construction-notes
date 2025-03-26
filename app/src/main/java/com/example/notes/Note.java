package com.example.notes;

public class Note {
    private int id;
    private String title;
    private String content;
    private String category;
    private long timestamp;
    private int priority; // 0 = low, 1 = medium, 2 = high
    private boolean isFavorite;
    private boolean isInDrive; // New field to track Drive upload status

    public Note(int id, String title, String content, String category, long timestamp, int priority) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.timestamp = timestamp;
        this.priority = priority;
        this.isFavorite = false;
        this.isInDrive = false; // Default to false for new notes
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isInDrive() {
        return isInDrive;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setInDrive(boolean inDrive) {
        isInDrive = inDrive;
    }
}
