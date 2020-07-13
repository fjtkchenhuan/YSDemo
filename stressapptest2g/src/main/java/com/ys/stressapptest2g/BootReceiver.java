package com.ys.stressapptest2g;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + "ddr_stressapptest_log.txt");
            if (logFile.exists()) {
                Log.d("sky", "delete log = " + logFile.delete());
            }

            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
