package com.ys.serialsendandaccept;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    AlertDialog mDevicesDialog;
    AlertDialog mRateDialog;

    Button deviceButton;
    Button rateButton;

    String curDevice;
    int curRate;

    public static String[] RATES = new String[]{"0", "50", "75", "110", "134", "150", "200", "300",
            "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400",
            "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000",
            "2500000", "3000000", "3500000", "4000000"};

    public static String[] getDevices() {
        String result = FileUtil.exec("find /dev/ -name \"tty*\"");
        if (TextUtils.isEmpty(result)) return null;
        return result.split(" ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceButton = findViewById(R.id.serialport_device);
        deviceButton.setOnClickListener(this);
        rateButton = findViewById(R.id.serialport_rate);
        rateButton.setOnClickListener(this);

       init();
    }

    protected void init() {
        rateButton.setText(RATES[0]);
        deviceButton.setText("/dev/ttyS3");
        ratesDialog();
        devicesDialog();
    }

    private void ratesDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(RATES, checkRateItem(RATES, curRate),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curRate = Integer.parseInt(RATES[which]);
                            rateButton.setText(RATES[which]);
                            mRateDialog.dismiss();
                        }
                    });
            mRateDialog = builder.create();
    }

    private void devicesDialog() {
            final String[] devices = getDevices();
            if (devices == null || devices.length == 0) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(devices, checkDeviceItem(devices, curDevice),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curDevice = devices[which];
                            deviceButton.setText(curDevice);
                            mDevicesDialog.dismiss();
                        }
                    });
            mDevicesDialog = builder.create();
    }

    private int checkDeviceItem(String[] devices, String curDevice) {
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].equals(curDevice)) return i;
        }
        return 0;
    }

    private int checkRateItem(String[] RATES, int curRate) {
        for (int i = 0; i < RATES.length; i++) {
            if (RATES[i].equals(String.valueOf(curRate))) return i;
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.serialport_device) {
            if (mDevicesDialog != null) mDevicesDialog.show();
        } else if (id == R.id.serialport_rate) {
            if (mRateDialog != null) mRateDialog.show();
        }
    }
}
