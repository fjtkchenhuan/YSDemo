package ys.com.linkwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

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

public class BootAndMountReceiver extends BroadcastReceiver {
    private static final String TAG = BootAndMountReceiver.class.getName();
    private String ssid = "ruiyixin-main";
    private String password = "ruiyixin888";
    private Context mContext;
    List<ScanResult> scanResults;

    private static WifiManager mWifiManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        Log.i("MyReceiver", action);
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            scanResults = WIFIConnectionManager.getInstance(mContext).getWifiManager().getScanResults();
            for (int i = 0; i < scanResults.size(); i++) {
                Log.e(TAG, "scanResults:----" + (scanResults.get(i)).SSID);
            }
            if (!WIFIConnectionManager.getInstance(mContext).isConnected(ssid)) {
                WIFIConnectionManager.getInstance(mContext).connect(ssid, password);
            }
        } else if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
            String path = intent.getData().getPath();
            if (!TextUtils.isEmpty(path)) {
                showDirectory(new File(path));
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
                                for (WifiInfo wifiInfo : list) {
                                    ssid = wifiInfo.getWifi();
                                    password = wifiInfo.getPwd();
                                    WIFIAutoConnectionService.start(mContext, ssid, password);
//                                    if (TextUtils.isEmpty(wifiInfo.getPwd())&&TextUtils.isEmpty(wifiInfo.getWifi())){
//                                        configWifiInfo(context,wifiInfo.getWifi(),wifiInfo.getPwd(),getType(mWifiManager.getScanResults().get()))
//                                    }
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


    public static WifiConfiguration configWifiInfo(Context context, String SSID, String password, int type) {
        WifiConfiguration config = null;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig == null) continue;
                if (existingConfig.SSID.equals("\"" + SSID + "\"")  /*&&  existingConfig.preSharedKey.equals("\""  +  password  +  "\"")*/) {
                    config = existingConfig;
                    break;
                }
            }
        }
        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 分为三种情况：0没有密码1用wep加密2用wpa加密
        if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == 1) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {   // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 获取热点的加密类型
     */
    private int getType(ScanResult scanResult) {
        int type;
        if (scanResult.capabilities.contains("WPA"))
            type = 2;
        else if (scanResult.capabilities.contains("WEP"))
            type = 1;
        else
            type = 0;
        return type;
    }

}
