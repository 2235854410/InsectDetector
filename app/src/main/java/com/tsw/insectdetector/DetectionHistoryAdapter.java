/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tsw.insectdetector.pojo.DetectionResultData;
import com.tsw.insectdetector.tool.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class DetectionHistoryAdapter extends RecyclerView.Adapter<DetectionHistoryAdapter.ViewHolder> {

    private final String TAG = getClass().getName();
    private final ArrayList<DetectionResultData> resultDataList;
    private Context context;
    private Activity activity;
    private View popupView;
    private PopupWindow popupWindow;

    public DetectionHistoryAdapter(ArrayList<DetectionResultData> resultDataList,Activity activity, Context context) {
        this.resultDataList = resultDataList;
        this.activity = activity;
        this.context = context;
        prepareHistoryPopupWindow();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detection_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetectionResultData data = resultDataList.get(position);

        File img = new File(context.getExternalFilesDir(null).getAbsolutePath()
                +"/insect/" + data.getDetectionTime() + ".jpg");
        Glide.with(holder.historyImg.getContext()).load(img).override(260,120).into(holder.historyImg);
        holder.historyTime.setText(data.getDetectionTime());
        holder.historyInfo.setText(data.getResultInfo());
        Log.d(TAG, "onBindViewHolder: " + data.getDetectionTime());
        holder.historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) holder.historyImg.getDrawable()).getBitmap();
                String info = holder.historyInfo.getText().toString();
                Log.d(TAG, "onClick: " + info);
                showHistoryPopupWindow(bitmap, info);
            }
        });
    }

    private void prepareHistoryPopupWindow() {
        popupView = View.inflate(context, R.layout.popupwindow_insect_info, null);
        popupWindow = new PopupWindow(popupView, ScreenUtil.getScreenWeight(activity)*5/6
                , ScreenUtil.getScreenHeight(activity)*5/7);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        Button close = popupView.findViewById(R.id.close_insect_popupwindow);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = Objects.requireNonNull(activity).getWindow().getAttributes();
            lp.alpha = 1.0f;
            Objects.requireNonNull(activity).getWindow().setAttributes(lp);
        });
        close.setOnClickListener((v) -> {
            popupWindow.dismiss();
        });
    }

    private void showHistoryPopupWindow(Bitmap bitmap, String info) {
        ImageView imageView = popupView.findViewById(R.id.insect_img);
        TextView textView = popupView.findViewById(R.id.insect_info);
        Glide.with(imageView.getContext()).load(bitmap).override(260,120).into(imageView);
        textView.setText(info);
        Log.d(TAG, "showHistoryPopupWindow: " + info);
        WindowManager.LayoutParams lp = Objects.requireNonNull(activity).getWindow().getAttributes();
        lp.alpha = 0.7f;
        popupWindow.showAtLocation(popupView, Gravity.CENTER,0,0);
    }

    @Override
    public int getItemCount() {
        return resultDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView historyTime;
        public TextView historyInfo;
        public ImageView historyImg;
        public FrameLayout historyLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            historyTime = itemView.findViewById(R.id.history_time);
            historyInfo = itemView.findViewById(R.id.history_info);
            historyImg = itemView.findViewById(R.id.history_img);
            historyLayout = itemView.findViewById(R.id.history_item);
        }
    }
}
