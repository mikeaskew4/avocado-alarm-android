package com.michaelaskew.avocadotimer.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// ... other necessary imports ...

public class ImageCaptureManager {

    private final Activity activity;
    private Uri imageUri;
    private static final int REQUEST_CAPTURE_IMAGE = 100;

    public interface ImageCaptureListener {
        void onImageCaptured(Uri imageUri);
    }

    private ImageCaptureListener listener;

    public ImageCaptureManager(Activity activity) {
        this.activity = activity;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageUri = Uri.fromFile(image);
        return image;
    }

    public void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d("ImageCaptureManager", "Activity: " + activity);
        if(activity != null) {
            Log.d("ImageCaptureManager", "PackageManager: " + activity.getPackageManager());
        }
        // @@TODO fix this error?
//        if (intent.resolveActivity(activity.getPackageManager()) != null) {
        if (true) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity, "com.michaelaskew.avocadotimer.fileprovider", photoFile);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            }
        } else {
            Toast.makeText(activity, "Oops", Toast.LENGTH_SHORT).show();
        }

    }

    public void setListener(ImageCaptureListener listener) {
        this.listener = listener;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK && listener != null) {
            Log.d("ImageCaptureManager", "Captured");
            listener.onImageCaptured(imageUri);
        }
    }
    // ... other methods like createImageFile() ...
}
