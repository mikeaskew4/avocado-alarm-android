package com.michaelaskew.avocadotimer.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.adapters.AvocadoAdapter;
import com.michaelaskew.avocadotimer.database.DatabaseHelper;
import com.michaelaskew.avocadotimer.models.Avocado;
import com.michaelaskew.avocadotimer.utilities.ImageCaptureManager;
import com.michaelaskew.avocadotimer.utilities.UpdateHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements ImageCaptureManager.ImageCaptureListener,  EasyPermissions.PermissionCallbacks  {

    private UpdateHelper mUpdateHelper;

    ImageCaptureManager imageCaptureManager;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final int RC_PERMISSIONS = 123;
    private static final int RC_CAMERA_PERM = 124;  // Specifically for requesting camera permission in `avocadoUploadImageClick` method

    // Define a constant for SharedPreferences
    private static final String PREFS_NAME = "AppPrefs";
    private static final String DENIED_COUNTER = "CameraPermissionDeniedCounter";

    private static final String KEY_LAUNCH_COUNT = "launch_count";

    private Button btnCaptureAvocado;
    private RecyclerView rvAvocadoList;
    private TextView mainHeadline;
    private TextView subhead;
    private List<Avocado> avocadoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the app is launched for the first time
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean firstTime = prefs.getBoolean("firstTime", true);

        if (firstTime) {
            // If it's the first time, request permissions
            requestNeededPermissions();

            // Mark firstTime as false
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
        }

        mUpdateHelper = new UpdateHelper(this);
        mUpdateHelper.checkForUpdate();

        imageCaptureManager = new ImageCaptureManager(this);
        imageCaptureManager.setListener(this);

        mainHeadline = findViewById(R.id.headline);
        subhead = findViewById(R.id.subhead);
        btnCaptureAvocado = findViewById(R.id.btnCaptureAvocado);
        rvAvocadoList = findViewById(R.id.rvAvocadoList);
        btnCaptureAvocado.setOnClickListener(this::avocadoUploadImageClick);


        // TODO: Set up RecyclerView with an adapter
        rvAvocadoList = findViewById(R.id.rvAvocadoList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvAvocadoList.setLayoutManager(layoutManager);

        loadAvocados();

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

                    DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                    if (dbHelper.deleteAvocado(avocadoToDelete.getId())) {
                        avocadoList.remove(position);

                        // Check if the list is empty after removing the item
                        if (avocadoList.isEmpty()) {
                            rvAvocadoList.getAdapter().notifyDataSetChanged();
                        } else {
                            rvAvocadoList.getAdapter().notifyItemRemoved(position);
                        }

                        Toast.makeText(MainActivity.this, "Avocado deleted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error deleting Avocado.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvAvocadoList);
    }

    private void loadAvocados() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        avocadoList = dbHelper.getAllAvocados(); // method to retrieve all avocados from the database
        if(avocadoList.size() == 0) {
            mainHeadline.setText("Add Some Avocados and Get Something to Look Forward To!");
        } else if (avocadoList.size() > 0){
            mainHeadline.setText("There's an avocado (or two) in your future..."); // Or whatever default text you want to display
        }

        // Parse the date strings and check if any are older than 5 days
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // assuming your timestamp is in this format
        Calendar fiveDaysAgo = Calendar.getInstance();
        fiveDaysAgo.add(Calendar.DAY_OF_YEAR, -5);  // get the date for 5 days ago

        boolean hasOldAvocados = false;
        for (Avocado avocado : avocadoList) {
            try {
                Date avocadoDate = sdf.parse(avocado.getCreationTime());
                if (avocadoDate.before(fiveDaysAgo.getTime())) {
                    hasOldAvocados = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (hasOldAvocados) {
            mainHeadline.setText("Looks Like Your Avocado List Needs Some Grooming");
            subhead.setText("Swipe on the list to remove an avocado you (obviosuly already ate");

        }

        AvocadoAdapter adapter = new AvocadoAdapter(this, avocadoList);

        rvAvocadoList.setAdapter(adapter);
        adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    "android.permission.POST_NOTIFICATIONS"  // This permission is hypothetical for the purpose of this answer
            };

            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @AfterPermissionGranted(RC_PERMISSIONS)
    private void requestPermissions() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                // Manifest.permission.POST_NOTIFICATIONS
        };  // Add the permissions you need
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "We need these permissions for the app to function properly.",
                    RC_PERMISSIONS, perms);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (requestCode == RC_CAMERA_PERM) {
            if (EasyPermissions.somePermissionDenied(this, permissions)) {
                onCameraPermissionDenied();
            }
        }
    }

    // Optional: if you want a callback after permissions are granted or denied
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been granted
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been denied
        // If some permissions are permanently denied then inform the user
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void onCameraPermissionDenied() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int deniedCount = prefs.getInt(DENIED_COUNTER, 0);
        prefs.edit().putInt(DENIED_COUNTER, deniedCount + 1).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the list is empty before resuming
        if (avocadoList.isEmpty()) {
            rvAvocadoList.getAdapter().notifyDataSetChanged();
        }
        loadAvocados();
        mUpdateHelper.onResumeUpdate();
    }

    @Override
    public void onImageCaptured(Uri imageUri) {
        // Handle the captured image here.
        // For example, display it in an ImageView:
        // ImageView imageView = findViewById(R.id.your_imageview_id);
        // imageView.setImageURI(imageUri);

        // Create an Intent to start the AvocadoDetailActivity
        Intent intent = new Intent(MainActivity.this, AvocadoDetailActivity.class);

        // Pass the captured image URI to the AvocadoDetailActivity
        intent.putExtra("capturedImageUri", imageUri.toString());

        // Start the activity
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageCaptureManager.onActivityResult(requestCode, resultCode, data);

//        if (!mUpdateHelper.onActivityResult(requestCode, resultCode)) {
//            // Handle other activity results if necessary
//        }
    }

    public void avocadoUploadImageClick(View v) {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a new Avocado");
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Take Photo")) {
                checkCameraPermissionAndCapture();
            } else if (options[which].equals("Choose from Gallery")) {
                chooseFromGallery();
            } else if (options[which].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    private void checkCameraPermissionAndCapture() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int deniedCount = prefs.getInt(DENIED_COUNTER, 0);

        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
//            imageCaptureManager.captureImage();
            Intent cameraIntent = new Intent(this, CameraActivity.class);
            startActivity(cameraIntent);
        } else {
            if (deniedCount > 0 && EasyPermissions.somePermissionPermanentlyDenied(this, Arrays.asList(perms))) {
                // If permissions are permanently denied and have been denied at least once before
                new AppSettingsDialog.Builder(this)
                        .setTitle("Required Permissions")
                        .setRationale("Camera permission is required for this feature. Please enable it in settings.")
                        .setPositiveButton("Open Settings")
                        .setNegativeButton("Cancel")
                        .setRequestCode(RC_CAMERA_PERM)
                        .build()
                        .show();
            } else {
                // Request permissions
                EasyPermissions.requestPermissions(this, "We need camera permission to take photos.",
                        RC_CAMERA_PERM, perms);
            }
        }
    }

    private void chooseFromGallery() {
        // @@TODO: Implement choose from gallery functionality
        // Create an AlertDialog.Builder
        AlertDialog.Builder comingSoonAlert = new AlertDialog.Builder(this);
        comingSoonAlert.setTitle("Feature coming soon...");
        comingSoonAlert.setMessage("We're still working on it!");
        comingSoonAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        comingSoonAlert.show();
    }

    @Override
    public void onBackPressed() {
        // Display a confirmation dialog
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the app
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing (user canceled)
                    }
                })
                .show();
    }
}
