package com.ys.doublenet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.rkapi.MyManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MyManager manager;
    private TextView txEthIp;
    private TextView txEthStatus;
    private TextView txWifiStatus;
    private TextView txEthPingIp;
    private TextView txWifiPingIp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = MyManager.getInstance(this);
        manager.bindAIDLService(this);

        txEthIp = findViewById(R.id.eth_ip);
        txEthStatus = findViewById(R.id.eth_status);
        txWifiStatus = findViewById(R.id.wifi_status);
        txEthPingIp = findViewById(R.id.ping_eth_ip);
        txWifiPingIp = findViewById(R.id.ping_wifi_ip);

        findViewById(R.id.check_eth).setOnClickListener(this);
        findViewById(R.id.check_wifi).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.check_eth:
                String ethIp = "";
                String gataway = manager.getGateway();
                if ("DHCP".equals(manager.getEthMode()))
                    ethIp = manager.getDhcpIpAddress();
                else if ("StaticIp".equals(manager.getEthMode()))
                    ethIp = manager.getStaticEthIPAddress();
                txEthIp.setText("以太网IP：" + ethIp);

                if (Utils.isEthNetworkAvailable(this)) {
                    if (Utils.isPingSuccess(gataway))
                        txEthStatus.setText("以太网连接正常");
                     else
                        txEthStatus.setText("以太网连接异常");
                } else {
                    Toast.makeText(this,"以太网未连接",Toast.LENGTH_LONG).show();
                    txEthStatus.setText("以太网未连接，请检查网线和开关");
                }

                txEthPingIp.setText("以太网ping的网关是："+gataway);
                break;
            case R.id.check_wifi:
                if (Utils.isWifiNetworkAvailable(this)) {
                    if (Utils.isPingSuccess("www.baidu.com"))
                        txWifiStatus.setText("wifi连接正常");
                    else
                        txWifiStatus.setText("wifi连接异常");
                } else {
                    txWifiStatus.setText("wifi未连接，请先连接好wifi");
                }
                txWifiPingIp.setText("wifi ping的地址是：www.baidu.com");
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        manager.unBindAIDLService(this);
        super.onDestroy();
    }
}
