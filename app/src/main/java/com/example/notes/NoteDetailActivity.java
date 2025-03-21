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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.notes.adapter.UserSearchAdapter;
import com.example.notes.model.UserTag;
import com.google.firebase.firestore.DocumentSnapshot;
import android.util.Log;
import com.example.notes.service.NotesFirebaseMessagingService;
import com.example.notes.model.SharedNote;
import com.example.notes.drive.DriveServiceHelper;
import com.example.notes.drive.DriveActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class NoteDetailActivity extends AppCompatActivity {

    private static final String TAG = "NoteDetailActivity";
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
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlertDialog userSearchDialog;
    private UserSearchAdapter userSearchAdapter;
    private RecyclerView userList;
    private DriveServiceHelper driveServiceHelper;

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
        if (intent != null) {
            // Check if we're receiving a long (the normal case)
            noteId = intent.getLongExtra("noteId", -1);

            // If the previous check failed, try for an int (fallback)
            if (noteId == -1) {
                noteId = intent.getIntExtra("noteId", -1);

                // Log the received ID for debugging
                Log.d(TAG, "Received note ID as int: " + noteId);
            } else {
                Log.d(TAG, "Received note ID as long: " + noteId);
            }

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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Add text watcher for @ symbol
        noteDetailsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 && s.charAt(start) == '@') {
                    showUserSearchDialog();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set up Google Drive integration
        setupDriveIntegration();
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
        try {
            note = databaseHelper.getNoteById((int) noteId);
            if (note != null) {
                Log.d(TAG, "Note loaded: " + note.getTitle() + ", content: " +
                        (note.getContent() != null ? note.getContent().substring(0,
                                Math.min(20, note.getContent().length())) + "..." : "null"));

                noteHeadingEditText.setText(note.getTitle());
                noteDetailsEditText.setText(note.getContent());
                setTitle("Edit: " + note.getTitle());

                // Set the correct category chip
                switch (note.getCategory()) {
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
            } else {
                Log.e(TAG, "Note not found for ID: " + noteId);
                Toast.makeText(this, "Error: Note not found", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity if note not found
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading note: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading note", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity on error
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
            Toast.makeText(this, "Saving the note", Toast.LENGTH_SHORT).show();

            // Save to database
            long result = databaseHelper.saveNote(note);
            if (result > 0) {
                // Update the note's ID if it was a new note
                if (note.getId() <= 0) {
                    note.setId((int) result);
                }
                Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareNote();
            return true;
        }
        if (item.getItemId() == R.id.action_favorite) {
            toggleFavorite();
            return true;
        }
        if (item.getItemId() == R.id.action_upload_drive) {
            uploadNoteToDrive();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFavoriteIcon() {
        if (menu != null && note != null) {
            MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
            if (favoriteItem != null) {
                favoriteItem.setIcon(note.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            }
        }
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

    private void shareNote() {
        String shareText = note.getTitle() + "\n\n" + note.getContent();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_note)));
    }

    private void showUserSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_user_search, null);
        EditText searchInput = view.findViewById(R.id.search_input);
        userList = view.findViewById(R.id.user_list);

        // Set up RecyclerView
        userList.setLayoutManager(new LinearLayoutManager(this));
        userSearchAdapter = new UserSearchAdapter(user -> {
            // Handle user selection
            tagUser(user.getUserId(), user.getEmail());
            // Insert mention without @ since it's already in the text
            int cursorPosition = noteDetailsEditText.getSelectionStart();
            String text = noteDetailsEditText.getText().toString();
            text = text.substring(0, cursorPosition - 1) + user.getEmail() + " " + text.substring(cursorPosition);
            noteDetailsEditText.setText(text);
            noteDetailsEditText.setSelection(cursorPosition - 1 + user.getEmail().length() + 1);
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
                searchUsers(s.toString());
            }
        });

        builder.setView(view);
        userSearchDialog = builder.create();
        userSearchDialog.show();
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            userSearchAdapter.submitList(new ArrayList<>());
            return;
        }

        // Query Firestore for users
        db.collection("users")
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", query + '\uf8ff')
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserTag> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        // Don't show current user in results
                        if (!doc.getId().equals(mAuth.getCurrentUser().getUid())) {
                            UserTag user = new UserTag(
                                    doc.getId(),
                                    doc.getString("email"),
                                    doc.getString("fcmToken"));
                            users.add(user);
                        }
                    }
                    if (userSearchAdapter != null) {
                        userSearchAdapter.submitList(users);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error searching users: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void tagUser(String userId, String email) {
        if (note != null && note.getId() > 0) {
            // Save to local database
            databaseHelper.addTagToNote(note.getId(), userId, email);

            // Save to Firestore for cross-device sync
            SharedNote sharedNote = new SharedNote(
                    String.valueOf(note.getId()),
                    note.getTitle(),
                    note.getContent(),
                    note.getCategory(),
                    mAuth.getCurrentUser().getUid(),
                    mAuth.getCurrentUser().getEmail(),
                    userId,
                    mAuth.getCurrentUser().getEmail()); // Add authorEmail parameter

            db.collection("shared_notes")
                    .add(sharedNote)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "SharedNote added with ID: " + documentReference.getId());
                        // Send notification after successful sharing
                        sendTagNotification(userId, email);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding shared note", e);
                        Toast.makeText(this, "Failed to share note", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void sendTagNotification(String userId, String email) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    String fcmToken = document.getString("fcmToken");
                    if (fcmToken != null) {
                        String currentUserEmail = mAuth.getCurrentUser().getEmail();
                        String title = getString(R.string.tag_notification_title);
                        String message = getString(R.string.tag_notification_message,
                                currentUserEmail, note.getTitle());

                        Log.d(TAG, "Sending notification to user: " + email + " with token: " + fcmToken);
                        NotesFirebaseMessagingService.sendDirectNotification(
                                this, fcmToken, title, message);

                        Toast.makeText(this, "Notification sent to " + email,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "FCM token not found for user: " + email);
                        Toast.makeText(this, "Could not send notification - user token not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user FCM token: " + e.getMessage());
                    Toast.makeText(this, "Error sending notification", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupDriveIntegration() {
        // Check if user is signed in to Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            driveServiceHelper = new DriveServiceHelper(this, account);
        }
    }

    private void uploadNoteToDrive() {
        if (note == null) {
            Toast.makeText(this, R.string.save_note_first, Toast.LENGTH_SHORT).show();
            return;
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // User is not signed in to Google, launch DriveActivity for authentication
            Intent intent = new Intent(this, DriveActivity.class);
            intent.putExtra("noteId", note.getId());
            startActivity(intent);
        } else {
            // User is already signed in, use DriveServiceHelper
            if (driveServiceHelper == null) {
                driveServiceHelper = new DriveServiceHelper(this, account);
            }

            // Show progress dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.uploading_to_drive));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Convert note to text content
            String noteContent = note.getTitle() + "\n\n" + note.getContent();
            String fileName = note.getTitle() + ".txt";

            // Upload to Drive
            driveServiceHelper.createFile(fileName, noteContent)
                    .addOnSuccessListener(fileId -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.note_uploaded_to_drive, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();
                        Log.e(TAG, "Couldn't create file", exception);
                        Toast.makeText(this, R.string.drive_upload_failed, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
