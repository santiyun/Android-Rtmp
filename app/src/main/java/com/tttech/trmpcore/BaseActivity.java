package com.tttech.trmpcore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.wushuangtech.wstechapi.TTTRtcEngine;

/**
 * Created by wangzhiguo on 17/10/12.
 */

public abstract class BaseActivity extends Activity {

    protected TTTRtcEngine mTTTEngine;
    protected Context mContext;

    protected AlertDialog mBaseErrorExitDialog;
    protected ProgressDialog mBaseProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //获取上下文
        mContext = this;
        //获取SDK实例对象
        mTTTEngine = TTTRtcEngine.getInstance();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initDialog();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("退出房间提示")
                .setCancelable(false)
                .setPositiveButton("确定", (dialog, which) -> {
                    exitRoom();
                });
        mBaseErrorExitDialog = builder.create();

        // 创建dialog
        mBaseProgressDialog = new ProgressDialog(this);
        mBaseProgressDialog.setCancelable(false);
    }

    public void showErrorExitDialog(String message) {
        if (mBaseErrorExitDialog != null && mBaseErrorExitDialog.isShowing()) {
            return;
        }

        if (!TextUtils.isEmpty(message)) {
            String msg = "退出原因 : " + message;
            mBaseErrorExitDialog.setMessage(msg);//设置显示的内容
            mBaseErrorExitDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        exitRoom();
    }

    protected void exitRoom() {
        finish();
    }
}
