/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.tsw.insectdetector.listener.IDataFetchListener;
import com.tsw.insectdetector.listener.IPageStateListener;
import com.tsw.insectdetector.pojo.DetectionResultData;
import com.tsw.insectdetector.task.DBReadTask;
import com.tsw.insectdetector.tool.ThreadPoolUtil;
import com.tsw.insectdetector.view.ReconfirmDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class DetectionHistoryFragment extends Fragment implements IDataFetchListener, IPageStateListener {

    private final String TAG = getClass().getName();
    private ArrayList<DetectionResultData> resultDataList;
    private DetectionHistoryAdapter historyAdapter;
    private ReconfirmDialog reconfirmDialog;
    private int cleanedDayIndex = 0;
    private final String[] historyDays = {"最近1天", "最近7天", "所有"};
    private AlertDialog alertDialog;
    private TextView pageEmpty;
    private RecyclerView recyclerView;
    Handler uiHandler = new Handler();
    private final int MILLISECONDS_PER_DAY = 24*60*60*1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_history, container, false);
        prepareReconfirmDialog();
        Button cleanHistory = view.findViewById(R.id.clean_history);
        pageEmpty = view.findViewById(R.id.empty_history);
        pageEmpty.setVisibility(View.INVISIBLE);
        recyclerView = view.findViewById(R.id.history_recycler);
        resultDataList = new ArrayList<>();
        alertDialog = new AlertDialog
                .Builder(Objects.requireNonNull(getContext()))
                .setItems(historyDays, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cleanedDayIndex = which;
                        showReconfirmDialog();
                    }
                }).create();
        cleanHistory.setOnClickListener((v) -> {
            alertDialog.show();
        });
        DBReadTask readTask = new DBReadTask(getContext(), this, resultDataList);
        ThreadPoolUtil threadPoolUtil = ThreadPoolUtil.getInstance();
        threadPoolUtil.execute(readTask);
        return view;
    }


    private void prepareReconfirmDialog(){
        reconfirmDialog = new ReconfirmDialog(getContext());
        reconfirmDialog.setTitle("消息提示");
        reconfirmDialog.setYesOnclickListener(new ReconfirmDialog.onYesOnclickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onYesClick() throws ParseException {
                writeToDatabase();
                deleteDataListElement();
                historyAdapter.notifyDataSetChanged();
                onPageEmpty(resultDataList.size() == 0);
                reconfirmDialog.dismiss();
            }
        });
        reconfirmDialog.setNoOnclickListener(new ReconfirmDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                reconfirmDialog.dismiss();
            }
        });
    }

    private void showReconfirmDialog() {
        reconfirmDialog.setMessage("是否确定清除"+historyDays[cleanedDayIndex] + "的历史记录？");
        reconfirmDialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeToDatabase(){
        DatabaseHelper myDBHelper = DatabaseHelper.getInstance(getContext());
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        if(cleanedDayIndex == 2){
            db.execSQL("delete from detectedhistory");
        }
        else {
            int[] days = {1,7};
            LocalDateTime now = LocalDateTime.now();
            db.execSQL("delete from detectedhistory where julianday('now') - julianday(detect_time) < "+days[cleanedDayIndex]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteDataListElement() throws ParseException {
        SimpleDateFormat sim =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d(TAG, "deleteDataListElement: start delete");
        Date now = new Date();
        if(cleanedDayIndex == 2) {
            resultDataList.clear();
            Log.d(TAG, "deleteDataListElement: delete all data");
        } else {
            for(DetectionResultData data : resultDataList) {
                Date date = sim.parse(data.getDetectionTime());
                int days = cleanedDayIndex == 0 ? 1 : 7;
                if((now.getTime() - date.getTime()) < (long) days * MILLISECONDS_PER_DAY) {
                    resultDataList.remove(data);
                }
            }
        }

    }

    @Override
    public void onDataReady() {

        Runnable runnable = () -> {
            if(resultDataList.size() == 0) {
                onPageEmpty(true);
            } else {
                onPageEmpty(false);
                historyAdapter = new DetectionHistoryAdapter(resultDataList, getActivity(), getContext());
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.setAdapter(historyAdapter);
            }

        };
        uiHandler.post(runnable);

    }

    @Override
    public void onFetchError() {

    }

    @Override
    public void onPageEmpty(boolean empty) {
        pageEmpty.setVisibility(empty? View.VISIBLE : View.INVISIBLE);
    }

}
