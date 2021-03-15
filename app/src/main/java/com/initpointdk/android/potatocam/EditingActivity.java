package com.initpointdk.android.potatocam;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageDilationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;


public class EditingActivity extends AppCompatActivity {
    public static ImageButton backBtn, saveBtn;
    public static TextView modeName;
    public static ArrayList seekBarValues;
    private static ImageView editImgImageView;
    private static Bitmap deeplapOutput;
    private static Bitmap originalImg;
    private static GPUImage gpu;
    private static ProgressDialog dialog;
    private static LensBlur lb;
    private RelativeLayout fragmentPlaceholder;
    private DeepLapClassifier deepLapClassifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_placeholder, ModesFragment.newInstance(R.id.fragment_placeholder)).commit();
        seekBarValues = new ArrayList();
        for (int i = 0; i < 6; i++)
            seekBarValues.add(0);
        resetEditingStates();
        lb = new LensBlur(25, 230, 4);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        gpu = new GPUImage(this);
        editImgImageView = (ImageView) findViewById(R.id.editImg);
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        modeName = (TextView) findViewById(R.id.editMode);
        fragmentPlaceholder = (RelativeLayout) findViewById(R.id.fragment_placeholder);
        deepLapClassifier = new DeepLapClassifier(this);
        if(getIntent().hasExtra("KEY1"))
            loadBitmapFromFile("KEY1");
        else if(getIntent().hasExtra("KEY2"))
            loadBitmapFromFile("KEY2");
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                backBtn.setBackgroundResource(outValue.resourceId);
                EditingActivity.this.finish();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                saveBtn.setBackgroundResource(outValue.resourceId);
                Bitmap finalOp = originalImg;
                finalOp = EditingActivity.applyBlur(finalOp,((Number)EditingActivity.seekBarValues.get(0)).floatValue());
                finalOp = EditingActivity.applySharpen(finalOp,((Number)EditingActivity.seekBarValues.get(1)).floatValue());
                finalOp = EditingActivity.applyBrightness(finalOp,((Number)EditingActivity.seekBarValues.get(2)).floatValue());
                finalOp = EditingActivity.applyContrast(finalOp,((Number)EditingActivity.seekBarValues.get(3)).floatValue());
                storeImage(finalOp);
                Toast.makeText(EditingActivity.this, "Image Saved!", Toast.LENGTH_LONG).show();
                EditingActivity.this.finish();
            }
        });
    }
    public void loadBitmapFromFile(String key){
        if(key.equals("KEY1")) {
            Uri uri = Uri.parse(getIntent().getExtras().getString(key));
            originalImg = createBitmapFromUri(uri);
            originalImg = imageOrientation(originalImg, uri);
        }
        else if(key.equals("KEY2")){
            String path = getIntent().getExtras().getString(key);
            File image = new File(path);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            originalImg = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            originalImg = imageOrientation(originalImg, path);
            image.delete();
        }
        originalImg = resizeBitmap(originalImg,1000);
        setEditImage(originalImg);
        dialog.show();
        new LoadImageDataTask().execute();
        if (originalImg == null)
            this.finish();
    }
    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(pictureFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private File getOutputMediaFile() {
        String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "img_" + currentTime;
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/DCIM/PotatoCam");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File mediaFile;
        String mImageName = filename + ".png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void resetEditingStates() {
        seekBarValues.set(0, 0);
        seekBarValues.set(1, 0);
        seekBarValues.set(2, 0);
        seekBarValues.set(3, 0);
        originalImg = null;
        deeplapOutput = null;
    }
    public Bitmap resizeBitmap(Bitmap img, int maxLen) {
        float ratio = (float) maxLen / (float) ((Math.max(img.getWidth(), img.getHeight())));
        Bitmap img2 = Bitmap.createScaledBitmap(img, (int) (img.getWidth() * ratio), (int) (img.getHeight() * ratio), false);
        return img2;
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

    public Bitmap imageOrientation(Bitmap img, Uri uri) {
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

    public Bitmap imageOrientation(Bitmap img, String path) {
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
    public void performMagic() {
        deeplapOutput = deepLapClassifier.run(originalImg);
    }
//    public static Bitmap applyBlur(Bitmap img, float blur){
//        Bitmap oriImg = img, BGImg = img, pplImg = img;
//        if(blur!=0) {
//            lb.setRadius(blur * 2);
//            lb.setBloomThreshold(10 - blur + 250);
//            lb.setBloom(blur);
//            Bitmap edge = edgeDetection(deeplapOutput, 3);
//            Bitmap temp = lb.filter(BGImg, deeplapOutput);
//            pplImg = cutImage(pplImg, deeplapOutput, Color.BLACK);
//            oriImg = overlay(temp, pplImg);
//            Bitmap innerEdge = getEdge(edge, deeplapOutput, 1);
//            Bitmap outerEdge = getEdge(edge, deeplapOutput, 0);
//            outerEdge = edgeDetection(outerEdge, blur - 5);
//            outerEdge = getEdge(outerEdge, deeplapOutput, 0);
//            oriImg = smoothEdges(oriImg, innerEdge, blur / 6f);
//            oriImg = smoothEdges(oriImg, outerEdge, blur / 4f);
//        }
//        return oriImg;
//    }
    public static Bitmap applyBlur(Bitmap img, float blur){
        Bitmap oriImg = img, BGImg = img, pplImg = img;
        if(blur!=0) {
            gpu.setFilter(new GPUImageGaussianBlurFilter(blur));
            gpu.setImage(pplImg);
            BGImg = gpu.getBitmapWithFilterApplied();
            Bitmap edge = edgeDetection(deeplapOutput, 3);
            pplImg = cutImage(pplImg, deeplapOutput, Color.BLACK);
            oriImg = overlay(BGImg, pplImg);
            Bitmap innerEdge = getEdge(edge, deeplapOutput, 1);
            Bitmap outerEdge = getEdge(edge, deeplapOutput, 0);
            outerEdge = edgeDetection(outerEdge, blur - 5);
            outerEdge = getEdge(outerEdge, deeplapOutput, 0);
            oriImg = smoothEdges(oriImg, innerEdge, blur / 6f);
            oriImg = smoothEdges(oriImg, outerEdge, blur / 4f);
        }
        return oriImg;
    }
    public static Bitmap applySharpen(Bitmap img, float sharp){
        Bitmap oriImg = img, BGImg = img, pplImg = img;
        if(sharp!=0) {
            gpu.setFilter(new GPUImageSharpenFilter(sharp));
            gpu.setImage(pplImg);
            pplImg = gpu.getBitmapWithFilterApplied();
            pplImg = cutImage(pplImg, deeplapOutput, Color.BLACK);
            oriImg = connectImages(oriImg, pplImg);
        }
        return oriImg;
    }
    public static Bitmap applyBrightness(Bitmap img, float brightness){
        Bitmap oriImg = img, BGImg = img, pplImg = img;
        if(brightness!=0) {
            gpu.setFilter(new GPUImageExposureFilter(brightness));
            gpu.setImage(oriImg);
            oriImg = gpu.getBitmapWithFilterApplied();
        }
        return oriImg;
    }
    public static Bitmap applyContrast(Bitmap img, float contrast){
        Bitmap oriImg = img, BGImg = img, pplImg = img;
        if(contrast!=0) {
            gpu.setFilter(new GPUImageContrastFilter(contrast));
            gpu.setImage(oriImg);
            oriImg = gpu.getBitmapWithFilterApplied();
        }
        return oriImg;
    }
    public static void setEditImage(Bitmap oriImg) {
            editImgImageView.setImageBitmap(oriImg);
    }
    public static Bitmap getOriginalImg(){return originalImg;}
    public static Bitmap cutImage(final Bitmap original, final Bitmap segmented, int color) {
        final int w = original.getWidth(), h = original.getHeight();
        int[] ori = new int[w * h];
        int[] seg = new int[w * h];
        int[] out = new int[w * h];
        original.getPixels(ori, 0, w, 0, 0, w, h);
        segmented.getPixels(seg, 0, w, 0, 0, w, h);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int rc = Color.red(color);
        int gc = Color.green(color);
        int bc = Color.blue(color);
        for (int i = 0; i < w * h; i++) {
            int p = seg[i];
            int r = Color.red(p);
            int g = Color.green(p);
            int b = Color.blue(p);
            int flag = 0;
            if (Math.abs((r + g + b) - (rc + gc + bc)) < 20) {
                out[i] = Color.argb(0, Color.red(ori[i]), Color.green(ori[i]), Color.blue(ori[i]));
                continue;
            }
            out[i] = ori[i];
        }
        bitmap.setPixels(out, 0, w, 0, 0, w, h);
        return bitmap;
    }
    public static Bitmap connectImages(final Bitmap fullImage, final Bitmap cutImage) {
        final int w = fullImage.getWidth(), h = fullImage.getHeight();
        int[] full = new int[w * h];
        int[] cut = new int[w * h];
        int[] out = new int[w * h];
        fullImage.getPixels(full, 0, w, 0, 0, w, h);
        cutImage.getPixels(cut, 0, w, 0, 0, w, h);
        final Bitmap bitmap = fullImage.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < w * h; i++) {
            int px1 = full[i];
            int px2 = cut[i];
            int ac = Color.alpha(px2);
            if (ac == 255)
                out[i] = px2;
            else
                out[i] = px1;
        }
        bitmap.setPixels(out, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    public static Bitmap edgeDetection(Bitmap img,float thickness) {
        int w = img.getWidth(), h = img.getHeight();
        int[] edge1 = new int[w * h];
        gpu.setFilter(new GPUImageSobelEdgeDetection());
        gpu.setImage(img);
        Bitmap temp1 = gpu.getBitmapWithFilterApplied();
        for (int i = 0; i < thickness; i++) {
            gpu.setFilter(new GPUImageDilationFilter(4));
            gpu.setImage(temp1);
            temp1 = gpu.getBitmapWithFilterApplied();
        }
        temp1.getPixels(edge1,0,w,0,0,w,h);
        for(int i=0;i<w*h;i++)
            if(Color.red(edge1[i])>0)
                edge1[i] = Color.argb(255,255,255,255);
            else
                edge1[i] = Color.argb(255,0,0,0);
        temp1.setPixels(edge1,0,w,0,0,w,h);
        return temp1;
    }

    public static Bitmap smoothEdges(Bitmap img, Bitmap edge, float blur) {
        gpu.setFilter(new GPUImageGaussianBlurFilter(blur));
        gpu.setImage(img);
        Bitmap temp1 = gpu.getBitmapWithFilterApplied();
        int w = img.getWidth(), h = img.getHeight();
        int[] edg = new int[w * h];
        int[] blr = new int[w * h];
        int[] org = new int[w * h];
        int[] op = new int[w * h];
        edge.getPixels(edg, 0, w, 0, 0, w, h);
        temp1.getPixels(blr, 0, w, 0, 0, w, h);
        img.getPixels(org, 0, w, 0, 0, w, h);
        for (int i = 0; i < w * h; i++) {
            int px = Color.red(edg[i]);
            if (px > 0)
                op[i] = blr[i];
            else if(px==0)
                op[i] = org[i];
        }
        Bitmap finalImg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        finalImg.setPixels(op, 0, w, 0, 0, w, h);
        return finalImg;
    }

    public static Bitmap getEdge(Bitmap edge, Bitmap segment, int color) {
        int w = edge.getWidth(), h = edge.getHeight();
        int[] edg = new int[w * h];
        int[] seg = new int[w * h];
        int[] op = new int[w * h];
        edge.getPixels(edg, 0, w, 0, 0, w, h);
        segment.getPixels(seg, 0, w, 0, 0, w, h);
        for (int i = 0; i < w * h; i++) {
            if (color == 0) {
                if (Color.red(seg[i]) == 0 && Color.red(edg[i]) > 0)
                    op[i] = Color.WHITE;
                else
                    op[i] = Color.BLACK;
            } else {
                if (Color.red(seg[i]) > 0 && Color.red(edg[i]) > 0)
                    op[i] = Color.WHITE;
                else
                    op[i] = Color.BLACK;
            }
        }
        Bitmap finalImg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        finalImg.setPixels(op, 0, w, 0, 0, w, h);
        return finalImg;
    }
    private class LoadImageDataTask extends AsyncTask<Void, Void, Bitmap[]> {
        LoadImageDataTask() {
        }
        @Override
        protected Bitmap[] doInBackground(Void... params) {
            performMagic();
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmap) {
            super.onPostExecute(bitmap);
            final Handler handler = new Handler();
            if (originalImg != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        editImgImageView.setImageBitmap(applyBlur(originalImg,5));
                        //setEditImage(seekBarValues);
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                }, 300);
            }
        }
    }
}

