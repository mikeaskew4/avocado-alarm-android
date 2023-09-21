package com.michaelaskew.avocadotimer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraActivity extends AppCompatActivity {

    private PreviewView cameraPreviewView;
    private LinearLayout preCaptureFeedback;
    private FrameLayout postCaptureFeedback;
    private ImageView capturedImageView;

    private ImageCapture imageCapture;
    private TextView caputredImageFeedback;

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
        caputredImageFeedback = findViewById(R.id.capturedImageFeedback);
        caputredImageFeedback.setAllCaps(false);

        sliderFeedback = findViewById(R.id.sliderFeedback);
        softnessSlider = findViewById(R.id.softnessSlider);

        initClassifier();
        initCamera();
    }

    private void initClassifier() {
        // @@TODO bring these into the model (non-quantized) --or-- make model binary
        String[] classNames = {
                "acerolas", "apples", "apricots", "avocados", "bananas", "blackberries", "blueberries", "cantaloupes", "cherries", "coconuts", "figs", "grapefruits", "grapes", "guava", "kiwifruit", "lemons", "limes", "mangos", "olives", "oranges", "passionfruit", "peaches", "pears", "pineapples", "plums", "pomegranates", "raspberries", "strawberries", "tomatoes", "watermelons"
        };
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
                                String categoryName = classNames[Integer.parseInt(category.getLabel())];
                                displayNames.add(categoryName);
                                if (categoryName == "avocados") {
                                    hasAvocado = true;
                                }
                            }
                        }
                        String toastMessage = "Didn't see an avocado, but we'll take your word for it.";
                        if (!displayNames.isEmpty()) {
                            toastMessage = "Are you sure there's an avocado there? Looks like " + displayNames.get(0) + "... but AI isn't very bright";
                            if (hasAvocado) {
                                toastMessage = "Nice avocado!";
                            }
                        }
                        caputredImageFeedback.setText(toastMessage);
                    }
                }
            }
        );
    }

    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        // Define the desired square aspect ratio
        int targetAspectRatio = 1; // 1:1
        // Calculate the target resolution based on the aspect ratio
        Size targetSize = new Size(640, 640); // You can adjust the size as needed

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(targetSize)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

                // set up buttons
                // @@TODO - better logic
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
//                    saveImage();
                    startActivity(intent);
                    finish();
                });

            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors
                // @@ TODO
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Save Image to Camera Roll
    // @@TODO not for production
    private void saveImage() {
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        // Define output file options for saving the captured image
        File outputDirectory = new File(getExternalMediaDirs()[0], "AvocadoTimer"); // Change directory as needed
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File outputFile = new File(outputDirectory, imageFileName);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        // Capture the image and save it to the photo library/camera roll
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Image saved successfully, you can add your logic here
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle error if image capture fails
            }
        });
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
                int modelInputSize = 299; // Adjust this based on your model's input size
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
                    imageClassifierHelper.classify(bitmap, rotation);
                }

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle capture error
            }
        });
    }
}
