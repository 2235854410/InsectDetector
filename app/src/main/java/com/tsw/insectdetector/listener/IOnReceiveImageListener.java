package com.tsw.insectdetector.listener;

import android.content.Intent;
import android.net.Uri;

public interface IOnReceiveImageListener {
    void onReceiveOriginalCameraData(Intent data);
    void onReceiveOriginalAlbumData(Intent data);
    void onReceiveCroppedImageData(Intent data);

}
