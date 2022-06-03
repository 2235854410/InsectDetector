/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tsw.insectdetector.pojo.InsectIntroductionData;
import com.tsw.insectdetector.tool.NetworkUtil;
import com.tsw.insectdetector.tool.ScreenUtil;

import java.util.List;
import java.util.Objects;

public class InsectIntroductionAdapter extends RecyclerView.Adapter<InsectIntroductionAdapter.ViewHolder> {

    private final String TAG = getClass().getName();
    private List<InsectIntroductionData> introductionDataList;
    private Context context;
    private Activity activity;
    private PopupWindow popupWindow;
    private View popupView;


    public InsectIntroductionAdapter(Activity activity, Context context, List<InsectIntroductionData> list) {
        this.activity = activity;
        this.context = context;
        introductionDataList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_insect_instroduction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        prepareHistoryPopupWindow();
        InsectIntroductionData data = introductionDataList.get(position);
        if("null".equals(data.getImgUrl())) {
            Glide.with(holder.introductionImg.getContext())
                    .load(BitmapFactory.decodeResource(context.getResources(), R.drawable.empty)).
                    override(260,120).into(holder.introductionImg);
        } else {
            Glide.with(holder.introductionImg.getContext()).load(NetworkUtil.HTTP_SERVER + data.getImgUrl()).
                    override(260,120).into(holder.introductionImg);
        }
        holder.introductionInfo.setText(data.getIntroduction());
        holder.insectLayout.setOnClickListener((v) -> {
            Bitmap bitmap = ((BitmapDrawable) holder.introductionImg.getDrawable()).getBitmap();
            String introduction = holder.introductionInfo.getText().toString();
            showHistoryPopupWindow(bitmap, introduction);
        });
    }

    @Override
    public int getItemCount() {
        return introductionDataList.size();
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
        Log.d(TAG, "showIntroductionPopupWindow: " + info);
        WindowManager.LayoutParams lp = Objects.requireNonNull(activity).getWindow().getAttributes();
        lp.alpha = 0.7f;
        popupWindow.showAtLocation(popupView, Gravity.CENTER,0,0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView introductionInfo;
        public ImageView introductionImg;
        public LinearLayout insectLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            introductionImg = itemView.findViewById(R.id.insect_img);
            introductionInfo = itemView.findViewById(R.id.insect_info);
            insectLayout = itemView.findViewById(R.id.insect_ll);
        }
    }
}
