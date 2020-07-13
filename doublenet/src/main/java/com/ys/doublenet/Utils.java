package com.ys.doublenet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isWifiNetworkAvailable(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEthNetworkAvailable(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ethNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (ethNetInfo != null && ethNetInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean is4GAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static void Write2File(File file,String mode) {
     //Log.d(TAG,"Write2File,write mode = "+mode);
     if((file == null) || (!file.exists()) || (mode == null)) return ;

     try {
         FileOutputStream fout = new FileOutputStream(file);
         PrintWriter pWriter = new PrintWriter(fout);
         pWriter.println(mode);
         pWriter.flush();
         pWriter.close();
         fout.close();
     } catch(IOException re) {
        return;
     }
 }

    public static boolean isPingSuccess(String web) {
        Log.d("sky","web = " + web);
        String result = "ok";
        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 " + web);
            int status = p.waitFor();
            if (status == 0) {
                return true;
            } else {
                result = ">>fail1";
            }
        } catch (IOException e) {
            result = ">>fail2";
        } catch (InterruptedException e) {
            result = ">>fail3";
        } finally {
            Log.d("harris", "isPingSuccess result = " + result);
        }
        return false;
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

    public static void sync() {
        try {
            Process process = Runtime.getRuntime().exec("sync");
            process.waitFor();
        } catch (Exception e) {
            Log.e("exect", e.getMessage(), e);
        } finally {

        }
    }

    public static void runSystemCommand(String command) {
        try {
            final Process process = Runtime.getRuntime().exec(new String[]{"/system/xbin/su","-c", command});
            int exitVal = process.waitFor();
            Log.d("harris", "exitVal " + exitVal);
        } catch (Exception e) {
            Log.d("harris", e.getMessage(), e);
        } finally {
        }
    }

    public static boolean isIPAvailable(String addr){
        //首先对长度进行判断
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)){
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        if (ipAddress == true){
            String ips[] = addr.split("\\.");
            if(ips.length == 4){
                try{
                    for(String ip : ips){
                        if(Integer.parseInt(ip) < 0 || Integer.parseInt(ip) > 255){
                            ipAddress = false;
                        }
                    }
                }catch (Exception e){
                    ipAddress = false;
                }
                ipAddress = true;
            }else{
                ipAddress = false;
            }
        }
        return ipAddress;
    }
}
