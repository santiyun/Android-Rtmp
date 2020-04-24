package com.tttech.trmpcore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tttech.trmpcore.bean.JniObjs;
import com.tttech.trmpcore.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.api.ExternalRtmpPublishModule;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import java.util.Timer;
import java.util.TimerTask;

public class RtmpPushActivity extends BaseActivity implements View.OnClickListener {
    private MyLocalBroadcastReceiver mMyLocalBroadcastReceiver;
    private ViewGroup mPushRoot;
    private Button mPushPause, mPauseAudio;
    private TextView mFpsTV;
    private TextView mAudioBitrateTV;
    private TextView mVideoBitrateTV;
    private boolean mIsPause;
    private boolean mIsAudioPause;
    private boolean mIsDestory;
    private Timer mTimer = null;

    private String pushUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);
        Intent intent = getIntent();
        pushUrl = intent.getStringExtra("pushUrl");
        initView();
        initTTTRtcEngine();
        openLocalVideo();
        startRtmpPush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBaseProgressDialog != null) {
            mBaseProgressDialog.dismiss();
            mBaseProgressDialog = null;
        }

        try {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMyLocalBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    private void initView() {
        mPushPause = findViewById(R.id.push_pause);
        mPushPause.setOnClickListener(this);
        mPauseAudio = findViewById(R.id.push_pause_audio);
        mPauseAudio.setOnClickListener(this);
        mPushRoot = findViewById(R.id.push_root);
        TextView mPushUrlTV = findViewById(R.id.push_url);
        mAudioBitrateTV = findViewById(R.id.pull_audio_down);
        mVideoBitrateTV = findViewById(R.id.pull_video_down);
        mFpsTV = findViewById(R.id.pull_fps_down);
        mBaseProgressDialog.setMessage("正在推流中...");
        mPushUrlTV.setText(pushUrl);
    }

    private void initTTTRtcEngine() {
        mMyLocalBroadcastReceiver = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        mContext.registerReceiver(mMyLocalBroadcastReceiver, filter);
        mTTTEngine.enableVideo();
    }

    private void openLocalVideo() {
        SurfaceView surfaceView = TTTRtcEngine.CreateRendererView(this);
        mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN, surfaceView), getRequestedOrientation());
        mPushRoot.addView(surfaceView);
        mTTTEngine.startPreview();
    }

    private void startRtmpPush() {
        mBaseProgressDialog.show();
        mTTTEngine.startRtmpPublish(pushUrl);
    }

    private void initTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                ExternalRtmpPublishModule.RtmpPushStatistics rtmpPushStatus = ExternalRtmpPublishModule.getInstance().getRtmpPushStatus();
                setTextViewContent(mFpsTV, R.string.videoly_fps, String.valueOf(rtmpPushStatus.mFps));
                setTextViewContent(mVideoBitrateTV, R.string.videoly_push_videoups, String.valueOf(rtmpPushStatus.mVideoRealBitrate));
                setTextViewContent(mAudioBitrateTV, R.string.videoly_push_audioup, String.valueOf(rtmpPushStatus.mAudioRealBitrate));
            }
        }, 1000, 1000);
    }

    private void setTextViewContent(TextView textView, int resourceID, String value) {
        runOnUiThread(() -> {
            String string = getResources().getString(resourceID);
            String result = String.format(string, value);
            textView.setText(result);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.push_pause:
                if (!mIsPause) {
                    mTTTEngine.pauseRtmpPublish();
                    mPushPause.setText("恢复推流");
                } else {
                    mTTTEngine.resumeRtmpPublish();
                    mPushPause.setText("暂停推流");
                }
                mIsPause = !mIsPause;
                break;
            case R.id.push_pause_audio:
                if (!mIsAudioPause) {
                    mTTTEngine.getTTTRtcEngineExtend().muteRtmpPublishAudioStream(true);
                    mPauseAudio.setText("取消静音");
                } else {
                    mTTTEngine.getTTTRtcEngineExtend().muteRtmpPublishAudioStream(false);
                    mPauseAudio.setText("静音");
                }
                mIsAudioPause = !mIsAudioPause;
                break;
        }
    }

    @Override
    protected void exitRoom() {
        mIsDestory = true;
        mTTTEngine.stopRtmpPublish();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        setResult(SplashActivity.ACTIVITY_PUSH);
        super.exitRoom();
    }

    class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = (JniObjs) intent.getSerializableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                if (mJniObjs == null) {
                    return;
                }
                mBaseProgressDialog.dismiss();
                if (mJniObjs.mJniType == LocalConstans.CALL_BACK_ON_RTMP_PUSH_STATE) {
                    int Type = mJniObjs.mErrorType;
                    String toastmsg = "";
                    if (Type == Constants.RTMP_PUSH_STATE_INITERROR) {
                        toastmsg = "推流初始化失败";
                    } else if (Type == Constants.RTMP_PUSH_STATE_OPENERROR) {
                        toastmsg = "推流启动失败";
                    } else if (Type == Constants.RTMP_PUSH_STATE_LINKFAILED) {
                        toastmsg = "推流发送失败";
                    } else if (Type == Constants.RTMP_PUSH_STATE_LINKSUCCESSED) {
                        Toast.makeText(mContext, "推流成功", Toast.LENGTH_SHORT).show();
                    }

                    if (toastmsg.length() > 0 && !mIsDestory) {
                        showErrorExitDialog(toastmsg);
                    } else {
                        initTimer();
                    }
                }
            }
        }
    }
}
