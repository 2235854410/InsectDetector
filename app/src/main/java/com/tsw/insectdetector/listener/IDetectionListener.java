package com.tsw.insectdetector.listener;

import com.tsw.insectdetector.pojo.DetectionResultData;

public interface IDetectionListener {
    void onStartDetection();
    void onSuccessDetection(DetectionResultData data);
    void onErrorDetection();
}
