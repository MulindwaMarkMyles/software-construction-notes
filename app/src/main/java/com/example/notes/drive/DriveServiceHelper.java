package com.example.notes.drive;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simplified helper for working with Google Drive REST API
 */
public class DriveServiceHelper {
    private static final String TAG = "DriveServiceHelper";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Drive driveService;
    private final Context context;

    public DriveServiceHelper(Context context, GoogleSignInAccount account) {
        this.context = context;

        Log.d(TAG, "Initializing DriveServiceHelper with account: " + account.getEmail());

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        // Build the Drive service
        driveService = new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Notes App")
                .build();

        Log.d(TAG, "Drive service initialized successfully");
    }

    /**
     * Creates a text file in the user's Drive.
     */
    public Task<String> createFile(String fileName, String content) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                Log.d(TAG, "Creating file: " + fileName);

                // Create file metadata
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                fileMetadata.setMimeType("text/plain");

                // Convert content to bytes
                com.google.api.client.http.ByteArrayContent mediaContent = new com.google.api.client.http.ByteArrayContent(
                        "text/plain", content.getBytes());

                // Create the file using Drive API
                File file = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, name, webViewLink")
                        .execute();

                Log.d(TAG, "File created with ID: " + file.getId() + " and name: " + file.getName());
                Log.d(TAG, "File can be viewed at: " + file.getWebViewLink());

                mainHandler.post(() -> taskCompletionSource.setResult(file.getId()));
            } catch (Exception e) {
                Log.e(TAG, "Error creating file", e);
                mainHandler.post(() -> taskCompletionSource.setException(e));
            }
        });

        return taskCompletionSource.getTask();
    }

    /**
     * Lists files in the user's Drive.
     */
    public Task<FileList> queryFiles() {
        TaskCompletionSource<FileList> taskCompletionSource = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                Log.d(TAG, "Querying files from Drive");

                FileList fileList = driveService.files().list()
                        .setSpaces("drive")
                        .setFields("files(id, name, mimeType, modifiedTime, webViewLink)")
                        .setPageSize(50) // Limit to 50 files
                        .execute();

                Log.d(TAG, "Found " + fileList.getFiles().size() + " files");

                mainHandler.post(() -> taskCompletionSource.setResult(fileList));
            } catch (Exception e) {
                Log.e(TAG, "Error querying files", e);
                mainHandler.post(() -> taskCompletionSource.setException(e));
            }
        });

        return taskCompletionSource.getTask();
    }

    /**
     * Downloads a file's content.
     */
    public Task<String> readFile(String fileId) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                String content = new String(outputStream.toByteArray());

                mainHandler.post(() -> taskCompletionSource.setResult(content));
            } catch (Exception e) {
                Log.e(TAG, "Error reading file", e);
                mainHandler.post(() -> taskCompletionSource.setException(e));
            }
        });

        return taskCompletionSource.getTask();
    }

    /**
     * Updates a file's content.
     */
    public Task<Void> updateFile(String fileId, String content) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                // Convert content to bytes
                com.google.api.client.http.ByteArrayContent mediaContent = new com.google.api.client.http.ByteArrayContent(
                        "text/plain", content.getBytes());

                driveService.files().update(fileId, null, mediaContent).execute();

                mainHandler.post(() -> taskCompletionSource.setResult(null));
            } catch (Exception e) {
                Log.e(TAG, "Error updating file", e);
                mainHandler.post(() -> taskCompletionSource.setException(e));
            }
        });

        return taskCompletionSource.getTask();
    }
}
