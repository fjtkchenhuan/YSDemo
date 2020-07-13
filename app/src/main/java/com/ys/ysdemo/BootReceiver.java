package com.ys.ysdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("sky","fffffffffffffffff = " + intent.getAction());
        final AlertDialog dialog = AlertDialog.getInstance(context);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Intent intent1 = new Intent(context,MainActivity.class);
//            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent1);
        } else if ("ys.intent.action.SCREEN_ON".equals(intent.getAction())) {
            boolean isShow = intent.getBooleanExtra("isShow",true);
            Log.d("sky","showLowMemoryDialog = " + isShow);
            if (isShow) {
                dialog.setTitle(context.getString(R.string.low_memory_title));
                dialog.setMsg(context.getString(R.string.low_memory_message));
                dialog.setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
               //去掉弹窗
                dialog.dismiss();
            }
        }
    }





}
