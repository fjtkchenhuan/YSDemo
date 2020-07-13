package com.ys.stressapptest2g;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public static void setValueToProp(String key, String val) {
        Class<?> classType;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getDeclaredMethod("set", new Class[]{String.class, String.class});
            method.invoke(classType, new Object[]{key, val});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getValueFromProp(String key) {
        String value = "";
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            value = (String) getMethod.invoke(classType, new Object[]{key});
        } catch (Exception e) {
        }
//        Log.i("yuanhang",value);
        return value;
    }

    public static void writeStringFileFor7(File file, String content) {
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


    public static void write2File(File file,String mode) {
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


}  