package com.example.notes;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends Fragment {

    private RecyclerView notesRecyclerView;
    private LinearLayout emptyState;
    private SearchView searchView;
    private List<Note> notesList = new ArrayList<>();
    private NoteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_list, container, false);

        // Initialize views
        notesRecyclerView = view.findViewById(R.id.notes_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        searchView = view.findViewById(R.id.search_view);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up search functionality
        setupSearch();

        // Load data
        loadNotes();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the database
        if (getContext() != null) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getContext());
            // Ensure the database is created
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            db.close();
        }
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(getContext(), notesList);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesRecyclerView.setAdapter(adapter);

        // Add click listener to open note detail
        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                // Open note detail with this note
                Intent intent = new Intent(getContext(), NoteDetailActivity.class);
                intent.putExtra("noteId", (long) note.getId());
                startActivity(intent);
            }
        });
    }

    private void setupSearch() {
        // Set focus change listener to immediately perform search when the search view
        // gets focus
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Open the keyboard when the search view is focused
                searchView.requestFocus();

                // If there's already text in the search field, filter using that
                String query = searchView.getQuery().toString();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
            }
        });

        // Regular query text listeners
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search in real-time as user types
                performSearch(newText);
                return true;
            }
        });

        // Customize search view click behavior
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchPlate.setOnClickListener(v -> {
            searchView.setIconified(false); // This expands the search view
            searchView.requestFocus();
        });
    }

    private void performSearch(String query) {
        if (getContext() == null)
            return;

        List<Note> searchResults;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());

        if (query.isEmpty()) {
            // If query is empty, show all notes
            searchResults = databaseHelper.getAllNotes();
        } else {
            // Use the database search method
            searchResults = databaseHelper.searchNotes(query);
        }

        if (adapter != null) {
            adapter.updateList(searchResults);
            showEmptyStateIfNeeded(searchResults);
        }
    }

    private void loadNotes() {
        // Clear existing list to avoid duplicates
        notesList.clear();

        // Try to load from database first
        if (getContext() != null) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getContext());
            List<Note> dbNotes = dbHelper.getAllNotes();

            if (dbNotes != null && !dbNotes.isEmpty()) {
                notesList.addAll(dbNotes);
            } else {
                // If database is empty, add sample notes
                addSampleNotes();
            }
        } else {
            // Fallback if context is null
            addSampleNotes();
        }

        if (adapter != null) {
            adapter.updateList(notesList);
            showEmptyStateIfNeeded(notesList);
        }
    }

    private void addSampleNotes() {
        notesList.add(new Note(1, "Meeting Notes", "Discuss project timeline and deliverables", "Work",
                System.currentTimeMillis(), 2));
        notesList.add(new Note(2, "Shopping List", "Milk, Eggs, Bread, Cheese", "Personal",
                System.currentTimeMillis() - 86400000, 1));
        notesList.add(new Note(3, "Book Recommendations", "1. Atomic Habits\n2. Deep Work\n3. Digital Minimalism",
                "Study", System.currentTimeMillis() - 172800000, 0));
    }

    private void showEmptyStateIfNeeded(List<Note> notes) {
        if (notes.isEmpty()) {
            notesRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            notesRecyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh notes list when returning to fragment
        refreshNotes();
    }

    private void refreshNotes() {
        try {
            if (getContext() != null) {
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());
                notesList.clear();
                notesList.addAll(databaseHelper.getAllNotes());
                if (notesList.isEmpty()) {
                    // Add sample notes if database is empty
                    addSampleNotes();
                }

                if (adapter != null) {
                    adapter.updateList(notesList);
                    showEmptyStateIfNeeded(notesList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample notes if there's any error
            notesList.clear();
            addSampleNotes();
            if (adapter != null) {
                adapter.updateList(notesList);
                showEmptyStateIfNeeded(notesList);
            }
        }
    }
}
