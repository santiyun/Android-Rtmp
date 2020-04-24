package com.tttech.trmpcore;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;

public class SplashActivity extends BaseActivity implements View.OnClickListener {

    public static final int ACTIVITY_PUSH = 1;
    public static final int ACTIVITY_PULL = 2;

    private String[] mApplyPermissions = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };

    private EditText mRtmpPullEt, mRtmpPushEt;

    private ProgressDialog mDialog;
    public boolean mIsLoging;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;
        // 权限申请
        AndPermission.with(this)
                .permission(mApplyPermissions)
                .onGranted(permissions -> {
                    if (mApplyPermissions.length != permissions.size()) {
                        finish();
                    }
                }).start();
        initViews();
    }

    private void initViews() {
        mRtmpPullEt = findViewById(R.id.rtmp_pull_et);
        findViewById(R.id.rtmp_pull_bt).setOnClickListener(this);
        mRtmpPushEt = findViewById(R.id.rtmp_push_et);
        findViewById(R.id.rtmp_push_bt).setOnClickListener(this);

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setTitle("");
        mDialog.setMessage("正在跳转界面中...");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_PUSH:
            case ACTIVITY_PULL:
                mIsLoging = false;
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.rtmp_push_bt == v.getId()) {
            String pushTarget = mRtmpPushEt.getText().toString();
            if (TextUtils.isEmpty(pushTarget)) {
                Toast.makeText(mContext, "推流地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsLoging) return;
            mIsLoging = true;
            mDialog.show();

            Intent i = new Intent(mContext, RtmpPushActivity.class);
            i.putExtra("pushUrl", pushTarget);
            startActivityForResult(i, ACTIVITY_PUSH);
        } else {
            String pullTarget = mRtmpPullEt.getText().toString();
            if (TextUtils.isEmpty(pullTarget)) {
                Toast.makeText(mContext, "拉流地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsLoging) return;
            mIsLoging = true;
            mDialog.show();

            Intent i = new Intent(mContext, RtmpPullActivity.class);
            i.putExtra("pullUrl", pullTarget);
            startActivityForResult(i, ACTIVITY_PULL);
        }
    }
}
