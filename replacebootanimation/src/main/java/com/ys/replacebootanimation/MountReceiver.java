package com.ys.replacebootanimation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class MountReceiver extends BroadcastReceiver {

    private static final String BOOTANIMATION_TXT = "bootanimation.zip";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
            String path = intent.getData().getPath();
            Log.d("sky","path="+path);

            if (path != null && !TextUtils.isEmpty(path) && !path.contains("/storage/emulated/")) {
                File paths = new File(path);
                doActionWithTestTxt(paths);
            }
        }
    }

    private void doActionWithTestTxt(File file) {
        File[] files = file.listFiles();

        if (files != null && files.length > 0) {
            for (File file1 : files) {
                if (file1.getAbsolutePath().contains(BOOTANIMATION_TXT)) {
                    String[] commands;
                    commands = new String[7];
                    commands[0] = "mount -o rw,remount -t ext4 /system";//"mount -o rw,remount /system";
                    commands[1] = "rm -rf /system/media/bootanimation.zip";
                    commands[2] = "cp  "+ file1.getAbsolutePath() + " /system/media/bootanimation.zip";
                    commands[3] = "chmod 755 /system/media/bootanimation.zip";
                    commands[4] = "sync";
                    commands[5] = "mount -o ro,remount -t ext4 /system";
                    commands[6] = "reboot";
                    for (int i = 0; i < commands.length; i++) {
//                        Log.d("ddd", "execCmd3(commands[i]) = " + commands[i]);
                        RootCmd.execFor7(commands[i]);
                    }
                }

//                if (file1.isDirectory())
//                    doActionWithTestTxt(file1);
            }
        }
    }
}
