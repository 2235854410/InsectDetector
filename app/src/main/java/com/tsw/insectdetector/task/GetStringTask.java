/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector.task;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetStringTask implements Runnable {
    private String url;
    private String name;
    private String content;
    private boolean haveContent;
    private IGetStringListener iGetStringListener;

    public GetStringTask(String url, IGetStringListener listener) {
        this.url = url;
        iGetStringListener = listener;
        haveContent = false;
    }

    public GetStringTask(String url, String name, String content, IGetStringListener listener) {
        haveContent = true;
        this.url = url;
        this.name = name;
        this.content = content;
        iGetStringListener = listener;
    }

    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient();
        Request request;
        if(haveContent) {
            RequestBody requestBody = new FormBody.Builder().
                    add(name, content).build();
            request = new Request.Builder().url(url)
                    .post(requestBody).build();
        } else {
            request = new Request.Builder().url(url).build();
        }
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            iGetStringListener.onSuccessGet(response.body());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            iGetStringListener.onErrorGet(0);
        }
    }


    public interface IGetStringListener {
        void onSuccessGet(ResponseBody responseBody) throws IOException, JSONException;
        void onErrorGet(int errorCode);
    }
}
