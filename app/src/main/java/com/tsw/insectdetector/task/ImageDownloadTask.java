/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector.task;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ImageDownloadTask implements Runnable{
    private IDownloadListener downloadListener;
    private String imgUrl;

    public ImageDownloadTask(String imageUrl, IDownloadListener iDownloadListener) {
        downloadListener = iDownloadListener;
        this.imgUrl = imageUrl;
    }
    @Override
    public void run() {
        //String  imgUrl = NetworkUtil.HTTP_SEVER + "result/" + imgName;

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request
                    .Builder()
                    .url(imgUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(null != response.body()) {
                downloadListener.onSuccessDownload(response.body());
            } else {
                downloadListener.onErrorDownload(0);
            }
        } catch (IOException| IllegalArgumentException e) {
            e.printStackTrace();
            downloadListener.onErrorDownload(0);   //TODO 链接不合法提醒
        }
    }

    public interface IDownloadListener {
        void onSuccessDownload(ResponseBody body);
        void onErrorDownload(int errorCode);
    }
}
