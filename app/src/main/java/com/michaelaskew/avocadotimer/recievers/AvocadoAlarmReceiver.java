package com.michaelaskew.avocadotimer.recievers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

import com.michaelaskew.avocadotimer.R;

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.at_logo_smalllogo)
                .setContentTitle("Your Avocado " + avocadoName + " is ready!")
                .setContentText("Open up Avocado Timer to learn more")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

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
