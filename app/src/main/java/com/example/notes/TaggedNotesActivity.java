package com.example.notes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.notes.model.SharedNote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaggedNotesActivity extends AppCompatActivity {
    private static final String TAG = "TaggedNotesActivity";

    private RecyclerView recyclerView;
    private ConstraintLayout emptyState;
    private SearchView searchView;
    private TaggedNoteAdapter adapter;
    private List<Note> allTaggedNotes = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_tagged_notes);

            // Initialize Firebase first
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Setup toolbar with back navigation
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
            }

            // Initialize views
            recyclerView = findViewById(R.id.notes_recycler_view);
            emptyState = findViewById(R.id.empty_state);
            searchView = findViewById(R.id.search_view);

            if (recyclerView == null || emptyState == null || searchView == null) {
                Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new TaggedNoteAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            // Setup search functionality
            setupSearchView();

            // Load notes
            loadTaggedNotes();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSearchView() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterNotes(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterNotes(newText);
                    return true;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        // If search is active and has text, clear it
        if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void filterNotes(String query) {
        List<Note> filteredNotes = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase().trim();

        if (lowercaseQuery.isEmpty()) {
            filteredNotes.addAll(allTaggedNotes);
        } else {
            for (Note note : allTaggedNotes) {
                if (note.getTitle().toLowerCase().contains(lowercaseQuery) ||
                        note.getContent().toLowerCase().contains(lowercaseQuery) ||
                        note.getCategory().toLowerCase().contains(lowercaseQuery)) {
                    filteredNotes.add(note);
                }
            }
        }

        adapter.updateNotes(filteredNotes);
        updateEmptyState(filteredNotes.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void loadTaggedNotes() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("shared_notes")
                .whereEqualTo("taggedUserId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        Toast.makeText(TaggedNotesActivity.this,
                                "Error loading notes: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allTaggedNotes.clear();
                    try {
                        if (value != null && !value.isEmpty()) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                SharedNote sharedNote = doc.toObject(SharedNote.class);
                                if (sharedNote != null) {
                                    // Add safety check for potential null values
                                    String noteId = sharedNote.getNoteId();
                                    if (noteId == null || noteId.isEmpty())
                                        continue;

                                    try {
                                        Note note = new Note(
                                                Integer.parseInt(noteId),
                                                sharedNote.getAuthorEmail() != null ? sharedNote.getAuthorEmail()
                                                        : "Unknown",
                                                sharedNote.getContent() != null ? sharedNote.getContent() : "",
                                                sharedNote.getCategory() != null ? sharedNote.getCategory()
                                                        : "Personal",
                                                sharedNote.getTimestamp() != null ? sharedNote.getTimestamp().getTime()
                                                        : System.currentTimeMillis(),
                                                0);
                                        allTaggedNotes.add(note);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Error parsing note ID", e);
                                    }
                                }
                            }
                        }

                        // Apply existing filter if any
                        if (searchView != null && searchView.getQuery().length() > 0) {
                            filterNotes(searchView.getQuery().toString());
                        } else {
                            adapter.updateNotes(allTaggedNotes);
                            updateEmptyState(allTaggedNotes.isEmpty());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing notes", e);
                        Toast.makeText(TaggedNotesActivity.this,
                                "Error processing notes",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
