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

        // Generate unique notification ID to prevent override
        int notificationId = new Random().nextInt(1000000);

        // Handle both data messages and notification messages
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String message = data.get("message");
            showNotification(title, message, notificationId);
            Log.d(TAG, "Showing notification with ID: " + notificationId);
        }

        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            showNotification(notification.getTitle(), notification.getBody(), notificationId);
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

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        try {
            RemoteMessage.Builder builder = new RemoteMessage.Builder(targetToken)
                    .setMessageId(String.valueOf(System.currentTimeMillis()))
                    .setTtl(3600); // 1 hour expiry

            for (Map.Entry<String, String> entry : data.entrySet()) {
                builder.addData(entry.getKey(), entry.getValue());
            }

            FirebaseMessaging.getInstance().send(builder.build());
            Log.d(TAG, "Notification sent successfully to token: " + targetToken);
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showNotification(String title, String message, int notificationId) {
        try {
            createNotificationChannel();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    notificationId, // Use unique request code
                    intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(notificationId, builder.build());
                Log.d(TAG, "Notification displayed successfully");
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(getString(R.string.channel_description));
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "NotificationChannel created successfully");
            }
        }
    }
}
