package com.tttech.trmpcore.callback;

import android.content.Context;
import android.content.Intent;

import com.tttech.trmpcore.bean.JniObjs;
import com.tttech.trmpcore.utils.MyLog;
import com.wushuangtech.wstechapi.TTTRtcEngineEventHandler;

import static com.tttech.trmpcore.LocalConstans.CALL_BACK_ON_RTMP_PUSH_STATE;


/**
 * Created by wangzhiguo on 17/10/24.
 */

public class MyTTTRtcEngineEventHandler extends TTTRtcEngineEventHandler {

    public static final String TAG = "MyTTTRtcEngineEventHandlers";
    public static final String MSG_TAG = "MyTTTRtcEngineEventHandlerMSG";
    private Context mContext;

    public MyTTTRtcEngineEventHandler(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onStatusOfRtmpPublish(int errorType) {
        MyLog.i("wzg", "onStatusOfRtmpPublish.... errorType : " + errorType);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_RTMP_PUSH_STATE;
        mJniObjs.mErrorType = errorType;
        sendMessage(mJniObjs);
    }

    private void sendMessage(JniObjs mJniObjs) {
        Intent i = new Intent();
        i.setAction(TAG);
        i.putExtra(MSG_TAG, mJniObjs);
        mContext.sendBroadcast(i);
    }
}
