package com.ys.resetdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CameraReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.ys.camera_add".equals(action))
            Toast.makeText(context,"有摄像头接入",Toast.LENGTH_LONG).show();
        else if ("com.ys.camera_remove".equals(action))
            Toast.makeText(context,"有摄像头断开",Toast.LENGTH_LONG).show();
    }
}
