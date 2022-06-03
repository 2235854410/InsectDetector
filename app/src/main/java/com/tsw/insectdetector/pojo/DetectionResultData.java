package com.tsw.insectdetector.pojo;

import android.graphics.Bitmap;

public class DetectionResultData {
    private String resultInfo;
    private String detectionTime;
    private Bitmap resultImg;

    public void setResultInfo(String resultInfo) {
        this.resultInfo = resultInfo;
    }

    public void setDetectionTime(String detectionTime) {
        this.detectionTime = detectionTime;
    }

    public void setResultImg(Bitmap resultImg) {
        this.resultImg = resultImg;
    }

    public String getResultInfo() {
        return resultInfo;
    }

    public String getDetectionTime() {
        return detectionTime;
    }

    public Bitmap getResultImg() {
        return resultImg;
    }
}
