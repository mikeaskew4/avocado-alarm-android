package com.michaelaskew.avocadotimer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.classifier.Classifications;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;

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
                        String toastMessage = "Is there an avocado there? Unless you want to try again, we'll take your word for it.";
                        if (!displayNames.isEmpty()) {
                            toastMessage = "Hmmm...didn't clearly see the avocado in the photo (" + displayNames.get(0) + "?). But if you say so...";
                        }
                        if (hasAvocado) {
                            toastMessage = "Looks good!";
                        }
                        caputredImageFeedback.setText(toastMessage);
                    }
                }
            },
            0,
            0.15f
        );
    }

    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        // Define the desired square aspect ratio

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // bind
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
//                        .setTargetResolution(CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);


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

    private void cropToSquare(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            int orientation = getOrientation(uri);
            int rotationDegrees = orientationToDegrees(orientation);
            bitmap = rotateBitmap(bitmap, rotationDegrees);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int size = Math.min(width, height);
            int x = (width - size) / 2;
            int y = (height - size) / 2;

            Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
            classifyImage(squareBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void classifyImage(Bitmap squareBitmap) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        // Resizing the bitmap
        int modelInputSize = 299; // Adjust this based on your model's input size
        Bitmap bitmap = Bitmap.createScaledBitmap(squareBitmap, modelInputSize, modelInputSize, true);
        displayCroppedImage(bitmap);

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

    private void displayCroppedImage(Bitmap squareBitmap) {
        capturedImageView.setImageDrawable(null); // Clear the ImageView
        capturedImageView.setImageBitmap(squareBitmap);

        saveBitmapToFile(squareBitmap, capturedImageUri);
    }

    private void saveBitmapToFile(Bitmap bitmap, Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(Uri imageUri) {
        try {
            ExifInterface exif = new ExifInterface(imageUri.getPath());
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    private int orientationToDegrees(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    // Capture Image
    private void captureImage() {
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(getExternalFilesDir(null), "avo_" + RandomStringUtils.randomAlphanumeric(64) + ".jpg")).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                capturedImageUri = Uri.fromFile(new File(outputFileResults.getSavedUri().getPath()));
                cropToSquare(capturedImageUri);

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
