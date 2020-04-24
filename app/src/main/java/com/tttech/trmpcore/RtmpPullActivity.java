package com.tttech.trmpcore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wushuangtech.library.Constants;

import ttt.ijk.media.exo.widget.media.IjkVideoView;

public class RtmpPullActivity extends BaseActivity {

    private FrameLayout mIjkLayout;
    private IjkVideoView mIjkVideoView;
    private TextView mFpsTV, mAudioBitrateTV, mVideoBitrateTV, mVideoDelayTV, mAudioDelayTV;
    private final Object mLock = new Object();

    private String pullUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull);
        Intent intent = getIntent();
        pullUrl = intent.getStringExtra("pullUrl");
        initView();
        startCDNPull();
    }

    private void initView() {
        mIjkLayout = findViewById(R.id.pull_ijk_ly);
        mAudioBitrateTV = findViewById(R.id.pull_audio_down);
        mVideoBitrateTV = findViewById(R.id.pull_video_down);
        mFpsTV = findViewById(R.id.pull_fps_down);
        mAudioDelayTV = findViewById(R.id.pull_audio_delay);
        mVideoDelayTV = findViewById(R.id.pull_video_delay);
        mBaseProgressDialog.setMessage("正在拉流中...");

        Spinner mRenderModeSpinner = findViewById(R.id.pull_render_mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ijk_render_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRenderModeSpinner.setAdapter(adapter);
        mRenderModeSpinner.setSelection(1);
        mRenderModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 默认的显示模式为 TTT_IJK_FIT_PARENT
                switch (position) {
                    case 0:
                        // 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
                        mIjkVideoView.setAspectRatio(Constants.TTT_IJK_ILL_PARENT); // 默认显示模式
                        break;
                    case 1:
                        // 可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
                        mIjkVideoView.setAspectRatio(Constants.TTT_IJK_FIT_PARENT);
                        break;
                    case 2:
                        // 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
                        mIjkVideoView.setAspectRatio(Constants.TTT_IJK_WRAP_CONTENT);
                        break;
                    case 3:
                        // 不剪裁,非等比例拉伸画面填满整个View
                        mIjkVideoView.setAspectRatio(Constants.TTT_IJK_MATCH_PARENT);
                        break;
                    case 4:
                        // 不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
                        mIjkVideoView.setAspectRatio(Constants.TTT_IJK_16_9_FIT_PARENT);
                        break;
                    case 5:
                        // 不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
                        mIjkVideoView.setAspectRatio(Constants.TTT_IJK_4_3_FIT_PARENT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 开始拉流
     */
    private void startCDNPull() {
        mBaseProgressDialog.show();
        // 1.创建 IjkVideoView 视频渲染控件。
        mIjkVideoView = mTTTEngine.CreateIjkRendererView(this);
        ViewGroup vp = (ViewGroup) mIjkVideoView.getParent();
        if (vp != null) {
            vp.removeView(mIjkVideoView);
        }
        // 2.监听拉流中出现的异常错误或主播停止推流，终止当前的拉流。
        // dmeo处理逻辑是结束当前页面，如果只是想重新拉流，可以先调用 stopCDNPull 再 startCDNPull 重新拉流。
        mIjkVideoView.setOnErrorListener((iMediaPlayer, what, extra) -> {
            mBaseProgressDialog.dismiss();
            showErrorExitDialog("拉流出现错误, type-" + what);
            return false;
        });
        // 3.监听拉流成功的回调通知。
        mIjkVideoView.setOnRenderingStart(() -> {
            mBaseProgressDialog.dismiss();
            Toast.makeText(mContext, "拉流成功", Toast.LENGTH_SHORT).show();
        });

        // 4.监听拉流中的音视频下行数据统计信息。
        mIjkVideoView.setOnRtmpPullDataCallBack(bean -> {
            synchronized (mLock) {
                if (mIjkVideoView == null) {
                    return;
                }
                setTextViewContent(mFpsTV, R.string.videoly_fps, String.valueOf(bean.mVideoFps)); // 帧率
                setTextViewContent(mVideoBitrateTV, R.string.videoly_videodown, String.valueOf(bean.mVideoBitrate)); // 视频码率
                setTextViewContent(mAudioBitrateTV, R.string.videoly_audiodown, String.valueOf(bean.mAudioBitrate)); // 音频码率
                setTextViewContent(mVideoDelayTV, R.string.videoly_videodelay, String.valueOf(bean.mVideoDelay)); // 视频延迟
                setTextViewContent(mAudioDelayTV, R.string.videoly_audiodelay, String.valueOf(bean.mAudioDelay)); // 音频延迟
            }
        });

        // 5.开始拉流。第二个参数 true 代表正常拉rtmp到流，false 代表使用ktv模式去拉流，具体ktv详情，请参考 https://github.com/santiyun/Android-KTV
        mTTTEngine.startIjkPlayer(pullUrl, true);
        mIjkLayout.addView(mIjkVideoView);
    }

    /**
     * 停止拉流
     */
    private void stopCDNPull() {
        mIjkLayout.removeAllViews();
        mTTTEngine.stopIjkPlayer();
        if (mIjkVideoView != null) {
            mIjkVideoView.release(true);
            mIjkVideoView = null;
        }

        synchronized (mLock) {
            setTextViewContent(mFpsTV, R.string.videoly_fps, String.valueOf(0));
            setTextViewContent(mVideoBitrateTV, R.string.videoly_videodown, String.valueOf(0));
            setTextViewContent(mAudioBitrateTV, R.string.videoly_audiodown, String.valueOf(0));
            setTextViewContent(mVideoDelayTV, R.string.videoly_videodelay, String.valueOf(0));
            setTextViewContent(mAudioDelayTV, R.string.videoly_audiodelay, String.valueOf(0));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCDNPull();
    }

    @Override
    protected void exitRoom() {
        stopCDNPull();
        setResult(SplashActivity.ACTIVITY_PULL);
        super.exitRoom();
    }

    private void setTextViewContent(TextView textView, int resourceID, String value) {
        runOnUiThread(() -> {
            String string = getResources().getString(resourceID);
            String result = String.format(string, value);
            textView.setText(result);
        });
    }
}
