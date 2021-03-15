package com.initpointdk.android.potatocam;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class DepthPredictionModel {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILE = "file:///android_asset/hanora_depth.pb";
    private static final String INPUT_TENSOR_NAME = "Placeholder:0";
    private static final String OUTPUT_TENSOR_NAME = "ConvPred/ConvPred:0";
    private static final int HIGHT = 228;
    private static final int  WIDTH = 304;

    private TensorFlowInferenceInterface inferenceInterface;
    private AppCompatActivity activity;

    public DepthPredictionModel(AppCompatActivity activity){
        this.activity = activity;
        inferenceInterface = new TensorFlowInferenceInterface(activity.getAssets(), MODEL_FILE);
    }

    public Bitmap run(Bitmap image){
        Bitmap resizedImage = Bitmap.createScaledBitmap(image, WIDTH, HIGHT, false);
        int [] intValues = new int[WIDTH*HIGHT];
        resizedImage.getPixels(intValues, 0, WIDTH, 0, 0, WIDTH, HIGHT);
        float [] vImage = new float[HIGHT*WIDTH*3];
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            vImage[i * 3] = ((val >> 16) & 0xff);
            vImage[i * 3 + 1] = ((val >> 8) & 0xff);
            vImage[i * 3 + 2] = (val & 0xff);
        }
        inferenceInterface.feed(INPUT_TENSOR_NAME, vImage, 1, resizedImage.getHeight(), resizedImage.getWidth(), 3);
        inferenceInterface.run(new String[]{OUTPUT_TENSOR_NAME});
        float [] out = new float[128 * 160];
        inferenceInterface.fetch(OUTPUT_TENSOR_NAME, out);

        ByteBuffer byteBuf = ByteBuffer.allocate(4 * out.length);
        FloatBuffer floatBuf = byteBuf.asFloatBuffer();
        floatBuf.put(out);
        byte [] byte_array = byteBuf.array();

        float max = 0;
        for (int i = 0; i < out.length; i++) {
            if(out[i] > max)
                max = out[i];
        }

        float ratio = 255/max;
        int [] int_arr = new int[out.length];
        for (int i = 0; i < out.length; i++) {
            int r = (int) (ratio*out[i]);
            int g = (int) (ratio*out[i]);
            int b = (int) (ratio*out[i]);
            int_arr[i] = Color.argb(0, r, g, b);
        }
        resizedImage = Bitmap.createScaledBitmap(resizedImage, 160, 128, true);
        resizedImage.setPixels(int_arr, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());

        return Bitmap.createScaledBitmap(resizedImage, image.getWidth(), image.getHeight(), true);
    }
}
