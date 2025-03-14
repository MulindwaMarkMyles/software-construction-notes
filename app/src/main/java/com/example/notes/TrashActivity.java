package com.example.notes;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrashActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TrashAdapter adapter;
    private DatabaseHelper databaseHelper;
    private View emptyState;
    private TextView emptyStateTitle, emptyStateSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.nav_trash);

        // Initialize views
        recyclerView = findViewById(R.id.trash_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        emptyStateTitle = findViewById(R.id.empty_state_title);
        emptyStateSubtitle = findViewById(R.id.empty_state_subtitle);
        
        // Set empty state message
        emptyStateTitle.setText(R.string.no_trashed_notes);
        emptyStateSubtitle.setText(R.string.trash_empty_message);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadTrashedNotes();
    }

    private void loadTrashedNotes() {
        List<Note> trashedNotes = databaseHelper.getTrashedNotes();
        adapter = new TrashAdapter(this, trashedNotes, new TrashAdapter.TrashActionListener() {
            @Override
            public void onRestore(Note note) {
                restoreNote(note);
            }

            @Override
            public void onDeletePermanently(Note note) {
                deletePermanently(note);
            }
        });
        recyclerView.setAdapter(adapter);
        
        // Show empty state if needed
        if (trashedNotes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void restoreNote(Note note) {
        databaseHelper.restoreNote(note);
        Toast.makeText(this, R.string.note_restored, Toast.LENGTH_SHORT).show();
        loadTrashedNotes(); // Refresh list
    }

    private void deletePermanently(Note note) {
        databaseHelper.deleteNote(note);
        Toast.makeText(this, R.string.note_deleted_permanently, Toast.LENGTH_SHORT).show();
        loadTrashedNotes(); // Refresh list
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
