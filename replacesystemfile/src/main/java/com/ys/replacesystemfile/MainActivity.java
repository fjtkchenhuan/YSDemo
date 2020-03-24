package com.ys.replacesystemfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Handler handler;
    private FileUtils fileUtils;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        handler = new Handler(new Callback(this));
        filePath = Environment.getExternalStorageDirectory() + File.separator;
        Log.d("ddd", "filePath = " + filePath);
        fileUtils = new FileUtils(this, "YSReceiver.apk", filePath);
        fileUtils.copy();

        String[] commands;
        commands = new String[6];
        commands[0] = "mount -o rw,remount -t ext4 /system";//"mount -o rw,remount /system";
        commands[1] = "rm -rf /system/app/YSReceiver/YSReceiver.apk";
        commands[2] = "cp  "+ filePath + "YSReceiver.apk" + " /system/app/YSReceiver/YSReceiver.apk";
        commands[3] = "chmod 755 /system/app/YSReceiver/YSReceiver.apk";
        commands[4] = "sync";
        commands[5] = "mount -o ro,remount -t ext4 /system";
        for (int i = 0; i < commands.length; i++) {
            Log.d("ddd", "execCmd3(commands[i]) = " + commands[i]);
            RootCmd.execFor7(commands[i]);
        }

        Toast.makeText(this, "replace YSReceiver", Toast.LENGTH_LONG).show();
        handler.postDelayed(uninstallAPP, 5000);
    }

    private Runnable uninstallAPP = new Runnable() {
        @Override
        public void run() {
            Log.d("ddd","delete = " + fileUtils.deleteFile(filePath + "YSReceiver.apk"));
            uninstallApk("com.ys.replacesystemfile");
        }
    };

    public void uninstallApk(String packageName) {
        Log.d("ddd","packageName = " + packageName);
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

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
