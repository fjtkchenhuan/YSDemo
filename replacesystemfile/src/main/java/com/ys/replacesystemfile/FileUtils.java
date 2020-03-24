package com.ys.replacesystemfile;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    private Context context;
    String fileName;
    String filePath;
  
    /**  
     *   
     * @param context  
     * @param fileName  
     *            fileName(assets文件夹下压缩文件):文件名称+后缀  
     * @param filePath  
     *            sd本地路径  
     */  
    public FileUtils(Context context, String fileName, String filePath) {
        this.context = context;  
        this.fileName = fileName;  
        this.filePath = filePath;  
    }  
  
    public void copy() {  
        InputStream inputStream;
        try {  
            inputStream = context.getResources().getAssets().open(fileName);// assets文件夹下的文件  
            File file = new File(filePath);
            if (!file.exists()) {  
                file.mkdirs();  
            }  
            FileOutputStream fileOutputStream = new FileOutputStream(filePath + "/" + fileName);// 保存到本地的文件夹下的文件
            byte[] buffer = new byte[1024];  
            int count = 0;  
            while ((count = inputStream.read(buffer)) > 0) {  
                fileOutputStream.write(buffer, 0, count);  
            }  
            fileOutputStream.flush();  
            fileOutputStream.close();  
            inputStream.close();  
        } catch (IOException e) {
            e.printStackTrace();  
        }  
    }

     boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete())
                return true;
             else
                return false;
            }
         else
            return false;
    }
}  