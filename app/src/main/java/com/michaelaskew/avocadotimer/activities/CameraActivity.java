package com.michaelaskew.avocadotimer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.utilities.ImageClassifierHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.classifier.Classifications;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private PreviewView cameraPreviewView;
    private LinearLayout preCaptureFeedback;
    private FrameLayout postCaptureFeedback;
    private ImageView capturedImageView;

    private ImageCapture imageCapture;

    private Uri capturedImageUri;

    private RelativeLayout sliderFeedback;
    private SeekBar softnessSlider;

    private ImageClassifierHelper imageClassifierHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreviewView = findViewById(R.id.cameraPreviewView);
        preCaptureFeedback = findViewById(R.id.preCaptureFeedback);
        postCaptureFeedback = findViewById(R.id.postCaptureFeedback);
        capturedImageView = findViewById(R.id.capturedImageView);

        sliderFeedback = findViewById(R.id.sliderFeedback);
        softnessSlider = findViewById(R.id.softnessSlider);


        initClassifier();
        initCamera();
    }

    private void initClassifier() {
        imageClassifierHelper = ImageClassifierHelper.create(
                this,
                new ImageClassifierHelper.ClassifierListener() {
                    @Override
                    public void onError(String error) {
                        // Handle error here
                        Toast.makeText(CameraActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResults(List<Classifications> results, long inferenceTime) {
                        // Handle the results here
                        if (results.size() > 0) {

                            Log.d("CameraActivity", "***********************************");
                            Log.d("CameraActivity", "***********************************");
                            Log.d("CameraActivity", "Results: " + results.toString());
                            Log.d("CameraActivity", "***********************************");
                            Log.d("CameraActivity", "***********************************");


                            boolean hasAvocado = false;
                            ArrayList<String> displayNames = new ArrayList<>();
                            for (org.tensorflow.lite.task.vision.classifier.Classifications classification : results) {
                                for (org.tensorflow.lite.support.label.Category category : classification.getCategories()) {
                                    displayNames.add(category.getLabel());
                                    if (category.getLabel() == "avocado") {
                                        hasAvocado = true;
                                    }
                                }
                            }
                            String toastMessage = "Hmm, we see a " + String.join(", a ", displayNames) + "... but not an avocado";
                            if (hasAvocado) {
                                toastMessage = "Confirmed avocado sighting!";
                            }
                            Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    }
                }
        );


    }

    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());


                Button captureButton = findViewById(R.id.captureButton);
                captureButton.setOnClickListener(v -> captureImage());

                Button acceptImageButton = findViewById(R.id.acceptImageButton);
                acceptImageButton.setOnClickListener(v -> {
                    preCaptureFeedback.setVisibility(View.GONE);
                    postCaptureFeedback.setVisibility(View.GONE);
                    sliderFeedback.setVisibility(View.VISIBLE);
                });


                Button rejectImageButton = findViewById(R.id.rejectImageButton);
                rejectImageButton.setOnClickListener(v -> {
                    // Handle the image rejection
                    // You can go back to the pre-capture state or allow the user to capture another image
                    preCaptureFeedback.setVisibility(View.VISIBLE);
                    postCaptureFeedback.setVisibility(View.GONE);
                });

                Button confirmSoftnessButton = findViewById(R.id.confirmSoftnessButton);
                confirmSoftnessButton.setOnClickListener(v -> {
                    int softnessValue = softnessSlider.getProgress();

                    Intent intent = new Intent(CameraActivity.this, AvocadoDetailActivity.class);
                    intent.putExtra("capturedImageUri", capturedImageUri.toString());
                    intent.putExtra("capturedSquishinessValue", softnessValue);
                    startActivity(intent);
                    finish();
                });

            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Capture Image
    private void captureImage() {
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(getExternalFilesDir(null), "capturedImage.jpg")).build();

        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                capturedImageUri = Uri.fromFile(new File(outputFileResults.getSavedUri().getPath()));
                Bitmap bitmap = BitmapFactory.decodeFile(capturedImageUri.getPath());
                capturedImageView.setImageDrawable(null);
                capturedImageView.setImageURI(capturedImageUri);
                capturedImageView.invalidate();
                preCaptureFeedback.setVisibility(View.GONE);
                postCaptureFeedback.setVisibility(View.VISIBLE);

                // Resizing the bitmap
                int modelInputSize = 224; // Adjust this based on your model's input size
                bitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true);

                // Normalizing the bitmap (if needed)
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int[] intValues = new int[width * height];
                bitmap.getPixels(intValues, 0, width, 0, 0, width, height);
                float[] floatValues = new float[width * height * 3];

                for (int i = 0; i < intValues.length; i++) {
                    final int val = intValues[i];
                    floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
                    floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
                    floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
                }

                if (bitmap != null) {
                    imageClassifierHelper.classify(bitmap, 0);
                }

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle capture error
            }
        });
    }
}
