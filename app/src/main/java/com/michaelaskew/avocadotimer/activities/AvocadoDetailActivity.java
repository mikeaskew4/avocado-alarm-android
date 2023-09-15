package com.michaelaskew.avocadotimer.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.michaelaskew.avocadotimer.R;
import com.avocadotimer.models.Avocado;

public class AvocadoDetailActivity extends AppCompatActivity {

    private ImageView imgAvocado;
    private EditText edtAvocadoName;
    private TextView tvCreationTime;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avocado_detail);

        imgAvocado = findViewById(R.id.imgAvocado);
        edtAvocadoName = findViewById(R.id.edtAvocadoName);
        tvCreationTime = findViewById(R.id.tvCreationTime);
        tvTimer = findViewById(R.id.tvTimer);

        // Assuming you're passing the Avocado object via intent (this is just an example)
        Avocado avocado = (Avocado) getIntent().getSerializableExtra("avocado");
        if (avocado != null) {
            // TODO: Load image into imgAvocado using Glide or Picasso
            edtAvocadoName.setText(avocado.getName());
            tvCreationTime.setText(getString(R.string.creation_time) + avocado.getCreationTime());
            // TODO: Calculate and display time remaining until avocado is ready
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Update the timer if needed when the user returns to this screen
    }

    private void setAvocadoReadyAlarm(long triggerTimeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AvocadoAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
        }
    }

    private void cancelAvocadoReadyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AvocadoAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }


}
