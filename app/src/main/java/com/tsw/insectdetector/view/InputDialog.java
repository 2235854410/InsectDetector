package com.tsw.insectdetector.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tsw.insectdetector.R;

public class InputDialog extends AlertDialog implements View.OnClickListener {
    EditText inputText;
    Button cancelBtn, submitBtn;
    Context mContext;
    InputCallback callBack;

    public InputDialog(Context context, InputCallback callBack) {
        super(context, R.style.CustomDialog);
        mContext = context;
        this.callBack = callBack;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);
        inputText = (EditText) findViewById(R.id.et_url);
        //保证EditText能弹出键盘
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        this.setCancelable(false);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);
        submitBtn = (Button) findViewById(R.id.btn_submit);
        submitBtn.setOnClickListener(this);
        this.setCanceledOnTouchOutside(true);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            case R.id.btn_submit:
                if (TextUtils.isEmpty(inputText.getText())) {
                    Toast.makeText(mContext, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    this.dismiss();
                    callBack.onInputSubmit(inputText.getText().toString() );

                }
                break;
            default:
                break;
        }
    }

    public interface InputCallback {
        void onInputSubmit(String input);
    }

}