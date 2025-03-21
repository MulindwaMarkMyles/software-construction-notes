package com.example.notes.drive;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
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

public class DriveServiceHelper {
    private static final String TAG = "DriveServiceHelper";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Drive driveService;

    public DriveServiceHelper(Context context, GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Notes App")
                .build();
    }

    /**
     * Creates a text file in the user's Drive.
     */
    public Task<String> createFile(String fileName, String content) {
        return Tasks.call(executor, () -> {
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setMimeType("text/plain");

            // Convert content to bytes
            java.io.ByteArrayContent mediaContent = new java.io.ByteArrayContent("text/plain", content.getBytes());

            // Create file in Google Drive
            File file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            if (file == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            Log.d(TAG, "Created file with ID: " + file.getId());
            return file.getId();
        });
    }

    /**
     * Lists files in the user's Drive.
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(executor, () -> driveService.files().list().setSpaces("drive")
                .setFields("files(id, name, mimeType, modifiedTime)")
                .execute());
    }

    /**
     * Downloads a file's content.
     */
    public Task<String> readFile(String fileId) {
        return Tasks.call(executor, () -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return new String(outputStream.toByteArray());
        });
    }

    /**
     * Updates a file's content.
     */
    public Task<Void> updateFile(String fileId, String content) {
        return Tasks.call(executor, () -> {
            // Convert content to bytes
            java.io.ByteArrayContent mediaContent = new java.io.ByteArrayContent("text/plain", content.getBytes());

            driveService.files().update(fileId, null, mediaContent).execute();
            return null;
        });
    }
}
