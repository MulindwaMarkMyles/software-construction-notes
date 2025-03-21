package com.example.notes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        setContentView(R.layout.activity_tagged_notes);

        // Setup toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        recyclerView = findViewById(R.id.notes_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        searchView = findViewById(R.id.search_view);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaggedNoteAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Setup search functionality
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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Load notes
        loadTaggedNotes();
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
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("shared_notes")
                .whereEqualTo("taggedUserId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    allTaggedNotes.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            SharedNote sharedNote = doc.toObject(SharedNote.class);
                            if (sharedNote != null) {
                                Note note = new Note(
                                        Integer.parseInt(sharedNote.getNoteId()),
                                        sharedNote.getAuthorEmail(), // Use author email instead of title
                                        sharedNote.getContent(),
                                        sharedNote.getCategory(),
                                        sharedNote.getTimestamp() != null ? sharedNote.getTimestamp().getTime()
                                                : System.currentTimeMillis(),
                                        0);
                                allTaggedNotes.add(note);
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
                });
    }
}
