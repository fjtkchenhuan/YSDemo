package com.ys.serialandgpiotest.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.ys.serialandgpiotest.util.GpioUtils;
import com.ys.serialandgpiotest.util.SerialPortUtils;

public class MyService extends Service implements SerialPortUtils.OnDataReceiveListener{
    private SerialPortUtils serialPortUtils;
    private Handler handler;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serialPortUtils = new SerialPortUtils("",9600);
        serialPortUtils.setOnDataReceiveListener(this);
        serialPortUtils.openSerialPort();
        handler = new Handler();
    }

    @Override
    public void onDataReceive(byte[] buffer, int size) {
        String r = "";
        for (int i = 0; i < size; i++  ) {
            String hex = Integer.toHexString(buffer[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0'  + hex;
            }
            r  += hex.toUpperCase();
//            Log.i("onDataReceive",r);
        }
        Log.i("MainActivity",r);

        if (r.length() > 0) {
           //接收到串口数据，读取io3的状态
//            String io3Value = GpioUtils.getGpioValue(68);
//            Log.d("sky","io3Value = " + io3Value);


//            Intent intent = new Intent();
//            intent.putExtra("io3Value",io3Value);
//            intent.setClass(this, Main2Activity.class);
//            startActivity(intent);

            //拉高io2，2s后再拉低
            GpioUtils.writeGpioValue(138,"1");
            GpioUtils.writeGpioValue(170,"1");
            handler.postDelayed(PullUpIo2,3000);
        }
    }

    private Runnable PullUpIo2 = new Runnable() {
        @Override
        public void run() {
            GpioUtils.writeGpioValue(138,"0");
            GpioUtils.writeGpioValue(170,"0");
        }
    };
}
