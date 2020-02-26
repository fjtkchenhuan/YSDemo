package com.ys.serialandgpiotest.util;


import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FileUtils {
    public static int copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if(!root.exists())
        {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if(!targetDir.exists())
        {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for(int i= 0;i<currentFiles.length;i++) {
            if(currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
            {
                copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");

            }else//如果当前项为文件则进行文件拷贝
            {
                CopySdcardFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
            }
        }
        deleteDirWihtFile(root);
        return 0;
    }


    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static int CopySdcardFile(String fromFile, String toFile) {

        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0)
            {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            File file = new File(fromFile);
            deleteDirWihtFile(file);
            return 0;

        } catch (Exception ex) {
            return -1;
        }
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 追加文件：使用FileWriter
     *
     * @param fileName
     * @param content
     */
    public static void method(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   // 如果是音频文件、图片、歌曲，就用字节流好点；如果是中文（文本）的，用字符流更好；
    public static StringBuilder read(String fileName) {
        FileInputStream fis = null;
        StringBuilder stringBuilder = null;
        byte[] bytes = new byte[1024];
        int count;
        try {
            fis = new FileInputStream(fileName);
            stringBuilder = new StringBuilder();//StringBuilder效率比buffer高，如果多线程需要用buffer
            while ((count = fis.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, count));
            }
            //正常情况下，走到这里就退出，此时finally代码也会执行，但是不会改变返回值。
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            try {
                if(fis != null) {
                    fis.close();//在finally里面关闭流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder;
    }

//    读取文件：这里需要做判断，如果没有这个文件会报错。
    public static String bufferRead(String path) {
        StringBuilder sb = null;
        File file = new File(path);
        if (!file.exists())
            return "";
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(path));
            String line = bfr.readLine();
            sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = bfr.readLine();
            }
            bfr.close();
            Log.d("buffer", "bufferRead: " + sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }



    /**
     * 检测是否在前台
     * @param context
     * @param packageName
     * @return
     */
    private boolean isMyAppRunning(Context context, String packageName) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.equals(packageName)) {
                    int status = runningAppProcessInfo.importance;
                    if (status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
                            || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }
    public static void writeFile(File file, String content) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}