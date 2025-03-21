package com.example.notes.drive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.DatabaseHelper;
import com.example.notes.Note;
import com.example.notes.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.List;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = "DriveActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;

    private GoogleSignInClient signInClient;
    private DriveServiceHelper driveServiceHelper;
    private ProgressDialog progressDialog;

    private TextView statusText;
    private Button signInButton;
    private Button signOutButton;
    private Button uploadButton;
    private RecyclerView driveFilesList;
    private LinearLayout signedInLayout;
    private LinearLayout signedOutLayout;

    private int noteId = -1;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        statusText = findViewById(R.id.status_text);
        signInButton = findViewById(R.id.sign_in_button);
        signOutButton = findViewById(R.id.sign_out_button);
        uploadButton = findViewById(R.id.upload_button);
        driveFilesList = findViewById(R.id.drive_files_list);
        signedInLayout = findViewById(R.id.signed_in_layout);
        signedOutLayout = findViewById(R.id.signed_out_layout);

        // Set up RecyclerView
        driveFilesList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Check if we're handling a specific note
        noteId = getIntent().getIntExtra("noteId", -1);

        // Sign in button click listener
        signInButton.setOnClickListener(v -> signIn());

        // Sign out button click listener
        signOutButton.setOnClickListener(v -> signOut());

        // Upload button click listener
        uploadButton.setOnClickListener(v -> {
            if (noteId != -1) {
                uploadNoteToDrive(noteId);
            } else {
                Toast.makeText(this, R.string.no_note_selected, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Google Sign In
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);

        // Check the current sign in status
        checkSignInStatus();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkSignInStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // User is signed in
            updateUI(true);
            driveServiceHelper = new DriveServiceHelper(this, account);
            loadDriveFiles();

            if (noteId != -1) {
                uploadButton.setVisibility(View.VISIBLE);
            } else {
                uploadButton.setVisibility(View.GONE);
            }
        } else {
            // User is not signed in
            updateUI(false);
            driveServiceHelper = null;
        }
    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            statusText.setText(getString(R.string.signed_in_as, account.getEmail()));
            signedInLayout.setVisibility(View.VISIBLE);
            signedOutLayout.setVisibility(View.GONE);
        } else {
            statusText.setText(R.string.signed_out);
            signedInLayout.setVisibility(View.GONE);
            signedOutLayout.setVisibility(View.VISIBLE);
        }
    }

    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    private void signOut() {
        signInClient.signOut().addOnCompleteListener(this, task -> {
            updateUI(false);
            driveServiceHelper = null;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            driveServiceHelper = new DriveServiceHelper(this, account);
            updateUI(true);
            loadDriveFiles();

            // If a note was passed, show upload button
            if (noteId != -1) {
                uploadButton.setVisibility(View.VISIBLE);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(false);
            Toast.makeText(this, R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDriveFiles() {
        if (driveServiceHelper == null) {
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_drive_files));
        progressDialog.show();

        driveServiceHelper.queryFiles()
                .addOnSuccessListener(fileList -> {
                    progressDialog.dismiss();
                    // Display files in RecyclerView
                    DriveFilesAdapter adapter = new DriveFilesAdapter(this, fileList.getFiles());
                    driveFilesList.setAdapter(adapter);
                })
                .addOnFailureListener(exception -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error loading files", exception);
                    Toast.makeText(this, R.string.error_loading_files, Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadNoteToDrive(int noteId) {
        Note note = databaseHelper.getNoteById(noteId);

        if (note == null) {
            Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        if (driveServiceHelper == null) {
            Toast.makeText(this, R.string.sign_in_required, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading_to_drive));
        progressDialog.show();

        // Convert note to text content
        String noteContent = note.getTitle() + "\n\n" + note.getContent();
        String fileName = note.getTitle() + ".txt";

        // Upload to Drive
        driveServiceHelper.createFile(fileName, noteContent)
                .addOnSuccessListener(fileId -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, R.string.note_uploaded_to_drive, Toast.LENGTH_SHORT).show();
                    loadDriveFiles(); // Refresh file list
                })
                .addOnFailureListener(exception -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Couldn't create file", exception);
                    Toast.makeText(this, R.string.drive_upload_failed, Toast.LENGTH_SHORT).show();
                });
    }
}
