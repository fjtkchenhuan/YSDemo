package com.ys.twoscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/12.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
          if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
              Intent intent1 = new Intent(context,MainActivity.class);
              intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(intent1);
          }
    }
}
