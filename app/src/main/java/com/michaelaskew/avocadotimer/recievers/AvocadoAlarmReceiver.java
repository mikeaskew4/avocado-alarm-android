package com.michaelaskew.avocadotimer.recievers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.activities.MainActivity;

public class AvocadoAlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "AVOCADO_CHANNEL_ID";
    private static final String CHANNEL_NAME = "Avocado Notification Channel";
    private static final int NOTIFICATION_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        String aName = intent.getStringExtra("a_name");

        Log.d("AvocadoAlarmReceiver", "message queued for: " + aName );
        createNotificationChannel(context);
        sendNotification(context, aName);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Avocado Timer Notifications");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(Context context, String avocadoName) {
        Intent notificationIntent = new Intent(context, MainActivity.class); // Replace YourMainActivity with your app's main activity
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Ensures that the main activity is not recreated

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.at_logo_v2_small_rev)
                .setContentTitle("Your Avocado " + avocadoName + " is ready!")
                .setContentText("Open up Avocado Timer to learn more")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // Set the pending intent to open the app

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // Notify the user
//        Toast.makeText(context, "Your avocado is ready!", Toast.LENGTH_LONG).show();
//        // TODO: You might want to show a notification or update the UI here.
//    }
}
