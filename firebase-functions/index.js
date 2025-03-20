const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        const notification = snap.data();
        
        const message = {
            token: notification.to,
            data: notification.data,
            android: {
                priority: 'high',
                notification: {
                    title: notification.data.title,
                    body: notification.data.message,
                    clickAction: 'OPEN_NOTE',
                    icon: '@drawable/ic_notification'
                }
            }
        };

        try {
            await admin.messaging().send(message);
            // Delete the notification document after sending
            await snap.ref.delete();
            return null;
        } catch (error) {
            console.error('Error sending notification:', error);
            return null;
        }
    });
