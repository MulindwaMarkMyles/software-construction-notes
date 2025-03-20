package com.example.notes.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.notes.MainActivity;
import com.example.notes.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NotesFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "notes_notifications";
    private static final String TAG = "NotesFirebaseMsgService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Received message from: " + remoteMessage.getFrom());
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            showNotification(title, message);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed FCM token: " + token);
        super.onNewToken(token);
        // Store new token in Firestore for the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .update("fcmToken", token);
        }
    }

    public static void sendDirectNotification(Context context, String targetToken, String title, String message) {
        Log.d(TAG, "Attempting to send notification to token: " + targetToken);
        Log.d(TAG, "Notification content - Title: " + title + ", Message: " + message);

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        data.put("priority", "high");

        FirebaseMessaging.getInstance()
                .subscribeToTopic("notifications")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully subscribed to notifications topic");
                        // Now send the message
                        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(targetToken)
                                .setMessageId(Integer.toString(new Random().nextInt(1000)))
                                .setData(data)
                                .build());
                        Log.d(TAG, "Notification sent successfully");
                    } else {
                        Log.e(TAG, "Failed to subscribe to notifications topic", task.getException());
                    }
                });
    }

    private void showNotification(String title, String message) {
        try {
            Log.d(TAG, "Showing notification - Title: " + title + ", Message: " + message);
            
            createNotificationChannel();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            // Use unique request code based on time
            int requestCode = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                // Use unique ID for each notification
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "Notification shown successfully with ID: " + notificationId);
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                );
                
                channel.setDescription(getString(R.string.channel_description));
                channel.enableLights(true);
                channel.enableVibration(true);
                
                NotificationManager notificationManager = 
                    getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "NotificationChannel created successfully");
                } else {
                    Log.e(TAG, "NotificationManager is null while creating channel");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }
}
