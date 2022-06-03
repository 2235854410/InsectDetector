/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.nanchen.compresshelper.CompressHelper;
import com.tsw.insectdetector.listener.IDetectionListener;
import com.tsw.insectdetector.pojo.DetectionResultData;
import com.tsw.insectdetector.task.ImageDownloadTask;
import com.tsw.insectdetector.task.ImageUploadTask;
import com.tsw.insectdetector.tool.NetworkUtil;
import com.tsw.insectdetector.tool.ThreadPoolUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;

public class DetectionHandler {
    private final String TAG = getClass().getName();
    private final IDetectionListener detectionListener;
    private final Context context;
    private final YoloV5Ncnn yolov5ncnn;
    private Map<String, Integer> resultRecordMap;
    private final ThreadPoolUtil threadPoolUtil;
    private DetectionResultData resultData;
    private final Handler uiHandler = new Handler();
    private final Runnable successDetectionRunnable = new Runnable() {
        @Override
        public void run() {
            detectionListener.onSuccessDetection(resultData);
        }
    };
    private final Runnable failedDetectionRunnable = new Runnable() {
        @Override
        public void run() {
            detectionListener.onErrorDetection();
        }
    };



    public DetectionHandler(Context context, IDetectionListener listener) {
        detectionListener = listener;
        this.context = context;
        resultRecordMap = new HashMap<>();
        yolov5ncnn = new YoloV5Ncnn();
        resultData = new DetectionResultData();
        threadPoolUtil = ThreadPoolUtil.getInstance();
        boolean ret_init = yolov5ncnn.Init(context.getAssets());
        if (!ret_init)
        {
            Log.e(TAG, "yolov5ncnn Init failed!");
        }
    }



    public void onlineDetection(Uri imgUri) {
        Bitmap inputBitmap = getCompressedBitmap(imgUri);
        onlineDetection(inputBitmap);
    }

