package com.ys.gpiotest;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {
    private static Toast toast;
    /**
     * 解决Toast重复弹出 长时间不消失的问题
     */
    public static void showToast(Context context,String message) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            View view = toast.getView();
            if (view != null) {
                TextView message1 = view.findViewById(android.R.id.message);
                message1.setTextSize(30);
                toast.setGravity(Gravity.CENTER, 0, 0);
            }
        } else {
            toast.cancel();
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
             View view = toast.getView();
            if (view != null) {
                TextView message1 = view.findViewById(android.R.id.message);
                message1.setTextSize(30);
                toast.setGravity(Gravity.CENTER, 0, 0);
            }
        }
        toast.show();//设置新的消息提示
    }

}