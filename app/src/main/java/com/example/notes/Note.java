package com.example.notes;

public class Note {
    private int id;
    private String title;
    private String content;
    private String category;
    private long timestamp;
    private int priority; // 0 = low, 1 = medium, 2 = high

    public Note(int id, String title, String content, String category, long timestamp, int priority) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.timestamp = timestamp;
        this.priority = priority;
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
}
