package com.michaelaskew.avocadotimer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.google.common.util.concurrent.ListenableFuture;
import com.michaelaskew.avocadotimer.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import java.io.File;
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

        initCamera();
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

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                capturedImageUri = Uri.fromFile(new File(outputFileResults.getSavedUri().getPath()));
                capturedImageView.setImageURI(capturedImageUri);

                preCaptureFeedback.setVisibility(View.GONE);
                postCaptureFeedback.setVisibility(View.VISIBLE);
            }


            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle capture error
            }
        });
    }
}
