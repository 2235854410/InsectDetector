/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = getClass().getName();
    private static final String DB_NAME = "insectdetector.db";
    private static final String TABLE_NAME = "detectedhistory";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper instance;
    private SQLiteDatabase database;
    private AtomicInteger openCounter = new AtomicInteger();

    public static DatabaseHelper getInstance(Context context) {
        if(null == instance) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
            }
        }
        return instance;
    }


    private DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //data_time键既是识别的时间也是图片的名字
        String sql = "create table if not exists " + TABLE_NAME + " (detect_time datetime primary key,  detect_result text)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    public synchronized SQLiteDatabase getWritableDB() {
        if(openCounter.incrementAndGet() == 1) {
            database = instance.getWritableDatabase();
            Log.d(TAG, "getWriteableDatabase: ");
        }
        return database;
    }



    public synchronized SQLiteDatabase getReadableDB() {
        if(openCounter.incrementAndGet() == 1) {
            database = getReadableDatabase();
        }
        return database;
    }

    public synchronized void closeDatabase() {
        if(openCounter.decrementAndGet() == 0) {
            database.close();
        }
    }
}
