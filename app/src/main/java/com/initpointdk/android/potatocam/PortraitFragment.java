package com.initpointdk.android.potatocam;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.warkiz.widget.IndicatorSeekBar;


public class PortraitFragment extends Fragment {
    private IndicatorSeekBar portraitSeekBar;
    private Button cancelBtn, doneBtn;
    private Bitmap originalImage;
    public static float blurSeekBarState = 5;
    private float x = blurSeekBarState;

    public PortraitFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_portrait, container, false);
        updateButtons(View.INVISIBLE);
        updateText("Blur");
        originalImage = EditingActivity.getOriginalImg();
        originalImage = EditingActivity.applySharpen(originalImage,((Number)EditingActivity.seekBarValues.get(1)).floatValue());
        originalImage = EditingActivity.applyBrightness(originalImage,((Number)EditingActivity.seekBarValues.get(2)).floatValue());
        originalImage = EditingActivity.applyContrast(originalImage,((Number)EditingActivity.seekBarValues.get(3)).floatValue());
        cancelBtn = v.findViewById(R.id.portrait_frag_cancel);
        doneBtn = v.findViewById(R.id.portrait_frag_done);
        portraitSeekBar = v.findViewById(R.id.portraitFragSeekbar);
        x = blurSeekBarState;
        portraitSeekBar.setProgress((int) Math.ceil(blurSeekBarState));
        if (originalImage != null) {
            portraitSeekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                    x = (float) progress/3f;
                }

                @Override
                public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    Bitmap temp = originalImage;
                    temp = EditingActivity.applyBlur(temp,x);
                    EditingActivity.setEditImage(temp);
                }
            });
        }
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    originalImage = EditingActivity.applyBlur(originalImage,((Number)EditingActivity.seekBarValues.get(0)).floatValue());
                    EditingActivity.setEditImage(originalImage);
                    updateButtons(View.VISIBLE);
                    updateText("");
                    removeFragment();
                }
            });
            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    blurSeekBarState = x;
                    EditingActivity.seekBarValues.set(0,x);
                    updateButtons(View.VISIBLE);
                    updateText("");
                    removeFragment();
                }
            });
            return v;
        }
    public void removeFragment(){
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(this);
        trans.commit();
        manager.popBackStack();
    }
    public void updateButtons(int n){
        EditingActivity.backBtn.setVisibility(n);
        EditingActivity.saveBtn.setVisibility(n);
    }
    public void updateText(String s){
        EditingActivity.modeName.setText(s);
    }
}
