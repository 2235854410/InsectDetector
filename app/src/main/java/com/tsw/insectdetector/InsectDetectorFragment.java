/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.tsw.insectdetector.listener.IDetectionListener;
import com.tsw.insectdetector.listener.IOnReceiveImageListener;
import com.tsw.insectdetector.pojo.DetectionResultData;
import com.tsw.insectdetector.task.ImageDownloadTask;
import com.tsw.insectdetector.tool.NetworkUtil;
import com.tsw.insectdetector.tool.ScreenUtil;
import com.tsw.insectdetector.tool.ThreadPoolUtil;
import com.tsw.insectdetector.view.InputDialog;
import com.tsw.insectdetector.view.SlideSwitch;
import com.wang.avi.AVLoadingIndicatorView;
import com.yalantis.ucrop.UCrop;

import java.io.InputStream;
import java.util.Objects;

import okhttp3.ResponseBody;

public class InsectDetectorFragment extends Fragment implements IOnReceiveImageListener, IDetectionListener {
    private final String TAG = getClass().getName();
    private static final int WRITE_PERMISSION = 0x01;
    private boolean onOfflineDetectionMode = false;
    private DetectionHandler detectionHandler;
    private MediaHandler mediaHandler;
    private PopupWindow popupWindow;
    private Button btnSubmit;
    private View popupView;
    private AVLoadingIndicatorView loadingIndicatorView;
    private DetectionResultFragment resultFragment;
    private InputDialog inputDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insect_detector, container, false);
        detectionHandler = new DetectionHandler(getContext(), this);
        loadingIndicatorView = view.findViewById(R.id.loading_anim);
        loadingIndicatorView.hide();
        resultFragment = (DetectionResultFragment) getFragmentManager().findFragmentByTag("DetectionResultFragment");
        mediaHandler = new MediaHandler();
        inputDialog = new InputDialog(getContext(), (input) -> {
            ImageDownloadTask imageDownloadTask = new ImageDownloadTask(input, new ImageDownloadTask.IDownloadListener() {
                @Override
                public void onSuccessDownload(ResponseBody body) {
                    onStartDetection();
                    InputStream is = body.byteStream();
                    Bitmap bitmap =  BitmapFactory.decodeStream(is);
                    detectionHandler.onlineDetection(bitmap);
                }

                @Override
                public void onErrorDownload(int errorCode) {

                }
            });
            ThreadPoolUtil threadPoolUtil = ThreadPoolUtil.getInstance();
            threadPoolUtil.execute(imageDownloadTask);
        });
        SlideSwitch slideSwitch = view.findViewById(R.id.offline_switch);
        slideSwitch.setOnStateChangedListener(state -> {   //离线模式默认关闭
            onOfflineDetectionMode = state;
            String action = onOfflineDetectionMode ? "开启" : "关闭";
            Toast.makeText(getContext(), "已" + action + "离线识别模式", Toast.LENGTH_SHORT).show();
        });
        btnSubmit = view.findViewById(R.id.btn_transmit_to_detector);
        preparePopupWindow();
        requestWritePermission();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowManager.LayoutParams lp = Objects.requireNonNull(getActivity()).getWindow().getAttributes();
                lp.alpha = 0.7f;
                popupWindow.showAtLocation(popupView, Gravity.BOTTOM,40,350);
                btnSubmit.setVisibility(View.INVISIBLE);
            }
        });
        return view;
    }

    private void preparePopupWindow() {
        popupView = View.inflate(getContext(), R.layout.popupwindow_submit_img, null);
        popupWindow = new PopupWindow(popupView, ScreenUtil.getScreenWeight(getActivity())*2/3, ScreenUtil.getScreenHeight(getActivity())/4);
        Button album = popupView.findViewById(R.id.btn_popup_album);
        Button camera = popupView.findViewById(R.id.btn_popup_camera);
        Button url = popupView.findViewById(R.id.btn_popup_url);
        Button close = popupView.findViewById(R.id.btn_popup_close);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = Objects.requireNonNull(getActivity()).getWindow().getAttributes();
            lp.alpha = 1.0f;
            Objects.requireNonNull(getActivity()).getWindow().setAttributes(lp);
            btnSubmit.setVisibility(View.VISIBLE);
        });

        close.setOnClickListener((view) -> {
            popupWindow.dismiss();
        });

        camera.setOnClickListener((view) -> {
            mediaHandler.takeCamera(getActivity(), getContext());
            popupWindow.dismiss();
        });

        album.setOnClickListener((view) -> {
            mediaHandler.openAlbum(getActivity(), getContext());
            popupWindow.dismiss();
        });

        url.setOnClickListener((v) -> {
            inputDialog.show();
            popupWindow.dismiss();
        });


    }

    private void requestWritePermission(){
        if(ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION);
        }
    }


    @Override
    public void onReceiveOriginalCameraData(Intent data) {
        mediaHandler.startUCrop(getActivity(), getContext(), mediaHandler.getImageUri());
    }

    @Override
    public void onReceiveOriginalAlbumData(Intent data) {
        mediaHandler.startUCrop(getActivity(), getContext(), data.getData());
    }

    @Override
    public void onReceiveCroppedImageData(Intent data) {
        onStartDetection();
        Uri uri = UCrop.getOutput(data);
        if(!onOfflineDetectionMode && NetworkUtil.checkNetworkConnectState(getContext())) {
            detectionHandler.onlineDetection(uri);
        } else {
            detectionHandler.offlineDetection(uri);
        }
    }

    @Override
    public void onStartDetection() {
        loadingIndicatorView.show();
    }

    @Override
    public void onSuccessDetection(DetectionResultData data) {
        loadingIndicatorView.hide();
        //Log.d(TAG, "onSuccessDetection: " + data.getResultInfo());
        resultFragment.setResultImg(data.getResultImg());
        resultFragment.setResultInfo(data.getResultInfo());

        getFragmentManager()
                .beginTransaction()
                .hide(this)
                .commit();
        getFragmentManager()
                .beginTransaction()
                .show(resultFragment)
                .commit();
    }

    @Override
    public void onErrorDetection() {
        loadingIndicatorView.hide();
    }
}
