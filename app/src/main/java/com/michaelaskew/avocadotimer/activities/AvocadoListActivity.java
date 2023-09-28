package com.michaelaskew.avocadotimer.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.database.DatabaseHelper;
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

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION && position < avocadoList.size()) {

                    Avocado avocadoToDelete = avocadoList.get(position);

                    DatabaseHelper dbHelper = new DatabaseHelper(AvocadoListActivity.this);
                    if (dbHelper.deleteAvocado(avocadoToDelete.getId())) {
                        avocadoList.remove(position);

                        // Check if the list is empty after removing the item
                        if (avocadoList.isEmpty()) {
                            rvAvocadoList.getAdapter().notifyDataSetChanged();
                        } else {
                            rvAvocadoList.getAdapter().notifyItemRemoved(position);
                        }

                        Toast.makeText(AvocadoListActivity.this, "Avocado deleted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AvocadoListActivity.this, "Error deleting Avocado.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvAvocadoList);
    }

    private void loadAvocados() {
        // Move the code for loading avocado data here
        // DatabaseHelper dbHelper = new DatabaseHelper(this);
        // avocadoList = dbHelper.getAllAvocados();
        // ...
    }

    // ... Other methods related to the RecyclerView
}