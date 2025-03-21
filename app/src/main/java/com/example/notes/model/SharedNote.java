package com.example.notes.model;

import com.google.firebase.Timestamp;
import java.util.Date;

public class SharedNote {
    private String noteId;
    private String title;
    private String content;
    private String category;
    private String ownerUserId;
    private String ownerEmail;
    private String taggedUserId;
    private String authorEmail;
    private Date timestamp;

    // Default constructor required for Firestore
    public SharedNote() {
        // Initialize with default values to prevent null issues
        this.noteId = "";
        this.title = "";
        this.content = "";
        this.category = "";
        this.ownerUserId = "";
        this.ownerEmail = "";
        this.taggedUserId = "";
        this.authorEmail = "";
        this.timestamp = new Date();
    }

    // Full constructor
    public SharedNote(String noteId, String title, String content, String category,
            String ownerUserId, String ownerEmail, String taggedUserId, String authorEmail) {
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.ownerUserId = ownerUserId;
        this.ownerEmail = ownerEmail;
        this.taggedUserId = taggedUserId;
        this.authorEmail = authorEmail;
        this.timestamp = new Date();
    }

    // Getters and setters
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

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
