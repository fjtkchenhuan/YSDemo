package com.ys.memtester2g;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

public final class RootCmd {
    // 执行linux命令并且输出结果
    protected static String execRootCmd(String paramString) {
        String result = "result : ";
        try {
            Process localProcess = Runtime.getRuntime().exec("su ");// 经过Root处理的android系统即有su命令
            OutputStream localOutputStream = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream(
                    localOutputStream);
            InputStream localInputStream = localProcess.getInputStream();
            DataInputStream localDataInputStream = new DataInputStream(
                    localInputStream);
            String str1 = String.valueOf(paramString);
            String str2 = str1 + "\n";
            localDataOutputStream.writeBytes(str2);
            localDataOutputStream.flush();
            String str3 = null;
            while ((str3 = localDataInputStream.readLine()) != null) {
                Log.d("result", str3);
                result += str3;
            }
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            return result;
        } catch (Exception localException) {
            localException.printStackTrace();
            return result;
        }
    }

    // 执行linux命令但不关注结果输出
    protected static int execRootCmdSilent(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream(
                    (OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            int result = localProcess.exitValue();
            return (Integer) result;
        } catch (Exception localException) {
            localException.printStackTrace();
            return -1;
        }
    }

    // 判断机器Android是否已经root，即是否获取root权限
    protected static boolean haveRoot() {
        int i = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
        if (i != -1) {
            return true;
        }
        return false;
    }

    public static boolean execFor7(String command) {
        Log.d("execFor7", "command = " + command);
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

    //输出具体的信息，例如logcat
    static String getCmdMsg(String command) {
        String msg = "";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/system/xbin/su","-c", command});
            String inputMsg;
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((inputMsg = inputStream.readLine()) != null) {
                msg += inputMsg + "\n";
            }
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}