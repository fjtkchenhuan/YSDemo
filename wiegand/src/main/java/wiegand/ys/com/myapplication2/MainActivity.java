package wiegand.ys.com.myapplication2;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.wiegand.Wiegand;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    int data;
    private volatile boolean threadStatus =false;
    Button button,button_output26,button_output34;
    TextView text,edit_query;
    Thread thread;
    int out;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x10:
                    text.setText("输入值为:"+data);
//                    text.setText("输出值为:"+out);
                    break;
                case 0x11:
                    text.setText("");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button_input);
        button_output26 = findViewById(R.id.button_output26);
        button_output34 = findViewById(R.id.button_output34);
        text = findViewById(R.id.text);
        edit_query = findViewById(R.id.edit_query);
//        button.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button.getText().equals("打开韦根输入")) {
                    Log.i(TAG, "inputOpen=" + Wiegand.inputOpen());
                    button.setText("关闭韦根输入");
                    threadStatus=false;
                    thread =new ReadThread();
                    thread.start();
                }else {
                    button.setText("打开韦根输入");
                    threadStatus=true;
//                    Wiegand.inputClose();
                    handler.sendEmptyMessageDelayed(0x11,3000);
                }
            }
        });
        button_output26.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button_output26.getText().equals("打开韦根26输出")){
                    if (edit_query.getText().toString().trim().equals("")){
                        Toast.makeText(MainActivity.this,"请输入有效数值",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG,"outputOpen="+Wiegand.outputOpen());
                    long value = Integer.parseInt(edit_query.getText().toString().trim());
                    Log.i(TAG,"value="+value);
                    int out = Wiegand.Output26(value);
                    Log.i(TAG,"out=="+out);
                    button_output26.setText("关闭韦根26输出");
                    text.setText("输出值为:"+out);
                }else {
                    Wiegand.outputClose();
                    button_output26.setText("打开韦根26输出");
                    text.setText("");
                }

//                if (threadStatus==false){
//                    Wiegand.inputClose();
//                    threadStatus =true;
//                    text.setText("输入值为:");
//                    button.setText("打开韦根输入");
//                }else {
//                    Wiegand.inputOpen();
//                    new ReadThread().start();
//                    threadStatus=false;
//                    button.setText("关闭韦根输入");
//                }
            }
        });
        button_output34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button_output34.getText().equals("打开韦根34输出")){
                    if (edit_query.getText().toString().trim().equals("")){
                        Toast.makeText(MainActivity.this,"请输入有效数值",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG,"outputOpen="+Wiegand.outputOpen());
                    long value = Integer.parseInt(edit_query.getText().toString().trim());
                    Log.i(TAG,"value="+value);
                    int out = Wiegand.Output34(value);
                    Log.i(TAG,"out=="+out);
                    button_output34.setText("关闭韦根34输出");
                    text.setText("输出值为:"+out);
                }else {
                    Wiegand.outputClose();
                    button_output34.setText("打开韦根34输出");
                    text.setText("");
                }

//                if (threadStatus==false){
//                    Wiegand.inputClose();
//                    threadStatus =true;
//                    text.setText("输入值为:");
//                    button.setText("打开韦根输入");
//                }else {
//                    Wiegand.inputOpen();
//                    new ReadThread().start();
//                    threadStatus=false;
//                    button.setText("关闭韦根输入");
//                }
            }
        });

    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!isInterrupted()){
                try {
                    Thread.sleep(1000);
                    data=Wiegand.inputRead();
                    Log.i(TAG,"data="+data);
                    String str = Integer.toHexString(data);
                    data = (int) (Long.parseLong(str,16));
                    Log.i(TAG,"10str="+Long.parseLong(str,16));
                    handler.sendEmptyMessage(0x10);
//                     out = Wiegand.readoutputWrite34();
//                    Wiegand.inputClose();
//                    Wiegand.outputClose();

                } catch (Exception e) {
                    Log.e(TAG, "run: 数据读取异常：" +e.toString());
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        threadStatus=true;
//        Wiegand.inputClose();
//        Wiegand.outputClose();
    }
}