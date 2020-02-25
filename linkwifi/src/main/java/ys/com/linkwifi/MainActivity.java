package ys.com.linkwifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String ssid = "YiSheng_2.4G";
    private String password = "yisheng888";
    private List<ScanResult> scanResults;
    private TextView text ;
    private MyReceiver myReceiver;
    static final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CHANGE_WIFI_STATE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT>22){
            checkPermission();
        }
        text = findViewById(R.id.text);
        myReceiver = new MyReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        iFilter.setPriority(1000);
        iFilter.addDataScheme("file");
        registerReceiver(myReceiver,iFilter);
        IntentFilter iFilter1 = new IntentFilter();
        iFilter1.addAction("android.net.wifi.RSSI_CHANGED");
        iFilter1.addAction("android.net.wifi.STATE_CHANGE");
        iFilter1.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        iFilter1.setPriority(1000);
        registerReceiver(myReceiver,iFilter1);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasUnCkeck = false;
            for (int i = 0; i < PERMISSION_LIST.length; i++) {
                if (checkSelfPermission(PERMISSION_LIST[i]) != PackageManager.PERMISSION_GRANTED)
                    hasUnCkeck = true;
            }
            if (hasUnCkeck)
                requestPermissions(PERMISSION_LIST, 300);
        }
    }

    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("MyReceiver", action);
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                scanResults =  WIFIConnectionManager.getInstance(MainActivity.this).getWifiManager().getScanResults();
                for (int i = 0 ; i < scanResults.size();i++) {
                    Log.e(TAG,"scanResults:----"+(scanResults.get(i)).SSID);
                }
                if (!WIFIConnectionManager.getInstance(MainActivity.this).isConnected(ssid)) {
                    WIFIConnectionManager.getInstance(MainActivity.this).connect(ssid, password);
                }else
                    text.setText("wifi已连上");
            }else if (action.equals("android.intent.action.MEDIA_MOUNTED")){
                String path = intent.getData().getPath();
                if (!TextUtils.isEmpty(path)) {
                    showDirectory(new File(path));
                }
            }
        }
    }

    public void showDirectory(File file) {
        final File[] files = file.listFiles();
        for (final File a : files) {
            if (a.getAbsolutePath().contains("wifi.xml")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream inputStream = new FileInputStream(a);
                            try {
                                List<WifiInfo> list = dom2xml(inputStream);
                                for (WifiInfo wifiInfo: list){
                                    ssid = wifiInfo.getWifi();
                                    password = wifiInfo.getPwd();
                                    WIFIAutoConnectionService.start(MainActivity.this,ssid,password);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }


    public List<WifiInfo> dom2xml(InputStream is) throws Exception {
        //一系列的初始化
        List<WifiInfo> list = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        //获得Document对象
        Document document = builder.parse(is);
        //获得WifiInfo的List
        NodeList WifiInfoList = document.getElementsByTagName("item");
        //遍历WifiInfo标签
        for (int i = 0; i < WifiInfoList.getLength(); i++) {
            //获得WifiInfo标签
            Node node_WifiInfo = WifiInfoList.item(i);
            //获得WifiInfo标签里面的标签
            NodeList childNodes = node_WifiInfo.getChildNodes();
            //新建WifiInfo对象
            WifiInfo WifiInfo = new WifiInfo();
            //遍历WifiInfo标签里面的标签
            for (int j = 0; j < childNodes.getLength(); j++) {
                //获得name和nickName标签
                Node childNode = childNodes.item(j);
                //判断是name还是nickName
                if ("wifi".equals(childNode.getNodeName())) {
                    String name = childNode.getTextContent();
                    WifiInfo.setWifi(name);
//                    //获取name的属性
//                    NamedNodeMap nnm = childNode.getAttributes();
//                    //获取sex属性，由于只有一个属性，所以取0
//                    Node n = nnm.item(0);
//                    WifiInfo.s(n.getTextContent());
                } else if ("pwd".equals(childNode.getNodeName())) {
                    String nickName = childNode.getTextContent();
                    WifiInfo.setPwd(nickName);
                }
            }
            //加到List中
            list.add(WifiInfo);
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }
}
