package com.michaelaskew.avocadotimer.activities;

import android.Manifest;
import android.content.Intent;
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
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageCaptureManager.ImageCaptureListener {

    ImageCaptureManager imageCaptureManager;

    private Button btnCaptureAvocado;
    private RecyclerView rvAvocadoList;
    private List<Avocado> avocadoList = new ArrayList<>();


    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_GALLERY_PERMISSION = 2;

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

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasReadExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_GALLERY_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//
//                // Both permissions were granted
//                // Continue with the camera/gallery intent
//            } else {
//                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
//            }
//        }
        // @@TODO Grant Gallery permissions wil require reworking this...
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted
                } else {
                    // Camera permission denied
                }
                break;
            case REQUEST_GALLERY_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Gallery permission granted
                } else {
                    // Gallery permission denied
                }
                break;
        }
    }


    public void avocadoUploadImageClick(View v) {
//        if (!hasCameraPermission() || !hasReadExternalStoragePermission()) {
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
////                        || !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
////                    // User has checked "Don't ask again"
////                    // Show a dialog/message directing the user to app settings to enable the permission
////                } else {
////                    requestPermissions();
////                }
////            }
//            requestPermissions();
//
//        } else {
            String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add a new Avocado");
            builder.setItems(options, (dialog, which) -> {
                if (options[which].equals("Take Photo")) {
                    imageCaptureManager.captureImage();
                } else if (options[which].equals("Choose from Gallery")) {
                    chooseFromGallery();
                } else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            });
            builder.show();
//        }
    }

    private void chooseFromGallery() {
        // TODO: Implement choose from gallery functionality
    }


}
