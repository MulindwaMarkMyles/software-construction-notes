package com.example.notes;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class NoteEditorActivity extends AppCompatActivity {

    private TextInputEditText titleInput, contentInput;
    private RadioButton priorityLow, priorityMedium, priorityHigh;
    private Chip chipPersonal, chipWork, chipStudy, chipMisc;
    private FloatingActionButton fabSave;
    private Toolbar toolbar;

    private Note currentNote;
    private int noteId = -1;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        // Initialize views
        initViews();

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Check if we're editing an existing note
        if (getIntent().hasExtra("NOTE_ID")) {
            noteId = getIntent().getIntExtra("NOTE_ID", -1);
            loadNote(noteId);
            setTitle(R.string.edit_note);
        } else {
            setTitle(R.string.new_note);
            currentNote = new Note(-1, "", "", "Personal", System.currentTimeMillis(), 0);
        }

        // Set up formatting buttons
        setupFormattingButtons();

        // Set up save button
        setupSaveButton();

        // Set up category chips
        setupCategoryChips();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        titleInput = findViewById(R.id.note_title_input);
        contentInput = findViewById(R.id.note_content_input);
        priorityLow = findViewById(R.id.priority_low);
        priorityMedium = findViewById(R.id.priority_medium);
        priorityHigh = findViewById(R.id.priority_high);
        chipPersonal = findViewById(R.id.chip_personal);
        chipWork = findViewById(R.id.chip_work);
        chipStudy = findViewById(R.id.chip_study);
        chipMisc = findViewById(R.id.chip_misc);
        fabSave = findViewById(R.id.fab_save);
    }

    private void loadNote(int noteId) {
        currentNote = databaseHelper.getNoteById(noteId);
        if (currentNote != null) {
            titleInput.setText(currentNote.getTitle());
            contentInput.setText(currentNote.getContent());

            // Set priority
            switch (currentNote.getPriority()) {
                case 2:
                    priorityHigh.setChecked(true);
                    break;
                case 1:
                    priorityMedium.setChecked(true);
                    break;
                default:
                    priorityLow.setChecked(true);
                    break;
            }

            // Set category
            switch (currentNote.getCategory()) {
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

    private void setupFormattingButtons() {
        findViewById(R.id.format_bold).setOnClickListener(v -> applyFormatting(new StyleSpan(Typeface.BOLD)));
        findViewById(R.id.format_italic).setOnClickListener(v -> applyFormatting(new StyleSpan(Typeface.ITALIC)));
        findViewById(R.id.format_underline).setOnClickListener(v -> applyFormatting(new UnderlineSpan()));
        findViewById(R.id.format_list_bulleted).setOnClickListener(v -> insertBulletedList());
        findViewById(R.id.format_list_numbered).setOnClickListener(v -> insertNumberedList());
    }

    private void setupSaveButton() {
        fabSave.setOnClickListener(v -> saveNote());
    }

    private void setupCategoryChips() {
        chipPersonal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentNote.setCategory("Personal");
            }
        });

        chipWork.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentNote.setCategory("Work");
            }
        });

        chipStudy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentNote.setCategory("Study");
            }
        });

        chipMisc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentNote.setCategory("Miscellaneous");
            }
        });
    }

    private void saveNote() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.title_required));
            titleInput.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            contentInput.setError(getString(R.string.content_required));
            contentInput.requestFocus();
            return;
        }

        // Update note data
        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setTimestamp(System.currentTimeMillis());

        // Set priority
        if (priorityHigh.isChecked()) {
            currentNote.setPriority(2);
        } else if (priorityMedium.isChecked()) {
            currentNote.setPriority(1);
        } else {
            currentNote.setPriority(0);
        }

        // Save to database
        long id = databaseHelper.saveNote(currentNote);
        if (id > 0) {
            Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show();
            finish(); // Close activity and return to main
        } else {
            Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFormatting(Object span) {
        int start = contentInput.getSelectionStart();
        int end = contentInput.getSelectionEnd();

        if (start < end) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(contentInput.getText());
            ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            contentInput.setText(ssb);
            contentInput.setSelection(end);
        }
    }

    private void insertBulletedList() {
        int start = contentInput.getSelectionStart();
        contentInput.getText().insert(start, "\n• ");
        contentInput.setSelection(start + 3);
    }

    private void insertNumberedList() {
        int start = contentInput.getSelectionStart();
        int lineNum = 1;

        // Check if we already have a numbered list
        String text = contentInput.getText().toString();
        String[] lines = text.substring(0, start).split("\n");
        for (String line : lines) {
            if (line.matches("^\\d+\\.\\s.*$")) {
                lineNum++;
            }
        }

        contentInput.getText().insert(start, "\n" + lineNum + ". ");
        contentInput.setSelection(start + 4);
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
