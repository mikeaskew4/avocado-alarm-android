package com.michaelaskew.avocadotimer.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.michaelaskew.avocadotimer.activities.CameraActivity;

import java.io.IOException;
import java.util.List;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

/** Helper class for wrapping Image Classification actions */
public class ImageClassifierHelper {
    private static final String TAG = "ImageClassifierHelper";
    private static final int DELEGATE_CPU = 0;
    private static final int DELEGATE_GPU = 1;
    private static final int DELEGATE_NNAPI = 2;
    private static final int MODEL_MOBILENETV1 = 0;
    private static final int MODEL_EFFICIENTNETV0 = 1;
    private static final int MODEL_EFFICIENTNETV1 = 2;
    private static final int MODEL_EFFICIENTNETV2 = 3;

    private float threshold;
    private int numThreads;
    private int maxResults;
    private int currentDelegate;
    private int currentModel;
    private final Context context;
    private final ClassifierListener imageClassifierListener;
    private ImageClassifier imageClassifier;

    /** Helper class for wrapping Image Classification actions */
    public ImageClassifierHelper(Float threshold,
                                 int numThreads,
                                 int maxResults,
                                 int currentDelegate,
                                 int currentModel,
                                 Context context,
                                 ClassifierListener imageClassifierListener) {
        this.threshold = threshold;
        this.numThreads = numThreads;
        this.maxResults = maxResults;
        this.currentDelegate = currentDelegate;
        this.currentModel = currentModel;
        this.context = context;
        this.imageClassifierListener = imageClassifierListener;

        Log.d(TAG, "***********************************");
        Log.d(TAG, "***********************************");
        Log.e(TAG, "Using Model: " + currentModel);
        Log.d(TAG, "***********************************");
        Log.d(TAG, "***********************************");
//            Toast.makeText(null, "hit", Toast.LENGTH_SHORT).show();
        setupImageClassifier();
    }

    public static ImageClassifierHelper create(
            Context context,
            ClassifierListener listener,
            Integer model,
            float threshold
    ) {
        return new ImageClassifierHelper(
                threshold,
                25,
                10,
                1,
                model,
                context,
                listener
        );
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setCurrentDelegate(int currentDelegate) {
        this.currentDelegate = currentDelegate;
    }

    public void setCurrentModel(int currentModel) {
        this.currentModel = currentModel;
    }

    private void setupImageClassifier() {
        ImageClassifier.ImageClassifierOptions.Builder optionsBuilder =
                ImageClassifier.ImageClassifierOptions.builder()
                        .setScoreThreshold(threshold)
                        .setMaxResults(maxResults);

        BaseOptions.Builder baseOptionsBuilder =
                BaseOptions.builder().setNumThreads(numThreads);

        switch (currentDelegate) {
            case DELEGATE_CPU:
                // Default
                break;
            case DELEGATE_GPU:
                if (new CompatibilityList().isDelegateSupportedOnThisDevice()) {
                    baseOptionsBuilder.useGpu();
                } else {
//                    imageClassifierListener.onError("GPU is not supported on "
//                            + "this device");
                }
                break;
            case DELEGATE_NNAPI:
                baseOptionsBuilder.useNnapi();
        }

        String modelName;
        switch (currentModel) {
            case MODEL_MOBILENETV1:
                modelName = "mobilenet_fids30_quantized_v9.tflite";
                break;
            case MODEL_EFFICIENTNETV0:
                modelName = "efficientnet-lite0.tflite";
                break;
            case MODEL_EFFICIENTNETV1:
                modelName = "efficientnet-lite1.tflite";
                break;
            case MODEL_EFFICIENTNETV2:
                modelName = "efficientnet-lite2.tflite";
                break;
            default:
                modelName = "mobilenet_fids30_quantized_v9.tflite";
        }
        try {


            imageClassifier =
                    ImageClassifier.createFromFileAndOptions(
                            context,
                            modelName,
                            optionsBuilder.build());


        } catch (IOException e) {
            imageClassifierListener.onError("Image classifier failed to "
                    + "initialize. See error logs for details");
            Log.e(TAG, "TFLite failed to load model with error: "
                    + e.getMessage());
        }
    }

    public void classify(Bitmap image, int imageRotation) {
        if (imageClassifier == null) {
            setupImageClassifier();
        }

        // Inference time is the difference between the system time at the start
        // and finish of the process
        long inferenceTime = SystemClock.uptimeMillis();

        // Create preprocessor for the image.
        // See https://www.tensorflow.org/lite/inference_with_metadata/
        //            lite_support#imageprocessor_architecture
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder().add(new Rot90Op(-imageRotation / 90)).build();

        // Preprocess the image and convert it into a TensorImage for classification.
        TensorImage tensorImage =
                imageProcessor.process(TensorImage.fromBitmap(image));

        // Classify the input image
        imageClassifier.classify(tensorImage);

        List<Classifications> result = imageClassifier.classify(tensorImage);

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;
        imageClassifierListener.onResults(result, inferenceTime);
    }

    public void clearImageClassifier() {
        imageClassifier = null;
    }

    /** Listener for passing results back to calling class */
    public interface ClassifierListener {
        void onError(String error);

        void onResults(List<Classifications> results, long inferenceTime);
    }
}