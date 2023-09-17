package com.michaelaskew.avocadotimer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.adapters.AvocadoAdapter;
import com.michaelaskew.avocadotimer.database.DatabaseHelper;
import com.michaelaskew.avocadotimer.models.Avocado;
import com.michaelaskew.avocadotimer.utilities.ImageCaptureManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements ImageCaptureManager.ImageCaptureListener,  EasyPermissions.PermissionCallbacks  {

    ImageCaptureManager imageCaptureManager;

    private static final int RC_PERMISSIONS = 123;
    private static final int RC_CAMERA_PERM = 124;  // Specifically for requesting camera permission in `avocadoUploadImageClick` method

    // Define a constant for SharedPreferences
    private static final String PREFS_NAME = "AppPrefs";
    private static final String DENIED_COUNTER = "CameraPermissionDeniedCounter";

    private static final String KEY_LAUNCH_COUNT = "launch_count";

    private Button btnCaptureAvocado;
    private RecyclerView rvAvocadoList;
    private List<Avocado> avocadoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageCaptureManager = new ImageCaptureManager(this);
        imageCaptureManager.setListener(this);

        btnCaptureAvocado = findViewById(R.id.btnCaptureAvocado);
        rvAvocadoList = findViewById(R.id.rvAvocadoList);
        btnCaptureAvocado.setOnClickListener(this::avocadoUploadImageClick);

        // TODO: Set up RecyclerView with an adapter
        RecyclerView rvAvocadoList = findViewById(R.id.rvAvocadoList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvAvocadoList.setLayoutManager(layoutManager);

        loadAvocados();
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
        loadAvocados();  // Assuming this method loads avocados from the database and updates the RecyclerView
    }

    private void loadAvocados() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Avocado> avocados = dbHelper.getAllAvocados(); // method to retrieve all avocados from the database
        AvocadoAdapter adapter = new AvocadoAdapter(this, avocados);

        rvAvocadoList.setAdapter(adapter);
        adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
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
        // TODO: Implement choose from gallery functionality
    }


}
