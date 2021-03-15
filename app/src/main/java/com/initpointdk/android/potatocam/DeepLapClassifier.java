package com.initpointdk.android.potatocam;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
public class DeepLapClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILE = "file:///android_asset/hanora_r_o.pb";
    private static final String INPUT_TENSOR_NAME = "ImageTensor:0";
    private static final String OUTPUT_TENSOR_NAME = "SemanticPredictions:0";



    private TensorFlowInferenceInterface inferenceInterface;

    private AppCompatActivity activity;
    public DeepLapClassifier(AppCompatActivity activity){
        this.activity = activity;
        inferenceInterface = new TensorFlowInferenceInterface(activity.getAssets(), MODEL_FILE);
    }

    public Bitmap run(Bitmap image){
        int max;
        if(image.getHeight() > image.getWidth())
            max = image.getHeight();
        else
            max = image.getWidth();
        float resizeRatio =  513f / max;
        int targetWidth = (int) (resizeRatio * image.getWidth());
        int targetHeight = (int) (resizeRatio * image.getHeight());
        Bitmap resizedImage = Bitmap.createScaledBitmap(image,targetWidth, targetHeight, false);

        int [] intValues = new int[targetWidth*targetHeight];

        resizedImage.getPixels(intValues, 0, targetWidth, 0, 0, targetWidth, targetHeight);

        int [] vImage = new int[targetHeight * targetWidth * 3];

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            vImage[i * 3] = ((val >> 16) & 0xff);
            vImage[i * 3 + 1] = ((val >> 8) & 0xff);
            vImage[i * 3 + 2] = (val & 0xff);
        }
        inferenceInterface.feed(INPUT_TENSOR_NAME, vImage, 1, resizedImage.getHeight(), resizedImage.getWidth(), 3);
        inferenceInterface.run(new String[]{OUTPUT_TENSOR_NAME});


        int [] out = new int[resizedImage.getWidth() * resizedImage.getHeight()];
        inferenceInterface.fetch(OUTPUT_TENSOR_NAME, out);

        for (int i = 0; i < out.length; i++) {
            if(out[i] == 15){
                out[i] = 0xff0000;
            }
        }
        resizedImage.setPixels(out, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());
        return Bitmap.createScaledBitmap(resizedImage,image.getWidth(), image.getHeight(), true);
    }

}
