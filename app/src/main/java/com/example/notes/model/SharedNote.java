package com.example.notes.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class SharedNote {
    private String noteId;
    private String title;
    private String content;
    private String category;
    private String ownerUserId;
    private String ownerEmail;
    private String taggedUserId;

    @ServerTimestamp
    private Date timestamp;

    // Add constructors, getters, and setters
    public SharedNote() {
    } // Required for Firestore

    public SharedNote(String noteId, String title, String content, String category,
            String ownerUserId, String ownerEmail, String taggedUserId) {
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.ownerUserId = ownerUserId;
        this.ownerEmail = ownerEmail;
        this.taggedUserId = taggedUserId;
    }

    // Add getters and setters
    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getTaggedUserId() {
        return taggedUserId;
    }

    public void setTaggedUserId(String taggedUserId) {
        this.taggedUserId = taggedUserId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
