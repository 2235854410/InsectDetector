/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tsw.insectdetector.task.GetStringTask;
import com.tsw.insectdetector.tool.NetworkUtil;
import com.tsw.insectdetector.tool.ThreadPoolUtil;
import com.tsw.insectdetector.view.ChinaMapView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;

import okhttp3.ResponseBody;

public class ChinaMapFragment extends Fragment {
    Handler uiHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_china_map, container, false);
        ChinaMapView mapView = view.findViewById(R.id.china_map);
        TextView insectDistribute = view.findViewById(R.id.insect_distribute);
        AVLoadingIndicatorView loadingAnim = view.findViewById(R.id.loading_anim);
        Button closePage = view.findViewById(R.id.close_map_page);
        loadingAnim.hide();
        closePage.setOnClickListener((v) -> {
            FragmentManager manager = getFragmentManager();
            assert manager != null;
            manager.beginTransaction().show(manager.findFragmentByTag("InsectIntroductionFragment")).commit();
            manager.beginTransaction().hide(this).commit();
        });
        mapView.setMapColor(Color.DKGRAY);
        mapView.setOnProvinceSelectedListener(new ChinaMapView.OnProvinceSelectedListener() {
            @Override
            public void onProvinceSelected(ChinaMapView.Area pArea) {
                loadingAnim.show();
                String url = NetworkUtil.HTTP_SERVER + "city";
                GetStringTask getStringTask = new GetStringTask(url, "city", pArea.getName()
                        , new GetStringTask.IGetStringListener() {
                    @Override
                    public void onSuccessGet(ResponseBody responseBody) throws IOException {
                        String result = responseBody.string();
                        Runnable uiRunnable = () -> {
                            insectDistribute.setText(result);
                            loadingAnim.hide();
                        };
                        uiHandler.post(uiRunnable);
                    }

                    @Override
                    public void onErrorGet(int errorCode) {

                    }
                });
                ThreadPoolUtil threadPoolUtil = ThreadPoolUtil.getInstance();
                threadPoolUtil.execute(getStringTask);
            }
        });
        return view;
    }
}