    public void onlineDetection(Bitmap bitmap) {
        ImageUploadTask imageUploadTask = new ImageUploadTask(bitmap, new ImageUploadTask.IUploadListener() {
            @Override
            public void onSuccessUpload(ResponseBody responseBody) throws IOException, JSONException {
                JSONObject json = new JSONObject(responseBody.string());
                String imgName = json.getString("img");
                String info = json.getString("name");
                String  imgUrl = NetworkUtil.HTTP_SEVER + "result/" + imgName;
                ImageDownloadTask imageDownloadTask = new ImageDownloadTask(imgUrl, new ImageDownloadTask.IDownloadListener() {
                    @Override
                    public void onSuccessDownload(ResponseBody responseBody) {
                        resultData.setResultInfo(info);
                        InputStream is = responseBody.byteStream();
                        Bitmap resultBitmap =  BitmapFactory.decodeStream(is);
                        resultData.setResultImg(resultBitmap);
                        Log.d(TAG, "onSuccessDownload: download bitmap");
                        saveDetectionResult();
                        uiHandler.post(successDetectionRunnable);
                    }

                    @Override
                    public void onErrorDownload(int errorCode) {
                        Log.d(TAG, "onErrorDownload: ");
                        Looper.prepare();
                        Toast.makeText(context,"无法获取到链接中的图片",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
                imageDownloadTask.run();
            }

            @Override
            public void onErrorUpload(int errorCode) {
                Log.d(TAG, "onErrorUpload: image upload failed!");
                Looper.prepare();
                Toast.makeText(context,"上传失败，请重试",Toast.LENGTH_SHORT).show();
                Looper.loop();
                uiHandler.post(failedDetectionRunnable);
            }
        });

        threadPoolUtil.execute(imageUploadTask);
    }

    public void offlineDetection(Uri imgUri) {
        Bitmap bitmap = getCompressedBitmap(imgUri);
        threadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(bitmap, false);
                drawObjects(objects, bitmap, resultRecordMap);
                saveDetectionResult();
                uiHandler.post(successDetectionRunnable);
            }
        });

    }

    private Bitmap getCompressedBitmap(Uri uri) {

        String storePath = Objects.requireNonNull(context).getExternalCacheDir()
                .getAbsolutePath() + File.separator + "UCropImg";
        File file = new File(storePath + "/original.jpg");
        new CompressHelper.Builder(context)
                .setMaxWidth(720)  // default max width is 720
                .setMaxHeight(960) // default max height is 960
                .setQuality(90)    // default max image quality is 90
                .setFileName("compressed")
                .setCompressFormat(Bitmap.CompressFormat.JPEG) // set default suffix of the image to jpeg
                .setDestinationDirectoryPath(storePath)
                .build()
                .compressToFile(file);
        return BitmapFactory.decodeFile(storePath + "/compressed.jpeg");   //The suffix of the compressed image is jpeg
    }

    private void drawObjects(YoloV5Ncnn.Obj[] objects, Bitmap bitmap,
                             Map<String, Integer> insectsMap) {
        insectsMap.clear();
        StringBuilder info = new StringBuilder();
        if (objects == null)
        {
            info.append("图中未检测到目标");
        }

        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        final int[] colors = new int[] {
                Color.rgb( 54,  67, 244),
                Color.rgb( 99,  30, 233),
                Color.rgb(176,  39, 156),
                Color.rgb(183,  58, 103),
                Color.rgb(181,  81,  63),
                Color.rgb(243, 150,  33),
                Color.rgb(244, 169,   3),
                Color.rgb(212, 188,   0),
                Color.rgb(136, 150,   0),
                Color.rgb( 80, 175,  76),
                Color.rgb( 74, 195, 139),
                Color.rgb( 57, 220, 205),
                Color.rgb( 59, 235, 255),
                Color.rgb(  7, 193, 255),
                Color.rgb(  0, 152, 255),
                Color.rgb( 34,  87, 255),
                Color.rgb( 72,  85, 121),
                Color.rgb(158, 158, 158),
                Color.rgb(139, 125,  96)
        };

        Canvas canvas = new Canvas(rgba);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(26);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++)
        {
            paint.setColor(colors[i % 19]);

            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);

            // draw filled text inside image
            {
                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";

                if(insectsMap.containsKey(objects[i].label)){     //键已经存在了，只要把值加一
                    int numbers = Integer.parseInt(insectsMap.get(objects[i].label).toString());
                    insectsMap.put(objects[i].label, numbers + 1);
                }
                else {        //键不存在 要新建这个键并且初值为1
                    insectsMap.put(objects[i].label, 1);
                }

                float text_width = textpaint.measureText(text);
                float text_height = - textpaint.ascent() + textpaint.descent();

                float x = objects[i].x;
                float y = objects[i].y - text_height;
                if (y < 0)
                    y = 0;
                if (x + text_width > rgba.getWidth())
                    x = rgba.getWidth() - text_width;

                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);

                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
            }
        }
        for (Map.Entry<String, Integer> entry : insectsMap.entrySet()) {
            info.append(entry.getValue()).append("个").append(entry.getKey()).append("\n");
        }
        if (info.length()==0){
            info.append("图中未检测到目标");
        }
        resultData.setResultImg(rgba);

        resultData.setResultInfo(info.toString());
        Log.d(TAG, "drewObjects: " + info.toString());
    }

    private void saveDetectionResult() {
        String dir = Objects.requireNonNull(context).getExternalFilesDir(null).getAbsolutePath() + "/insect";
        Date dd = new Date();
        SimpleDateFormat sim =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sim.format(dd);
        String name = time + ".jpg";
        saveBitmapToLocal(resultData.getResultImg(), dir, name);
        writeResultDataToDB(time, resultData.getResultInfo());
    }

    private void saveBitmapToLocal(Bitmap bitmap, String dir, String name) {
        File appDir = new File(dir);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, name);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            Log.d(TAG, "saveBitmapToLocal: ");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();}
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeResultDataToDB(String time, String info) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("detect_time",time);
        values.put("detect_result", info);
        db.insert("detectedhistory",null,values);
    }


}
