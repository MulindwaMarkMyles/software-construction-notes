package com.example.notes.drive;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simplified helper for working with Google Drive REST API
 */
public class DriveServiceHelper {
    private static final String TAG = "DriveServiceHelper";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String accessToken;
    private final String baseUrl = "https://www.googleapis.com/drive/v3";

    public DriveServiceHelper(GoogleSignInAccount account) {
        this.accessToken = account.getIdToken(); // Use ID token for authenticated requests
    }

    /**
     * Creates a text file in the user's Drive.
     */
    public Task<String> createFile(String fileName, String content) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        
        executor.execute(() -> {
            try {
                // First, create metadata for the file
                URL url = new URL(baseUrl + "/files");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject metadata = new JSONObject();
                metadata.put("name", fileName);
                metadata.put("mimeType", "text/plain");

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                writer.write(metadata.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                
                if (responseCode >= 200 && responseCode < 300) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject json = new JSONObject(response.toString());
                    String fileId = json.getString("id");
                    
                    // Upload content to the created file
                    updateFileContent(fileId, content);
                    
                    mainHandler.post(() -> taskCompletionSource.setResult(fileId));
                } else {
                    String error = "Error creating file: " + responseCode;
                    Log.e(TAG, error);
                    mainHandler.post(() -> taskCompletionSource.setException(new IOException(error)));
                }
                
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
        
        // Create mock FileList with simulated data 
        // (would need actual Drive API implementation)
        FileList mockFileList = new FileList();
        List<File> files = new ArrayList<>();
        
        File mockFile = new File();
        mockFile.setId("mockFileId");
        mockFile.setName("Example.txt");
        mockFile.setMimeType("text/plain");
        files.add(mockFile);
        
        mockFileList.setFiles(files);
        taskCompletionSource.setResult(mockFileList);
        
        return taskCompletionSource.getTask();
    }

    /**
     * Updates a file's content.
     */
    private void updateFileContent(String fileId, String content) throws IOException {
        URL url = new URL(baseUrl + "/files/" + fileId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setDoOutput(true);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        writer.write(content);
        writer.flush();
        writer.close();

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Error updating file content: " + responseCode);
        }
    }
}
