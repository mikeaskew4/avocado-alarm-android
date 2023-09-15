package com.michaelaskew.avocadotimer.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AvocadoAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Notify the user
        Toast.makeText(context, "Your avocado is ready!", Toast.LENGTH_LONG).show();
        // TODO: You might want to show a notification or update the UI here.
    }
}
