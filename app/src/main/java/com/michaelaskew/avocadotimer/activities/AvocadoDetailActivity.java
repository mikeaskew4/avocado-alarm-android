package com.michaelaskew.avocadotimer.activities;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import com.github.javafaker.Faker;
import com.michaelaskew.avocadotimer.BuildConfig;
import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.database.DatabaseHelper;
import com.michaelaskew.avocadotimer.models.Avocado;
import com.michaelaskew.avocadotimer.recievers.AvocadoAlarmReceiver;
import com.michaelaskew.avocadotimer.utilities.TimeUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AvocadoDetailActivity extends AppCompatActivity {
    private static final int RC_NOTIFICATIONS_PERM = 112;

    private LinearLayout avocadoDetailLayout;
    private ImageView imgAvocado;
    private EditText edtAvocadoName;
    private TextView tvCreationTime;
    private TextView tvTimer;

    private Button btnSave;
    private DatabaseHelper db;

    private Button deleteButton;
    private Avocado avocado;

    private int readyIn = 3600000;

    int selectedAvocadoId = -1;

    private Faker faker = new Faker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avocado_detail);

        avocadoDetailLayout = findViewById(R.id.avocadoDetailLayout);
        imgAvocado = findViewById(R.id.imgAvocado);
        edtAvocadoName = findViewById(R.id.edtAvocadoName);
        tvCreationTime = findViewById(R.id.tvCreationTime);
        tvTimer = findViewById(R.id.tvTimer);

        deleteButton = findViewById(R.id.deleteButton);

        selectedAvocadoId = getIntent().getIntExtra("avocado_id", -1);
        if (selectedAvocadoId > 0) {
            DatabaseHelper db = new DatabaseHelper(this);
            avocado = db.getAvocado(selectedAvocadoId);
            Uri capturedImageUri = Uri.parse(avocado.getImagePath());
            imgAvocado.setImageURI(capturedImageUri);
            edtAvocadoName.setText(avocado.getName());

            // Formatting creation time for display
            String creationTime = avocado.getCreationTime();
            tvCreationTime.setText(getString(R.string.creation_time) + TimeUtils.getRelativeTimeText(creationTime));

            Object[] results = TimeUtils.getTimeRemaining(creationTime, readyIn, avocado.getSquishiness());
            String formattedTime = (String) results[0];
            double fractionElapsed = (double) results[1];

//            tvTimer.setText(getString(R.string.timer_value) + formattedTime);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCurrentAvocado();
                }
            });

        } else {
            String imageUriString = getIntent().getStringExtra("capturedImageUri");
            int capturedSquishinessValue = getIntent().getIntExtra("capturedSquishinessValue", 0);
            Uri capturedImageUri = Uri.parse(imageUriString);
            String firstName = faker.name().firstName();

            edtAvocadoName.setText(firstName);

            imgAvocado.setImageURI(capturedImageUri);
//            tvCreationTime.setText(LocalDateTime.now().toString());
            // Assuming you're passing the Avocado object via intent (this is just an example)
            avocado = (Avocado) getIntent().getSerializableExtra("avocado");

            if (avocado != null) {
                avocado.setImagePath(capturedImageUri.toString());
                // If creationTime hasn't been set yet, set it to the current time
                if (avocado.getCreationTime() == null) {
                    avocado.setCreationTime(LocalDateTime.now().toString());
                }
                avocado.setSquishiness(capturedSquishinessValue);
                // TODO: Load image into imgAvocado using Glide or Picasso

                // TODO: Calculate and display time remaining until avocado is ready
            }

            deleteButton.setVisibility(View.GONE);
        }
        btnSave = findViewById(R.id.btnSave);

        // Initialize your database helper
        db = new DatabaseHelper(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNotificationsPermissionAndSaveAvocado();
            }
        });

        avocadoDetailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the keyboard is currently open
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    View currentFocus = getCurrentFocus();
                    if (currentFocus != null) {
                        // Hide the keyboard
                        inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    }
                }
            }
        });
    }

    private void saveAvocado() {
        DatabaseHelper db = new DatabaseHelper(AvocadoDetailActivity.this);
        String imageUriString = getIntent().getStringExtra("capturedImageUri");
        int squishinessValue = getIntent().getIntExtra("capturedSquishinessValue", 0);

        if (imageUriString != null) { // Assuming Avocado's ID is an Integer. Check if it's null (or 0 if it's int).
            // New avocado, insert it
            // Create a new Avocado object with data from your views (EditText, ImageView, etc.)
            Avocado avocado = new Avocado();
            avocado.setName(edtAvocadoName.getText().toString());
            avocado.setImagePath(imageUriString);
            avocado.setSquishiness(squishinessValue);
            if (avocado.getCreationTime() == null) {
//                avocado.setCreationTime(new String[]{LocalDateTime.now()});
            }
            // ... set other fields ...

//            avocado.setCreationTime(LocalDateTime.now().toString());
            long newId = db.insertAvocado(avocado);
            avocado.setId((int) newId); // Set the newly generated ID to your avocado object
            Toast.makeText(AvocadoDetailActivity.this, "Avocado added!", Toast.LENGTH_SHORT).show();
            // @@TODO - temp timer
            int delayInMills = (((readyIn - (squishinessValue * 600000 )) + 100000) * 100) - readyIn;
            Log.d("Detail",  String.valueOf(db.getAvocado((int) newId)));
            setAvocadoReadyAlarm(delayInMills, db.getAvocado((int) newId));
        } else {
            // Existing avocado, update it
            Avocado selectedAvocado = db.getAvocado(selectedAvocadoId);
            selectedAvocado.setName(edtAvocadoName.getText().toString());
            // ... set other fields ...

            db.updateAvocado(selectedAvocado);
            Toast.makeText(AvocadoDetailActivity.this, "Avocado updated!", Toast.LENGTH_SHORT).show();
        }

        // Optional: You can finish the activity if you want to go back to the main screen after saving
         finish();
    }

    private void saveAvocado(boolean notificationsEnabled) {
        DatabaseHelper db = new DatabaseHelper(AvocadoDetailActivity.this);
        String imageUriString = getIntent().getStringExtra("capturedImageUri");
        int squishinessValue = getIntent().getIntExtra("capturedSquishinessValue", 0);

        if (imageUriString != null) { // Assuming Avocado's ID is an Integer. Check if it's null (or 0 if it's int).
            // New avocado, insert it
            // Create a new Avocado object with data from your views (EditText, ImageView, etc.)
            Avocado avocado = new Avocado();
            avocado.setName(edtAvocadoName.getText().toString());
            avocado.setImagePath(imageUriString);
            avocado.setSquishiness(squishinessValue);
            if (avocado.getCreationTime() == null) {
//                avocado.setCreationTime(new String[]{LocalDateTime.now()});
            }
            // ... set other fields ...

//            avocado.setCreationTime(LocalDateTime.now().toString());
            long newId = db.insertAvocado(avocado);
            avocado.setId((int) newId); // Set the newly generated ID to your avocado object
            Toast.makeText(AvocadoDetailActivity.this, "Avocado added!", Toast.LENGTH_SHORT).show();
        } else {
            // Existing avocado, update it
            Avocado selectedAvocado = db.getAvocado(selectedAvocadoId);
            selectedAvocado.setName(edtAvocadoName.getText().toString());
            // ... set other fields ...

            db.updateAvocado(selectedAvocado);
            Toast.makeText(AvocadoDetailActivity.this, "Avocado updated!", Toast.LENGTH_SHORT).show();
        }

        // Optional: You can finish the activity if you want to go back to the main screen after saving
        finish();
    }

    private void deleteCurrentAvocado() {
        if (avocado != null && db.deleteAvocado(avocado.getId())) {
            Toast.makeText(this, "Avocado deleted!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error deleting Avocado.", Toast.LENGTH_SHORT).show();
        }
    }

    @AfterPermissionGranted(RC_NOTIFICATIONS_PERM)
    private void checkNotificationsPermissionAndSaveAvocado() {
        String[] perms = {Manifest.permission.POST_NOTIFICATIONS};
        if (EasyPermissions.hasPermissions(this, perms)) {
            saveAvocado();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "We need notifications permission to let you know the timer is doner.",
                    RC_NOTIFICATIONS_PERM, perms);
        }
        saveAvocado(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Update the timer if needed when the user returns to this screen
    }

    private int generateUniqueRequestCode() {
        return (int) System.currentTimeMillis();
    }

    private void setAvocadoReadyAlarm(long delayInMillis, Avocado avocado) {
        long triggerTimeInMillis = System.currentTimeMillis() + (BuildConfig.IS_APP_STORE ? delayInMillis : 3600);
        long days = TimeUnit.MILLISECONDS.toHours(delayInMillis);

        Intent intent = new Intent(this, AvocadoAlarmReceiver.class);
        intent.putExtra("a_name", avocado.getName()); // Add custom title

        int requestCode = generateUniqueRequestCode(); // Generate a unique request code
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Log.d("AvocadoDetailActivity", "Alarm is set, " + intent.getStringExtra("a_name") + " will see you in: ~" + String.valueOf(days) + ".5 hours");

        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            }
        }
    }

    private void cancelAvocadoReadyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AvocadoAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }


}
