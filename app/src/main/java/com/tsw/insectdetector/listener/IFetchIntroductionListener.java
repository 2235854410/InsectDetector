package com.tsw.insectdetector.listener;

import okhttp3.Response;

public interface IFetchIntroductionListener {
    void onStartFetchIntroduction();
    void onSuccessFetchIntroduction(Response response);
    void onFailureFetchIntroduction(int errorCode);
}
