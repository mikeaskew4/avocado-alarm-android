package com.michaelaskew.avocadotimer.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.models.Avocado;

import java.util.ArrayList;
import java.util.List;

public class AvocadoListActivity extends AppCompatActivity {
    private RecyclerView rvAvocadoList;
    private List<Avocado> avocadoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avocado_list);

        rvAvocadoList = findViewById(R.id.rvAvocadoList); // Initialize RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvAvocadoList.setLayoutManager(layoutManager);

        loadAvocados(); // Move the code for loading avocado data here

        // ... Rest of your RecyclerView-related code
    }

    private void loadAvocados() {
        // Move the code for loading avocado data here
        // DatabaseHelper dbHelper = new DatabaseHelper(this);
        // avocadoList = dbHelper.getAllAvocados();
        // ...
    }

    // ... Other methods related to the RecyclerView
}