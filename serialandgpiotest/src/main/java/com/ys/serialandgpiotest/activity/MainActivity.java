package com.ys.serialandgpiotest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.serialandgpiotest.R;
import com.ys.serialandgpiotest.Service.MyService;
import com.ys.serialandgpiotest.util.GpioUtils;
import com.ys.serialandgpiotest.util.SerialPortUtils;
import com.ys.serialandgpiotest.util.SerialPortUtils1;

import java.io.File;

public class MainActivity extends Activity implements SerialPortUtils.OnDataReceiveListener, SerialPortUtils1.OnDataReceiveListener {

    private TextView textView;
    private Handler handler;
    private SerialPortUtils serialPortUtils1;
    private SerialPortUtils1 serialPortUtils2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        initGpio();

        serialPortUtils1 = new SerialPortUtils("/dev/ttyS3",9600);
        serialPortUtils1.setOnDataReceiveListener(this);
        serialPortUtils1.openSerialPort();

        serialPortUtils2 = new SerialPortUtils1("/dev/ttyS0",9600);
        serialPortUtils2.setOnDataReceiveListener(this);
        serialPortUtils2.openSerialPort();

//        Intent intent1 = new Intent(MainActivity.this, MyService.class);
//        startService(intent1);


        textView = findViewById(R.id.text);
        String io3Value = GpioUtils.getGpioValue(68);
        if ("0".equals(io3Value))
            textView.setText("锁闭合");
        else
            textView.setText("锁开启");
        handler.postDelayed(showIo3, 3000);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPortUtils2.sendSerialPort("11111111");
                }

            }
        });
//        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (int i = 0; i < 100; i++) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    serialPortUtils1.sendSerialPort("22222222");
//                }
//            }
//        });

//        Intent intent = getIntent();
//        String value = intent.getStringExtra("io3Value");
//        if (value != null && !"".equals(value)) {
//
//            textView.setVisibility(View.VISIBLE);
//            textView.setText("io3的电压是：" + value);
//            Toast.makeText(this,"io3的电压是" + value,Toast.LENGTH_SHORT).show();
//        }

    }

    private Runnable showIo3 = new Runnable() {
        @Override
        public void run() {
            String io3Value = GpioUtils.getGpioValue(68);
            if ("0".equals(io3Value)) {
                textView.setText("锁闭合");
//                Toast.makeText(MainActivity.this,"锁闭合",Toast.LENGTH_SHORT).show();
            } else {
                textView.setText("锁开启");
//                Toast.makeText(MainActivity.this,"锁开启",Toast.LENGTH_SHORT).show();
            }
            handler.postDelayed(showIo3, 3000);
        }
    };


    private void initGpio() {
        GpioUtils.upgradeRootPermissionForExport();

        if (!new File("/sys/class/gpio/gpio68/direction").exists()) {
            GpioUtils.exportGpio(68);
            GpioUtils.upgradeRootPermissionForGpio(68);
        }

        if (!new File("/sys/class/gpio/gpio138/direction").exists()) {
            GpioUtils.exportGpio(138);
            GpioUtils.upgradeRootPermissionForGpio(138);
            GpioUtils.setGpioDirection(138, 0);
//            GpioUtils.writeGpioValue(138,"1");
        }

        GpioUtils.upgradeRootPermissionForGpio(170);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataReceive(byte[] buffer, int size) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
