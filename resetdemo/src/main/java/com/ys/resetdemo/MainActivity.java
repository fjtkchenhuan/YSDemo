package com.ys.resetdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int device;
    private EditText etEthWeb;
    private Button ethSwitchBtn;
    private Button ethWebsiteBtn;
    private TextView tvEth;
    private EditText etWifiWeb;
    private Button wifiSwitchBtn;
    private Button wifiWebsiteBtn;
    private TextView tvWifi;
    private EditText etPhoneWeb;
    private Button phoneSwitchBtn;
    private Button phoneWebsiteBtn;
    private TextView tvPhone;
    private EditText etCheckTime;
    private Button checkTimeBtn;
    private TextView tvCheckTime;
    private EditText etUsbReset;
    private Button usbResetBtn;
    private TextView tvUsbReset;

    private static final String ETH_RESET_PROPERTY = "persist.sys.eth_fix";
    private static final String WIFI_RESET_PROPERTY = "persist.sys.wifi_fix";
    private static final String PHONE_RESET_PROPERTY = "persist.sys.phone_fix";
    private static final String ETH_WEBSITE_PROPERTY = "persist.sys.eth_website";
    private static final String WIFI_WEBSITE_PROPERTY = "persist.sys.wifi_website";
    private static final String PHONE_WEBSITE_PROPERTY = "persist.sys.phone_website";
    private static final String CHECK_TIME_PROPERTY = "persist.sys.checktime";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        GpioUtils.upgradeRootPermissionForExport();
    }

    private void initView() {
        etEthWeb = findViewById(R.id.eth_website);
        ethSwitchBtn = findViewById(R.id.eth_reset_switch);
        ethWebsiteBtn = findViewById(R.id.set_eth_website);
        tvEth = findViewById(R.id.eth_current_status);

        etWifiWeb = findViewById(R.id.wifi_website);
        wifiSwitchBtn = findViewById(R.id.wifi_reset_switch);
        wifiWebsiteBtn = findViewById(R.id.set_wifi_website);
        tvWifi = findViewById(R.id.wifi_current_status);

        etPhoneWeb = findViewById(R.id.phone_website);
        phoneSwitchBtn = findViewById(R.id.phone_reset_switch);
        phoneWebsiteBtn = findViewById(R.id.set_phone_website);
        tvPhone = findViewById(R.id.phone_current_status);

        etCheckTime = findViewById(R.id.check_net_gap);
        checkTimeBtn = findViewById(R.id.set_gap);
        tvCheckTime = findViewById(R.id.current_check_time);

        etUsbReset = findViewById(R.id.usb_index);
        tvUsbReset = findViewById(R.id.reset_usb_status);
        usbResetBtn = findViewById(R.id.reset_usb);

        ethSwitchBtn.setOnClickListener(this);
        ethWebsiteBtn.setOnClickListener(this);
        wifiSwitchBtn.setOnClickListener(this);
        wifiWebsiteBtn.setOnClickListener(this);
        phoneSwitchBtn.setOnClickListener(this);
        phoneWebsiteBtn.setOnClickListener(this);
        checkTimeBtn.setOnClickListener(this);
        usbResetBtn.setOnClickListener(this);

        updateEthStatus();
        updatePhoneStatus();
        updateWifiStatus();
        updateCheckTime();

        RadioGroup radioGroup = findViewById(R.id.rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rk3288_7_1:
                        device = 1;
                        break;
                    case R.id.rk3368_7_1:
                        device = 2;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.eth_reset_switch:
                String ethSwitch = Utils.getValueFromProp(ETH_RESET_PROPERTY);
                if ("true".equals(ethSwitch))
                    Utils.setValueToProp(ETH_RESET_PROPERTY,"false");
                if ("false".equals(ethSwitch))
                    Utils.setValueToProp(ETH_RESET_PROPERTY,"true");
                updateEthStatus();
                break;
            case R.id.set_eth_website:
                String ethWeb = etEthWeb.getText().toString();
                Utils.setValueToProp(ETH_WEBSITE_PROPERTY,ethWeb);
                updateEthStatus();
                break;
            case R.id.wifi_reset_switch:
                String wifiSwitch = Utils.getValueFromProp(WIFI_RESET_PROPERTY);
                if ("true".equals(wifiSwitch))
                    Utils.setValueToProp(WIFI_RESET_PROPERTY,"false");
                if ("false".equals(wifiSwitch))
                    Utils.setValueToProp(WIFI_RESET_PROPERTY,"true");
                updateWifiStatus();
                break;
            case R.id.set_wifi_website:
                String wifiWeb = etWifiWeb.getText().toString();
                Utils.setValueToProp(WIFI_WEBSITE_PROPERTY,wifiWeb);
                updateWifiStatus();
                break;
            case R.id.phone_reset_switch:
                String phoneSwitch = Utils.getValueFromProp(PHONE_RESET_PROPERTY);
                if ("true".equals(phoneSwitch))
                    Utils.setValueToProp(PHONE_RESET_PROPERTY,"false");
                if ("false".equals(phoneSwitch))
                    Utils.setValueToProp(PHONE_RESET_PROPERTY,"true");
                updatePhoneStatus();
                break;
            case R.id.set_phone_website:
                String phoneWeb = etPhoneWeb.getText().toString();
                Utils.setValueToProp(PHONE_WEBSITE_PROPERTY,phoneWeb);
                updatePhoneStatus();
                break;
            case R.id.set_gap:
                String time = etCheckTime.getText().toString();
                Utils.setValueToProp(CHECK_TIME_PROPERTY,time);
                updateCheckTime();
                break;
            case R.id.reset_usb:
                tvUsbReset.setText("");
                String indexText = etUsbReset.getText().toString();
                int index;
                if (!"".equals(indexText)) {
                    index = Integer.parseInt(indexText);
                    if (GpioUtils.exportGpio(index)) {
                        GpioUtils.upgradeRootPermissionForGpio(index);
                        String status = GpioUtils.getGpioDirection(index);
                        if ("".equals(status)) {
                            tvUsbReset.setText("该索引值无效");
                            return;
                        } else {
                           String ioValue = GpioUtils.getGpioValue(index);
                           if ("0".equals(ioValue)) {
                               if (GpioUtils.writeGpioValue(index,"1"))
                                   tvUsbReset.setText("正在复位中...");

                               try {
                                   Thread.sleep(2000);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }

                               if (GpioUtils.writeGpioValue(index,"0"))
                                   tvUsbReset.setText("复位成功");
                           } else if ("1".equals(ioValue)) {
                               if (GpioUtils.writeGpioValue(index,"0"))
                                   tvUsbReset.setText("正在复位中...");

                               try {
                                   Thread.sleep(2000);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }

                               if (GpioUtils.writeGpioValue(index,"1"))
                                   tvUsbReset.setText("复位成功");
                           }
                        }
                    }
                }
                break;
            default:
                break;
        }

    }

    private void updateEthStatus() {
        String ethWebsite = Utils.getValueFromProp(ETH_WEBSITE_PROPERTY);
        String ethSwitch = Utils.getValueFromProp(ETH_RESET_PROPERTY);
        if ("true".equals(ethSwitch))
            tvEth.setText("当前以太网复位打开，复位检查地址是"+ethWebsite);
        if ("false".equals(ethSwitch))
            tvEth.setText("当前以太网复位关闭，复位检查地址是"+ethWebsite);
    }

    private void updateWifiStatus() {
        String wifiWebsite = Utils.getValueFromProp(WIFI_WEBSITE_PROPERTY);
        String wifiSwitch = Utils.getValueFromProp(WIFI_RESET_PROPERTY);
        if ("true".equals(wifiSwitch))
            tvWifi.setText("当前wifi复位打开，复位检查地址是"+wifiWebsite);
        if ("false".equals(wifiSwitch))
            tvWifi.setText("当前wifi复位关闭，复位检查地址是"+wifiWebsite);
    }

    private void updatePhoneStatus() {
        String phoneWebsite = Utils.getValueFromProp(PHONE_WEBSITE_PROPERTY);
        String phoneSwitch = Utils.getValueFromProp(PHONE_RESET_PROPERTY);
        if ("true".equals(phoneSwitch))
            tvPhone.setText("当前4G复位打开，复位检查地址是"+phoneWebsite);
        if ("false".equals(phoneSwitch))
            tvPhone.setText("当前4G复位关闭，复位检查地址是"+phoneWebsite);
    }

    private void updateCheckTime() {
        String time = Utils.getValueFromProp(CHECK_TIME_PROPERTY);
        tvCheckTime.setText("当前网络复位检查网络时间间隔是"+time+"s");
    }
}
