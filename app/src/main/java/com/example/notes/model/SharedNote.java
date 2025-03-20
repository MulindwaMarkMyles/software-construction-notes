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
}
