package com.ys.gpiotest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
                if (!"".equals(indexText)) {
                    index = Integer.parseInt(indexText);
                    if (GpioUtils.exportGpio(index)) {
                        GpioUtils.upgradeRootPermissionForGpio(index);
                        String status = GpioUtils.getGpioDirection(index);
                        if ("".equals(status))
                            inValidText.setText(getText(R.string.invalid_gpio));
                        else
                            inValidText.setText(getText(R.string.valid_gpio));
                    }
                }
                break;
            case R.id.get_io_status:
                ToastUtils.showToast(this, getString(R.string.current_io_type)+ ": "+GpioUtils.getGpioDirection(index));
                break;
            case R.id.set_input:
                if (GpioUtils.setGpioDirection(index, 1))
                    ToastUtils.showToast(this, getString(R.string.success_set_io_in));
                break;
            case R.id.set_output:
                if (GpioUtils.setGpioDirection(index, 0))
                    ToastUtils.showToast(this, getString(R.string.success_set_io_out));
                break;
            case R.id.get_io_value:
                ToastUtils.showToast(this, getString(R.string.current_io_volt) + GpioUtils.getGpioValue(index));
                break;
            case R.id.set_highvalue:
                if (GpioUtils.writeGpioValue(index, "1"))
                    ToastUtils.showToast(this, getString(R.string.success_set_io_high));
                break;
            case R.id.set_lowvalue:
                if (GpioUtils.writeGpioValue(index, "0"))
                    ToastUtils.showToast(this, getString(R.string.success_set_io_low));
                break;
            default:
                break;
        }

    }
}

