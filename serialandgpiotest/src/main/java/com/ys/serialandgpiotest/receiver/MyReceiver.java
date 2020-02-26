package com.ys.serialandgpiotest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ys.serialandgpiotest.Service.MyService;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MyReceiver==",intent.getAction());
        Intent intent1 = new Intent(context, MyService.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intent1);
    }
}