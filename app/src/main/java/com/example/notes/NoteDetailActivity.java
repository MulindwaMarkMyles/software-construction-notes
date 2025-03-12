package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class NoteDetailActivity extends AppCompatActivity {

    private TextInputEditText noteHeadingEditText, noteDetailsEditText;
    private MaterialButton updateButton, deleteButton;
    private long noteId;
    private DatabaseHelper databaseHelper;
    private Note note;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        // Initialize views
        noteHeadingEditText = findViewById(R.id.noteHeadingEditText);
        noteDetailsEditText = findViewById(R.id.noteDetailsEditText);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Get note ID from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("noteId")) {
            noteId = intent.getLongExtra("noteId", -1);
            if (noteId != -1) {
                // Load existing note
                loadNote();
                // Update button text
                updateButton.setText("Update");
                // Show delete button
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                // Create new note
                createNewNote();
            }
        } else {
            // Create new note
            createNewNote();
        }

        // Set up button listeners
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();
            }
        });
    }

    private void createNewNote() {
        // Initialize a new empty note
        note = new Note(-1, "", "", "Personal", System.currentTimeMillis(), 0);
        setTitle("Create New Note");
        // Update button text
        updateButton.setText("Save");
        // Hide delete button (can't delete a note that doesn't exist yet)
        deleteButton.setVisibility(View.GONE);
    }

    private void loadNote() {
        note = databaseHelper.getNoteById((int) noteId);
        if (note != null) {
            noteHeadingEditText.setText(note.getTitle());
            noteDetailsEditText.setText(note.getContent());
            setTitle("Edit: " + note.getTitle());
        }
    }

    private void saveNote() {
        String heading = noteHeadingEditText.getText().toString().trim();
        String details = noteDetailsEditText.getText().toString().trim();

        if (heading.isEmpty()) {
            noteHeadingEditText.setError("Heading cannot be empty");
            return;
        }

        // Update note properties
        note.setTitle(heading);
        note.setContent(details);
        note.setTimestamp(System.currentTimeMillis());

        // Save to database
        long id = databaseHelper.saveNote(note);
        if (id > 0) {
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNote() {
        if (note != null) {
            databaseHelper.deleteNote(note);
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
