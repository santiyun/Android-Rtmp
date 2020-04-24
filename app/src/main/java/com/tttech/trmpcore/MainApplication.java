package com.tttech.trmpcore;

import android.app.Application;

import com.tttech.trmpcore.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.TTTRtcEngine;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        TTTRtcEngine mTTTRtmpEngine = TTTRtcEngine.create(getApplicationContext(), "", mMyTTTRtcEngineEventHandler);
        if (mTTTRtmpEngine == null) {
            System.exit(0);
        }
    }
}
