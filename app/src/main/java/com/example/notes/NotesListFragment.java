package com.example.notes;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends Fragment {

    private RecyclerView notesRecyclerView;
    private LinearLayout emptyState;
    private SearchView searchView;
    private TextView emptyStateTitle;
    private TextView emptyStateSubtitle;
    private List<Note> notesList = new ArrayList<>();
    private NoteAdapter adapter;
    private String currentCategory = null;
    private boolean showFavoritesOnly = false;
    private boolean isDarkTheme = false;
    private AlertDialog userSearchDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_list, container, false);

        // Initialize views
        notesRecyclerView = view.findViewById(R.id.notes_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        searchView = view.findViewById(R.id.search_view);
        emptyStateTitle = view.findViewById(R.id.empty_state_title);
        emptyStateSubtitle = view.findViewById(R.id.empty_state_subtitle);

        // Check current theme
        updateThemeStatus();

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

        // Apply theme after creating adapter
        updateAdapterTheme();

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

        // Add long click listener for tagging
        adapter.setOnNoteLongClickListener((note, view1) -> {
            // Show options menu with tag option
            PopupMenu popup = new PopupMenu(getContext(), view1);
            popup.getMenuInflater().inflate(R.menu.note_context_menu, popup.getMenu());

            // Handle menu item clicks
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_tag_user) {
                    showTagUserDialog(note);
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    private void updateAdapterTheme() {
        if (adapter != null && getContext() != null) {
            adapter.updateTheme(isDarkTheme);
        }
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

        filterNotes(query);
    }

    private void updateThemeStatus() {
        if (getContext() != null) {
            // Use the same theme detection as in SettingsManager
            int nightModeFlags = getContext().getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK;
            isDarkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
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
            updateThemeStatus();
            updateAdapterTheme();
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
            updateEmptyStateMessage();
        } else {
            notesRecyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if theme has changed
        boolean wasDarkTheme = isDarkTheme;
        updateThemeStatus();
        if (wasDarkTheme != isDarkTheme) {
            updateAdapterTheme();
        }
        // Refresh notes list when returning to fragment
        refreshNotes();
    }

    public void filterByCategory(String category) {
        currentCategory = category;
        showFavoritesOnly = false;
        searchView.setQuery("", false); // Clear search when changing filters
        refreshNotes();
        updateEmptyStateMessage();
    }

    public void filterFavorites() {
        showFavoritesOnly = true;
        currentCategory = null;
        searchView.setQuery("", false); // Clear search when changing filters
        refreshNotes();
        updateEmptyStateMessage();
    }

    private void updateEmptyStateMessage() {
        if (emptyStateTitle == null || emptyStateSubtitle == null) {
            return;
        }

        if (showFavoritesOnly) {
            emptyStateTitle.setText("No Favorites");
            emptyStateSubtitle.setText("Notes marked as favorites will appear here");
        } else if (currentCategory != null) {
            emptyStateTitle.setText("No " + currentCategory + " Notes");
            emptyStateSubtitle.setText("Create a note in this category to see it here");
        } else {
            emptyStateTitle.setText(R.string.no_notes_found);
            emptyStateSubtitle.setText(R.string.create_first_note);
        }
    }

    private void refreshNotes() {
        try {
            if (getContext() != null) {
                updateThemeStatus();
                updateAdapterTheme();

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());
                notesList.clear();

                if (showFavoritesOnly) {
                    notesList.addAll(databaseHelper.getFavoriteNotes());
                } else if (currentCategory != null) {
                    notesList.addAll(databaseHelper.getNotesByCategory(currentCategory));
                } else {
                    notesList.addAll(databaseHelper.getAllNotes());
                }

                if (notesList.isEmpty() && currentCategory == null && !showFavoritesOnly) {
                    addSampleNotes();
                }

                if (adapter != null) {
                    adapter.updateList(notesList);
                    showEmptyStateIfNeeded(notesList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show empty state
            showEmptyStateIfNeeded(new ArrayList<>());
        }
    }

    private void filterNotes(String query) {
        if (getContext() == null)
            return;

        List<Note> searchResults;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());

        if (query.isEmpty()) {
            // Show category-filtered or all notes when query is empty
            if (showFavoritesOnly) {
                searchResults = databaseHelper.getFavoriteNotes();
            } else if (currentCategory != null) {
                searchResults = databaseHelper.getNotesByCategory(currentCategory);
            } else {
                searchResults = databaseHelper.getAllNotes();
            }
        } else {
            // Search within current category or favorites if filtered
            if (showFavoritesOnly) {
                searchResults = databaseHelper.searchFavoriteNotes(query);
            } else if (currentCategory != null) {
                searchResults = databaseHelper.searchNotesByCategory(query, currentCategory);
            } else {
                searchResults = databaseHelper.searchNotes(query);
            }
        }

        if (adapter != null) {
            adapter.updateList(searchResults);
            showEmptyStateIfNeeded(searchResults);
        }
    }

    /**
     * Closes the search view if it's open
     */
    public void closeSearch() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
    }

    private void showTagUserDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_user_search, null);
        EditText searchInput = view.findViewById(R.id.search_input);
        RecyclerView userList = view.findViewById(R.id.user_list);

        // Set up RecyclerView
        userList.setLayoutManager(new LinearLayoutManager(requireContext()));

        UserSearchAdapter userSearchAdapter = new UserSearchAdapter(user -> {
            // Handle user selection - tag the selected user
            tagUser(note, user.getUserId(), user.getEmail());
            if (userSearchDialog != null) {
                userSearchDialog.dismiss();
            }
        });

        userList.setAdapter(userSearchAdapter);

        // Search for users as user types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchUsers(s.toString(), userSearchAdapter);
            }
        });

        builder.setView(view)
                .setTitle("Tag a user")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        userSearchDialog = builder.create();
        userSearchDialog.show();
    }

    private void searchUsers(String query, UserSearchAdapter adapter) {
        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            return;
        }

        // Query Firestore for users
        FirebaseFirestore.getInstance().collection("users")
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", query + '\uf8ff')
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserTag> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        // Don't show current user in results
                        if (!doc.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            UserTag user = new UserTag(
                                    doc.getId(),
                                    doc.getString("email"),
                                    doc.getString("fcmToken"));
                            users.add(user);
                        }
                    }
                    adapter.submitList(users);
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Error searching users: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void tagUser(Note note, String userId, String email) {
        // Save to local database
        DatabaseHelper.getInstance(requireContext()).addTagToNote(note.getId(), userId, email);

        // Get current user details
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        // Save to Firestore for cross-device sync
        SharedNote sharedNote = new SharedNote(
                String.valueOf(note.getId()),
                note.getTitle(),
                note.getContent(),
                note.getCategory(),
                currentUser.getUid(),
                currentUser.getEmail(),
                userId,
                currentUser.getEmail());

        FirebaseFirestore.getInstance().collection("shared_notes")
                .add(sharedNote)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "SharedNote added with ID: " + documentReference.getId());
                    // Send notification
                    sendTagNotification(userId, email, note);
                    // Refresh adapter to show tag icon
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding shared note", e);
                    Toast.makeText(requireContext(), "Failed to share note", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendTagNotification(String userId, String email, Note note) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    String fcmToken = document.getString("fcmToken");
                    if (fcmToken != null) {
                        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        String title = getString(R.string.tag_notification_title);
                        String message = getString(R.string.tag_notification_message,
                                currentUserEmail, note.getTitle());

                        NotesFirebaseMessagingService.sendDirectNotification(
                                requireContext(), fcmToken, title, message);

                        Toast.makeText(requireContext(), "Notification sent to " + email,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
