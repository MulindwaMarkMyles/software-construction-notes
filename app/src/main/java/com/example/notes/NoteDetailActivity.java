package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class NoteDetailActivity extends AppCompatActivity {

    private TextInputEditText noteHeadingEditText, noteDetailsEditText;
    private MaterialButton updateButton, deleteButton;
    private long noteId;
    private DatabaseHelper databaseHelper;
    private Note note;
    private Toolbar toolbar;
    private ChipGroup categoryChips;
    private Chip chipPersonal, chipWork, chipStudy, chipMisc;
    private String currentCategory = "Personal";
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        SettingsManager.getInstance(this).applyTheme();

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

        // Initialize category chips
        categoryChips = findViewById(R.id.category_chips);
        chipPersonal = findViewById(R.id.chip_personal);
        chipWork = findViewById(R.id.chip_work);
        chipStudy = findViewById(R.id.chip_study);
        chipMisc = findViewById(R.id.chip_misc);

        setupCategoryChips();

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

    private void setupCategoryChips() {
        categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Ensure at least one category is selected
                chipPersonal.setChecked(true);
                currentCategory = "Personal";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    currentCategory = selectedChip.getText().toString();
                }
            }
        });
    }

    private void createNewNote() {
        // Initialize a new empty note with default category from settings
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        String defaultCategory = settingsManager.getDefaultCategory();
        note = new Note(-1, "", "", defaultCategory, System.currentTimeMillis(), 0);

        // Set the appropriate chip as checked
        switch (defaultCategory) {
            case "Work":
                chipWork.setChecked(true);
                currentCategory = "Work";
                break;
            case "Study":
                chipStudy.setChecked(true);
                currentCategory = "Study";
                break;
            case "Miscellaneous":
                chipMisc.setChecked(true);
                currentCategory = "Miscellaneous";
                break;
            default:
                chipPersonal.setChecked(true);
                currentCategory = "Personal";
                break;
        }

        setTitle("Create New Note");
        updateButton.setText("Save");
        deleteButton.setVisibility(View.GONE);
    }

    private void loadNote() {
        note = databaseHelper.getNoteById((int) noteId);
        if (note != null) {
            noteHeadingEditText.setText(note.getTitle());
            noteDetailsEditText.setText(note.getContent());
            setTitle("Edit: " + note.getTitle());

            // Set the correct category chip
            switch (note.getCategory()) {
                case "Work":
                    chipWork.setChecked(true);
                    break;
                case "Study":
                    chipStudy.setChecked(true);
                    break;
                case "Miscellaneous":
                    chipMisc.setChecked(true);
                    break;
                default:
                    chipPersonal.setChecked(true);
                    break;
            }
        }
    }

    private void saveNote() {
        String heading = noteHeadingEditText.getText().toString().trim();
        String details = noteDetailsEditText.getText().toString().trim();

        if (heading.isEmpty()) {
            noteHeadingEditText.setError("Heading cannot be empty");
            return;
        }

        try {
            // Create a new note if null or update existing note
            if (note == null) {
                note = new Note(-1, heading, details, currentCategory, System.currentTimeMillis(), 0);
            } else {
                note.setTitle(heading);
                note.setContent(details);
                note.setCategory(currentCategory);
                note.setTimestamp(System.currentTimeMillis());
                // Preserve favorite status when updating
            }

            // Debug output
            Toast.makeText(this, "Saving note with ID: " + note.getId(), Toast.LENGTH_SHORT).show();

            // Save to database
            long result = databaseHelper.saveNote(note);
            if (result > 0) {
                // Update the note's ID if it was a new note
                if (note.getId() <= 0) {
                    note.setId((int) result);
                }
                Toast.makeText(this, "Note saved successfully with ID: " + result, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteNote() {
        if (note != null) {
            SettingsManager settingsManager = SettingsManager.getInstance(this);
            if (settingsManager.shouldConfirmDelete()) {
                // Show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Move to Trash")
                        .setMessage("Are you sure you want to move this note to trash?")
                        .setPositiveButton("Move to Trash", (dialog, which) -> {
                            // User confirmed, move to trash
                            databaseHelper.trashNote(note);
                            Toast.makeText(this, "Note moved to trash", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // No confirmation needed, move to trash directly
                databaseHelper.trashNote(note);
                Toast.makeText(this, "Note moved to trash", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_detail, menu);
        this.menu = menu;
        updateFavoriteIcon();
        return true;
    }

    private void updateFavoriteIcon() {
        if (menu != null && note != null) {
            MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
            if (favoriteItem != null) {
                favoriteItem.setIcon(note.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            toggleFavorite();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        if (note != null) {
            note.setFavorite(!note.isFavorite());
            updateFavoriteIcon();

            try {
                // Save changes immediately
                long result = databaseHelper.saveNote(note);
                if (result > 0) {
                    Toast.makeText(this,
                            note.isFavorite() ? "Added to favorites" : "Removed from favorites",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update favorite status", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error updating favorite status: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
