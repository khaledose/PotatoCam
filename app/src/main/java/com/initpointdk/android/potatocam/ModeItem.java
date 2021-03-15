package com.initpointdk.android.potatocam;

public class ModeItem {
    private String modeName;
    private int modeRes;
    public ModeItem(String modeName, int modeRes){
        this.modeName = modeName;
        this.modeRes = modeRes;
    }
    public String getModeName(){return modeName;}
    public int getModeRes(){return modeRes;}
    public void setModeName(String s){modeName = s;}
    public void setModeRes(int r){modeRes = r;}
}
