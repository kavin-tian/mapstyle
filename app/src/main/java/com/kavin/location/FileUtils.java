package com.kavin.location;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {


    /**
     * 把assets目录下的文件考到缓存目录下
     */
    public static void copyFile(Context context, String filename) {

        try {
            //String filename = "amap/style.data";
            String outFile_temporary = getDiskCacheDir(context) + File.separator + filename + ".temp";
            String outFile = getDiskCacheDir(context) + File.separator + filename;

            boolean exists = new File(outFile).exists();
            if (exists) {
                return;
            }

            byte[] buf = new byte[1024];
            InputStream inputStream = context.getAssets().open(filename);
            FileOutputStream outputStream = new FileOutputStream(outFile_temporary);

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            inputStream.close();
            outputStream.close();

            File file = new File(outFile_temporary);
            file.renameTo(new File(outFile));

        } catch (IOException e) {
            Log.e("------FileUtils", "拷贝文件失败，copyFile: ", e);
        }

    }

    /**
     * 异步拷贝
     */
    public static void copyFileAsync(Context context, String filename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.copyFile(context, filename);
            }
        }).start();

    }

    /**
     * 当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，否则就调用getCacheDir()方法来获取缓存路径。
     * 前者获取到的就是 /sdcard/Android/data/<application package>/cache 这个路径，而后者获取到的是 /data/data/<application package>/cache 这个路径。
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

}
