/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector.task;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.util.Log;

import com.tsw.insectdetector.DatabaseHelper;
import com.tsw.insectdetector.pojo.DetectionResultData;
import com.tsw.insectdetector.listener.IDataFetchListener;

import java.util.List;

public class DBReadTask implements Runnable{
    private final String TAG = getClass().getName();
    private Context context;
    private IDataFetchListener dataFetchListener;
    private List<DetectionResultData> resultDataList;

    public DBReadTask(Context context, IDataFetchListener dataFetchListener, List<DetectionResultData> list) {
        this.context = context;
        this.dataFetchListener = dataFetchListener;
        resultDataList = list;
    }

    @Override
    public void run() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = databaseHelper.getReadableDB();
        String sql = "select * from detectedhistory order by detect_time desc";
        Cursor cursor = db.rawQuery(sql, null);//query("history",null,null,null,null,null,null);

        if(cursor.getCount() == 0){
            Log.d(TAG, "no history");
        }


        if(cursor.moveToFirst()){
            do{
                String time = cursor.getString(cursor.getColumnIndex("detect_time"));
                String info = cursor.getString(cursor.getColumnIndex("detect_result"));
                DetectionResultData data = new DetectionResultData();
                data.setDetectionTime(time);
                data.setResultInfo(info);
                resultDataList.add(data);
                Log.d(TAG, "run: " + data.getDetectionTime());
            }while (cursor.moveToNext());
        }
        Looper.prepare();
        dataFetchListener.onDataReady();
        Looper.loop();

    }
}
