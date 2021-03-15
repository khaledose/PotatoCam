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

public class SharpenFragment extends Fragment {
    private IndicatorSeekBar sharpenSeekBar;
    private Button cancelBtn, doneBtn;
    private Bitmap originalImage;
    public static float seekBarState = 0;
    private float x = seekBarState;

    public SharpenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sharpen, container, false);
        updateButtons(View.INVISIBLE);
        updateText("Sharpen");
        originalImage = EditingActivity.getOriginalImg();
        originalImage = EditingActivity.applyBlur(originalImage,((Number)EditingActivity.seekBarValues.get(0)).floatValue());
        originalImage = EditingActivity.applyBrightness(originalImage,((Number)EditingActivity.seekBarValues.get(2)).floatValue());
        originalImage = EditingActivity.applyContrast(originalImage,((Number)EditingActivity.seekBarValues.get(3)).floatValue());
        cancelBtn = v.findViewById(R.id.sharpen_frag_cancel);
        doneBtn = v.findViewById(R.id.sharpen_frag_done);
        sharpenSeekBar = v.findViewById(R.id.sharpenFragSeekbar);
        x = seekBarState;
        sharpenSeekBar.setProgress((int) Math.ceil(seekBarState * 10));
        if (originalImage != null) {
            sharpenSeekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {

                @Override
                public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    Bitmap temp = originalImage;
                    temp = EditingActivity.applySharpen(temp,x);
                    EditingActivity.setEditImage(temp);                }

                @Override
                public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                    x = (float) progress / 10f;
                }
            });
        }
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originalImage = EditingActivity.applySharpen(originalImage,((Number)EditingActivity.seekBarValues.get(1)).floatValue());
                EditingActivity.setEditImage(originalImage);
                updateButtons(View.VISIBLE);
                updateText("");
                removeFragment();
            }
        });
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBarState = x;
                EditingActivity.seekBarValues.set(1,x);
                updateButtons(View.VISIBLE);
                updateText("");
                removeFragment();
            }
        });
        return v;
    }

    public void removeFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(this);
        trans.commit();
        manager.popBackStack();
    }

    public void updateButtons(int n) {
        EditingActivity.backBtn.setVisibility(n);
        EditingActivity.saveBtn.setVisibility(n);
    }

    public void updateText(String s) {
        EditingActivity.modeName.setText(s);
    }
}
