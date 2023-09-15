package com.michaelaskew.avocadotimer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.avocadotimer.R;
import com.avocadotimer.models.Avocado;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnCaptureAvocado;
    private RecyclerView rvAvocadoList;
    private List<Avocado> avocadoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCaptureAvocado = findViewById(R.id.btnCaptureAvocado);
        rvAvocadoList = findViewById(R.id.rvAvocadoList);

        // TODO: Set up RecyclerView with an adapter

        btnCaptureAvocado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Open camera or gallery intent
            }
        });
    }
}
