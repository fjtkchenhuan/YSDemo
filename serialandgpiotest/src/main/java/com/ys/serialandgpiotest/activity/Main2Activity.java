package com.ys.serialandgpiotest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.serialandgpiotest.R;
import com.ys.serialandgpiotest.Service.MyService;
import com.ys.serialandgpiotest.util.GpioUtils;

import java.io.File;

public class Main2Activity extends Activity  {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_main);
        textView = findViewById(R.id.text);

        Intent intent = getIntent();
        String value = intent.getStringExtra("io3Value");
        if (value != null && !"".equals(value)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("io3的电压是：" + value);
            Toast.makeText(this,"io3的电压是" + value,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent intent1 = getIntent();
        String value = intent1.getStringExtra("io3Value");
        if (value != null && !"".equals(value)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("io3的电压是：" + value);
            Toast.makeText(this,"io3的电压是" + value,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
