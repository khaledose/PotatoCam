package com.initpointdk.android.potatocam;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class PotatoImage {
    private Bitmap mask;
    private Bitmap image;
    private GPUImage gpu;
    private DeepLapClassifier deepLab;
    private Dictionary<String, Integer> values;
    public PotatoImage(Context context){
        gpu = new GPUImage(context);
        deepLab = new DeepLapClassifier((AppCompatActivity) context);
    }
    public void loadImageFromFile(String key){
        if(key.equals("KEY1")) {
            Uri uri = Uri.parse(getIntent().getExtras().getString(key));
            this.image = createBitmapFromUri(uri);
            this.image = imageOrientation(this.image, uri);
        }
        else if(key.equals("KEY2")){
            String path = getIntent().getExtras().getString(key);
            File image = new File(path);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            this.image = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            this.image = imageOrientation(this.image, path);
            image.delete();
        }
        this.image = resizeBitmap(this.image,1000);
    }

    public Bitmap createBitmapFromUri(Uri uri) {
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap img = BitmapFactory.decodeStream(imageStream);

        return resizeBitmap(img, 1000);
    }

    private Bitmap resizeBitmap(Bitmap img, int maxLen) {
        float ratio = (float) maxLen / (float) ((Math.max(img.getWidth(), img.getHeight())));
        img = Bitmap.createScaledBitmap(img, (int) (img.getWidth() * ratio), (int) (img.getHeight() * ratio), false);
        return img;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Bitmap imageOrientation(Bitmap img, Uri uri) {
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    private Bitmap imageOrientation(Bitmap img, String path) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(img, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(img, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(img, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = img;
        }
        return rotatedBitmap;
    }
}
