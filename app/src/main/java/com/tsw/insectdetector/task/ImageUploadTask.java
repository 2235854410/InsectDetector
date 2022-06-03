/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector.task;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.tsw.insectdetector.tool.NetworkUtil;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ImageUploadTask implements Runnable{
    private final String TAG = getClass().getName();
    private IUploadListener uploadListener;
    private Bitmap bitmap;

    public ImageUploadTask(Bitmap bitmap, IUploadListener iUploadListener) {
        this.bitmap = bitmap;
        uploadListener = iUploadListener;
    }

    @Override
    public void run() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 97, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody
                .Builder()
                .add("img",base64)
                .build();
        Request request = new Request
                .Builder()
                .url(NetworkUtil.HTTP_SERVER + "getDetect")
                .post(requestBody)
                .build();
        try {
            Response response = client
                    .newCall(request)
                    .execute();
            if(null != response.body()) {
                uploadListener.onSuccessUpload(response.body());
            } else {
                uploadListener.onErrorUpload(0);
                Log.d(TAG, "run: responseBody == null");
            }

        } catch (IOException |JSONException e) {
            e.printStackTrace();
            uploadListener.onErrorUpload(0);
        }
    }

    public interface IUploadListener {
        void onSuccessUpload(ResponseBody responseBody) throws IOException, JSONException;
        void onErrorUpload(int errorCode);
    }
}
