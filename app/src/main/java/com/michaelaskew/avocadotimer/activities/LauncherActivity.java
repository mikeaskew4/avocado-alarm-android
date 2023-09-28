package com.michaelaskew.avocadotimer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.michaelaskew.avocadotimer.BuildConfig;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int currentVersionCode = BuildConfig.VERSION_CODE;

        SharedPreferences prefs = getSharedPreferences("app_preferences", MODE_PRIVATE);
        int lastVersionCode = prefs.getInt("last_version_code", -1);

        if (currentVersionCode != lastVersionCode) {
            // Either a first install or an update
            startActivity(new Intent(this, OnboardingScreen.class));
            finish();
        } else {
            // Launch the main activity as usual
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        SharedPreferences.Editor editor = prefs.edit();

        // @@TODO - use actual version
        editor.putInt("last_version_code", currentVersionCode);
//        editor.putInt("last_version_code", 0);
        editor.apply();
    }
}