package com.ys.gpiotest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText gpioIndex;
    private TextView inValidText;

    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GpioUtils.upgradeRootPermissionForExport();

        gpioIndex = findViewById(R.id.gpio_index);
        inValidText = findViewById(R.id.invalid);

        findViewById(R.id.get_io_status).setOnClickListener(this);
        findViewById(R.id.set_input).setOnClickListener(this);
        findViewById(R.id.set_output).setOnClickListener(this);
        findViewById(R.id.get_io_value).setOnClickListener(this);
        findViewById(R.id.set_highvalue).setOnClickListener(this);
        findViewById(R.id.set_lowvalue).setOnClickListener(this);
        findViewById(R.id.checkio).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkio:
                String indexText = gpioIndex.getText().toString();
                if ( !"".equals(indexText)) {
                    index = Integer.parseInt(indexText);
                    if (GpioUtils.exportGpio(index)) {
                        GpioUtils.upgradeRootPermissionForGpio(index);
                        String status = GpioUtils.getGpioDirection(index);
                        if ("".equals(status))
                            inValidText.setText("无效的GPIO");
                        else
                            inValidText.setText("有效的GPIO");
                    }
                }
                break;
            case R.id.get_io_status:
                Toast.makeText(this,"当前io的类型 = " + GpioUtils.getGpioDirection(index),Toast.LENGTH_LONG).show();
                break;
            case R.id.set_input:
                if (GpioUtils.setGpioDirection(index, 1))
                    Toast.makeText(this,"成功设置该io为输入口",Toast.LENGTH_LONG).show();
                break;
            case R.id.set_output:
                if (GpioUtils.setGpioDirection(index, 0))
                    Toast.makeText(this,"成功设置该io为输出口",Toast.LENGTH_LONG).show();
                break;
            case R.id.get_io_value:
                Toast.makeText(this,"当前io的电平 = " + GpioUtils.getGpioValue(index),Toast.LENGTH_SHORT).show();
                break;
            case R.id.set_highvalue:
                if (GpioUtils.writeGpioValue(index,"1"))
                    Toast.makeText(this,"成功设置该io高电平",Toast.LENGTH_SHORT).show();
                break;
            case R.id.set_lowvalue:
                if (GpioUtils.writeGpioValue(index,"0"))
                    Toast.makeText(this,"成功设置该io低电平",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

    }
}

