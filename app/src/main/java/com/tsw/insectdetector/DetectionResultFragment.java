/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.tsw.insectdetector.pojo.DetectionResultData;

public class DetectionResultFragment extends Fragment{
    private final String TAG = getClass().getName();
    private SubsamplingScaleImageView imageView;
    private TextView textView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_result, container, false);
        imageView = view.findViewById(R.id.detect_result_img);
        textView = view.findViewById(R.id.detect_result_info);
        Button returnDetectorPage = view.findViewById(R.id.close_result_page);
        returnDetectorPage.setOnClickListener((v) -> {
            FragmentManager manager = getFragmentManager();
            assert manager != null;
            manager.beginTransaction().show(manager.findFragmentByTag("InsectDetectorFragment")).commit();  //TODO
            manager.beginTransaction().hide(this).commit();


        });
        return view;
    }

    public void setResultData(DetectionResultData data) {
        setResultImg(data.getResultImg());
        setResultInfo(data.getResultInfo());
    }

    public void setResultImg(Bitmap bitmap) {
        imageView.setImage(ImageSource.bitmap(bitmap));
    }

    public void setResultInfo(String info) {
        textView.setText(info);
    }


}
