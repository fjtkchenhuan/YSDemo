package com.ys.ysdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("sky","onCreate");
        handler = new Handler();

        final AlertDialog dialog = AlertDialog.getInstance(this);
        dialog.setTitle(getString(R.string.low_memory_title));
        dialog.setMsg(getString(R.string.low_memory_message));

        dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private Runnable logcatRunnable = new Runnable() {
        @Override
        public void run() {
            WifiUtil.getIns().init(getApplicationContext());
            WifiUtil.getIns().changeToWifi("YiSheng_2.4G","yisheng888");

        }
    };


    private void sendKeyCode2(final int keyCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建一个Instrumentation对象
                    Instrumentation inst = new Instrumentation();
                    // 调用inst对象的按键模拟方法
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //输出具体的信息，例如logcat
    private String getCmdMsg(String command) {
        String msg = "";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/system/xbin/su","-c", command});
            String inputMsg;
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((inputMsg = inputStream.readLine()) != null) {
                msg += inputMsg + "\n";
            }
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean execFor7(String command) {
        Log.d("execFor7","command = " + command);
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String s = command + "\n";
            dataOutputStream.write(s.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            // 读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            Log.d("execFor7", "execFor7 msg is " + msg);
            // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
            if (!msg.contains("Failure")) {
                result = true;
            }
        } catch (Exception e) {
            Log.e("execFor7", e.getMessage(), e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                Log.e("TAG", e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("sky","onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("sky","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("sky","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacks(logcatRunnable);
        Log.d("sky","onDestroy");
    }


}
