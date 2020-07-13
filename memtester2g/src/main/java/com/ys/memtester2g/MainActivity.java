package com.ys.memtester2g;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ys.rkapi.MyManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FileUtils fileUtils;
    private String filePath;
    private TextView textView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyManager myManager = MyManager.getInstance(this);
        myManager.setADBOpen(true);
        handler = new Handler(new Callback(this));

        textView = findViewById(R.id.text);
        textView.setText("正在执行命令");
        filePath = Environment.getExternalStorageDirectory() + File.separator;
        fileUtils = new FileUtils(this, "memtester64bit", filePath);
        fileUtils.copy();

        fileUtils = new FileUtils(this, "stressapptest_64bit", filePath);
        fileUtils.copy();

        fileUtils = new FileUtils(this, "ddr_freq_scan.sh", filePath);
        fileUtils.copy();

        List<String> flags = new ArrayList<>();
        String[] commands;
        commands = new String[11];
        commands[0] = "mount -o rw,remount -t ext4 /system";
        commands[1] = "sleep 5";
        commands[2] = "cp  " + filePath + "memtester_64bit" + " /data/memtester";
        commands[3] = "cp  " + filePath + "stressapptest_64bit" + " /data/stressapptest";
        commands[4] = "chmod 755 /data/memtester";
        commands[5] = "chmod 755 /data/stressapptest";
        commands[6] = "cp  " + filePath + "ddr_freq_scan.sh" + " /data/ddr_freq_scan.sh";
        commands[7] = "chmod 755 /data/ddr_freq_scan.sh";
        commands[8] = "sync";
        commands[9] = "/data/memtester 256m > /sdcard/ddr_memteste_log.txt &";
        commands[10] = "mount -o ro,remount -t ext4 /system";
        for (int i = 0; i < commands.length; i++) {
            boolean isSuccess = RootCmd.execFor7(commands[i]);
            Log.d("sky", "isSuccess = " + isSuccess);
            flags.add(isSuccess + "");
        }

        if (flags.size() == 11) {
            for (String s : flags)
                if (!s.equals("true")) {
                    textView.setText("命令执行失败！！！");
                    break;
                } else
                    textView.setText("命令执行成功！！！");

        } else
            textView.setText("命令执行失败！！！");

        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("package:" + "com.ys.memtester2g");
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        handler.postDelayed(deleteFile, 5000);
    }

    private Runnable deleteFile = new Runnable() {
        @Override
        public void run() {
            File file1 = new File(filePath + "memtester_64bit");
            if (file1.exists())
                Log.d("sky", "delete1 = " + fileUtils.deleteFile(filePath + "memtester_64bit"));
            File file2 = new File(filePath + "stressapptest_64bit");
            if (file2.exists())
                Log.d("sky", "delete2 = " + fileUtils.deleteFile(filePath + "stressapptest_64bit"));
            File file3 = new File(filePath + "ddr_freq_scan.sh");
            if (file3.exists())
                Log.d("sky", "delete3 = " + fileUtils.deleteFile(filePath + "ddr_freq_scan.sh"));


            final String s = RootCmd.getCmdMsg("cat /sdcard/ddr_memteste_log.txt | grep FAILURE");//FAILURE
            Log.d("sky","ssss = " + s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if ("".equals(s))
                        textView.setText("暂时没有错误信息");
                    else {
                        textView.setText(s);
                        handler.removeCallbacks(SwitchRedAndGreen1);
                        handler.removeCallbacks(SwitchRedAndGreen);
                        handler.postDelayed(SwitchRedAndGreen,1000);
                    }
                }
            });

            handler.postDelayed(deleteFile, 20 * 60 *1000);
        }
    };

    private Runnable SwitchRedAndGreen = new Runnable() {
        @Override
        public void run() {
            FileUtils.write2File(new File("/sys/devices/platform/misc_power_en/green_led"),"1");
            FileUtils.write2File(new File("/sys/devices/platform/misc_power_en/red_led"),"1");
            handler.postDelayed(SwitchRedAndGreen1,1000);
        }
    };

    private Runnable SwitchRedAndGreen1 = new Runnable() {
        @Override
        public void run() {
//            Log.d("sky","SwitchRedAndGreen1");
            FileUtils.write2File(new File("/sys/devices/platform/misc_power_en/green_led"),"0");
            FileUtils.write2File(new File("/sys/devices/platform/misc_power_en/red_led"),"0");
            handler.postDelayed(SwitchRedAndGreen,1000);
        }
    };


    private static class Callback implements Handler.Callback {
        WeakReference<MainActivity> reference;

        private Callback(MainActivity bootReceiver) {
            reference = new WeakReference<MainActivity>(bootReceiver);
        }

        @Override
        public boolean handleMessage(Message msg) {
            MainActivity bootReceiver = reference.get();
            return bootReceiver != null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacks(SwitchRedAndGreen);
//        handler.removeCallbacks(SwitchRedAndGreen1);
//        handler.removeCallbacks(deleteFile);
//        handler.removeCallbacksAndMessages(null);
    }
}
