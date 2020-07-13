package com.ys.stressapptest2g;

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
    private Handler handler;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyManager myManager = MyManager.getInstance(this);
        handler = new Handler(new Callback(this));

        textView = findViewById(R.id.text);
        textView.setText("正在执行命令");
        myManager.setADBOpen(true);
        filePath = Environment.getExternalStorageDirectory() + File.separator;
        fileUtils = new FileUtils(this, "memtester_64bit", filePath);
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
        commands[2] = "cp  "+ filePath + "memtester_64bit" + " /data/memtester";
        commands[3] = "cp  "+ filePath + "stressapptest_64bit" + " /data/stressapptest";
        commands[4] = "chmod 755 /data/memtester";
        commands[5] = "chmod 755 /data/stressapptest";
        commands[6] = "cp  "+ filePath + "ddr_freq_scan.sh" + " /data/ddr_freq_scan.sh";
        commands[7] = "chmod 755 /data/ddr_freq_scan.sh";
        commands[8] = "sync";
         // "/data/stressapptest -s 43200 -i 4 -C 4 -W --stop_on_errors -M 512 > /sdcard/ddr_stressapptest_log.txt &"
        // "/data/stressapptest -s 43200 -i 4 -C 4 -W --stop_on_errors -M 256 > /sdcard/ddr_stressapptest_log.txt &"
        commands[9] = "/data/stressapptest -s 43200 -i 4 -C 4 -W --stop_on_errors -M 256 > /sdcard/ddr_stressapptest_log.txt &";
        commands[10] = "mount -o ro,remount -t ext4 /system";
        for (int i = 0; i < commands.length; i++) {
//            Log.d("ddd", "execCmd3(commands[i]) = " + commands[i]);
            boolean isSuccess = RootCmd.execFor7(commands[i]);
            Log.d("sky","isSuccess = " + isSuccess);
            flags.add(isSuccess+"");
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
                Uri uri = Uri.parse("package:" + "com.ys.stressapptest2g");
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        handler.postDelayed(deleteFile,10000);
    }

    private Runnable deleteFile = new Runnable() {
        @Override
        public void run() {
            Log.d("sky","delete1 = " + fileUtils.deleteFile(filePath + "memtester_64bit"));
            Log.d("sky","delete2 = " + fileUtils.deleteFile(filePath + "stressapptest_64bit"));
            Log.d("sky","delete3 = " + fileUtils.deleteFile(filePath + "ddr_freq_scan.sh"));

            handler.postDelayed(showFinishMsg,45000 * 1000);
        }
    };

    private Runnable showFinishMsg = new Runnable() {
        @Override
        public void run() {
            final String s = RootCmd.getCmdMsg("cat /sdcard/ddr_stressapptest_log.txt | grep FAIL");//FAILURE
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if ("".equals(s))
                        textView.setText("Status: PASS");
                    else {
                        textView.setText(s);
                        handler.removeCallbacks(SwitchRedAndGreen1);
                        handler.removeCallbacks(SwitchRedAndGreen);
                        handler.postDelayed(SwitchRedAndGreen,1000);
                    }
                }
            });
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
}
