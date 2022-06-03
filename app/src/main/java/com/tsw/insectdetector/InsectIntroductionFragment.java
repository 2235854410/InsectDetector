/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.tsw.insectdetector.pojo.InsectIntroductionData;
import com.tsw.insectdetector.task.GetStringTask;
import com.tsw.insectdetector.tool.NetworkUtil;
import com.tsw.insectdetector.tool.ThreadPoolUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;


public class InsectIntroductionFragment extends Fragment {

    private final String TAG = getClass().getName();
    private AVLoadingIndicatorView loadingAnim;
    private ChinaMapFragment chinaMapFragment;
    private InsectIntroductionAdapter introductionAdapter;
    private Spinner insectOrderSpinner, insectFamilySpinner;
    private ArrayList<InsectIntroductionData> introductionDataList = new ArrayList<>();
    private final String[] insectOrderList = {"选择目", "全部目", "半翅目","鳞翅目","鞘翅目"};
    private final String[][] AllInsectFamily = {
            {"选择科", "蝉科","蝽科","兜蝽科", "蛾蜡蝉科", "负子蝽科", "广蜡蝉科",
                    "蜡蝉科", "珠蚧科", "网蝽科", "象蜡蝉科","叶蝉科", "缘蝽科", "斑蝶科", "尺蛾科", "刺蛾科"
                    , "大蚕蛾科", "灯蛾科", "毒蛾科", "粉蝶科", "凤蝶科", "钩蛾科", "蛀果蛾科", "蛱蝶科"
                    , "枯叶蛾科", "螟蛾科", "天蛾科", "夜蛾科", "舟蛾科", "花金龟科", "吉丁甲科", "金龟科"
                    , "天牛科", "锹甲科", "叶甲科"},
            {"选择科", "蝉科","蝽科","兜蝽科", "蛾蜡蝉科", "负子蝽科", "广蜡蝉科",
                    "蜡蝉科", "珠蚧科", "网蝽科", "象蜡蝉科","叶蝉科", "缘蝽科"},
            {"选择科", "斑蝶科", "尺蛾科", "刺蛾科", "大蚕蛾科", "灯蛾科", "毒蛾科", "粉蝶科",
                    "凤蝶科", "钩蛾科", "蛀果蛾科", "蛱蝶科", "枯叶蛾科", "螟蛾科",
                    "天蛾科", "夜蛾科", "舟蛾科"},
            {"选择科", "花金龟科", "吉丁甲科", "金龟科", "天牛科", "锹甲科", "叶甲科"}
    };
    private String[] insectFamilyList = AllInsectFamily[0];
    private final ThreadPoolUtil threadPoolUtil = ThreadPoolUtil.getInstance();
    private final String url = NetworkUtil.HTTP_SERVER + "handbook";
    private final String name = "family";
    private final Handler uiHandler = new Handler();
    private TextView emptyPage, offlineSignal;
    private Button refreshBtn;
    private final GetStringTask getStringTask = new GetStringTask(url, name, "all", new GetStringTask.IGetStringListener() {
        @Override
        public void onSuccessGet(ResponseBody responseBody) throws IOException, JSONException {
            disposeIntroductionString(responseBody.string());
        }
        @Override
        public void onErrorGet(int errorCode) {

        }
    });



    public InsectIntroductionFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insect_introduction, container, false);
        loadingAnim = view.findViewById(R.id.loading_anim);
        loadingAnim.hide();
        refreshBtn = view.findViewById(R.id.refresh_introduction_btn);
        Button chinaMap = view.findViewById(R.id.china_map);
        emptyPage = view.findViewById(R.id.empty_introduction);
        offlineSignal = view.findViewById(R.id.offline_signal);
        insectOrderSpinner = view.findViewById(R.id.order_spinner);
        insectFamilySpinner = view.findViewById(R.id.family_spinner);
        introductionAdapter = new InsectIntroductionAdapter(getActivity(), getContext(), introductionDataList);
        RecyclerView recyclerView = view.findViewById(R.id.introduction_recycler);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(introductionAdapter);
        chinaMapFragment = (ChinaMapFragment) getFragmentManager().findFragmentByTag("ChinaMapFragment");
        initializePage();
        refreshBtn.setOnClickListener((v) -> {
            if(NetworkUtil.checkNetworkConnectState(getContext())) {
                threadPoolUtil.execute(getStringTask);
                showOfflineIcon(false);
            }
        });
        initializeSpinner();
        if(null == chinaMapFragment) {
            chinaMapFragment = new ChinaMapFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, chinaMapFragment, "ChinaMapFragment")
                    .commit();
            getFragmentManager()
                    .beginTransaction()
                    .hide(chinaMapFragment)
                    .commit();
        }
        chinaMap.setOnClickListener((v) -> {
            getFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .commit();
            getFragmentManager()
                    .beginTransaction()
                    .show(chinaMapFragment)
                    .commit();
        });
        return view;
    }

    private void initializePage() {
        if(!NetworkUtil.checkNetworkConnectState(getContext())) {
            showOfflineIcon(true);
        } else {
            showOfflineIcon(false);
            threadPoolUtil.execute(getStringTask);
        }
    }

    private void initializeSpinner() {
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<String>(getContext()
                , R.layout.item_spinner_selected, insectOrderList);
        orderAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        insectOrderSpinner.setAdapter(orderAdapter);
        ArrayAdapter<String> familyAdapter = new ArrayAdapter<>(getContext()
                , R.layout.item_spinner_selected, insectFamilyList);
        familyAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        insectFamilySpinner.setAdapter(familyAdapter);
        insectOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0) {
                    insectFamilyList = AllInsectFamily[i - 1];
                    ArrayAdapter<String> newFamilyAdapter = new ArrayAdapter<>(getContext()
                            , R.layout.item_spinner_selected, insectFamilyList);
                    newFamilyAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                    insectFamilySpinner.setAdapter(newFamilyAdapter);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        insectFamilySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if( i != 0) {
                    //content = insectFamilyList[i];
                    //Log.d(TAG, "onItemSelected: " + content);
                    GetStringTask task = new GetStringTask(url, name, insectFamilyList[i]
                            , new GetStringTask.IGetStringListener() {
                        @Override
                        public void onSuccessGet(ResponseBody responseBody) throws IOException, JSONException {
                            disposeIntroductionString(responseBody.string());
                        }

                        @Override
                        public void onErrorGet(int errorCode) {

                        }
                    });
                    threadPoolUtil.execute(task);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void disposeIntroductionString(String result) throws JSONException {
        //String result = responseBody.string();
        introductionDataList.clear();
        if( !"null".equalsIgnoreCase(result)) {
            JSONArray jsonArray = new JSONArray(result);
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    InsectIntroductionData data = new InsectIntroductionData();
                    data.setName(jsonObject.getString("name"));
                    String imgUrl = jsonObject.getString("img_path");
                    data.setImgUrl(imgUrl);
                    data.setIntroduction(jsonObject.getString("info"));
                    introductionDataList.add(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadingAnim.hide();
                emptyPage.setVisibility("null".equalsIgnoreCase(result) ? View.VISIBLE : View.INVISIBLE);
                introductionAdapter.notifyDataSetChanged();
            }
        };
        uiHandler.post(runnable);
    }

    private void showOfflineIcon(boolean show) {
        offlineSignal.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        refreshBtn.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }


}