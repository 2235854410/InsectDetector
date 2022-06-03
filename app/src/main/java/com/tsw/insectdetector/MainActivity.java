/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */


package com.tsw.insectdetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.yalantis.ucrop.UCrop;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private RadioGroup bottomNavigationBg;
    private InsectDetectorFragment detectorFragment;
    private InsectIntroductionFragment introductionFragment;
    private DetectionHistoryFragment historyFragment;
    private ChinaMapFragment chinaMapFragment;
    private FragmentManager fragmentManager;
    private DetectionResultFragment resultFragment;
    private View stateBarPaddingView;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        if(null != savedInstanceState) {
            detectorFragment = (InsectDetectorFragment) fragmentManager.findFragmentByTag("InsectDetectorFragment");
            introductionFragment = (InsectIntroductionFragment) fragmentManager.findFragmentByTag("InsectIntroductionFragment");
            historyFragment = (DetectionHistoryFragment) fragmentManager.findFragmentByTag("DetectionHistoryFragment");

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stateBarPaddingView = findViewById(R.id.state_bar_padding);
        ViewGroup.LayoutParams lp = stateBarPaddingView.getLayoutParams();
        lp.height = getStatusBarHeight();
        stateBarPaddingView.setLayoutParams(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(0xffF4F4F4);
        bottomNavigationBg = findViewById(R.id.bottom_navigation_rg);
        resultFragment = new DetectionResultFragment();
        chinaMapFragment = new ChinaMapFragment();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, chinaMapFragment, "ChinaMapFragment")
                .commit();
        fragmentManager.
                beginTransaction()
                .hide(chinaMapFragment)
                .commit();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, resultFragment, "DetectionResultFragment")
                .commit();
        fragmentManager.
                beginTransaction()
                .hide(resultFragment)
                .commit();
        bottomNavigationBg.setOnCheckedChangeListener((radioGroup, i) -> {
            for(Fragment fragment: fragmentManager.getFragments()) {
                if(fragment.getTag() == "DetectionHistoryFragment") {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                } else {
                    fragmentManager.beginTransaction().hide(fragment).commit();
                }
            }
            switch (i) {
                case R.id.detector_rb:
                    if(null == detectorFragment) {
                        detectorFragment = new InsectDetectorFragment();
                        fragmentManager
                                .beginTransaction()
                                .add(R.id.fragment_container, detectorFragment, "InsectDetectorFragment")
                                .commit();
                        Log.d(TAG, "onCreate: new detectorFragment");
                    } else {
                        fragmentManager.
                                beginTransaction()
                                .show(detectorFragment)
                                .commit();
                        Log.d(TAG, "onCreate: show old detectorFragment");
                    }
                    break;
                case R.id.introduction_rb:
                    if(null == introductionFragment) {
                        introductionFragment = new InsectIntroductionFragment();
                        fragmentManager
                                .beginTransaction()
                                .add(R.id.fragment_container, introductionFragment, "InsectIntroductionFragment")
                                .commit();
                    } else {
                        fragmentManager
                                .beginTransaction()
                                .show(introductionFragment)
                                .commit();
                    }
                    break;
                case R.id.history_rb:
                    if(null == historyFragment) {
                        historyFragment = new DetectionHistoryFragment();
                    }
                    fragmentManager
                            .beginTransaction()
                            .add(R.id.fragment_container, historyFragment, "DetectionHistoryFragment")
                            .commit();
                    break;
            }
        });

        ((RadioButton) bottomNavigationBg.getChildAt(1)).setChecked(true);


    }

    private int getStatusBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        return resources.getDimensionPixelSize(resourceId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MediaHandler.TAKE_CAMERA:
                if(resultCode == Activity.RESULT_OK) {
                    detectorFragment.onReceiveOriginalCameraData(data);
                }
                break;
            case MediaHandler.OPEN_ALBUM:
                if(null != data) {
                    detectorFragment.onReceiveOriginalAlbumData(data);
                }
                break;
            case UCrop.REQUEST_CROP:
                if(resultCode == Activity.RESULT_OK && null != data) {
                    detectorFragment.onReceiveCroppedImageData(data);
                }

        }
    }
}