package com.kavin.location;

import android.app.Application;
import android.util.Log;

public class MyApp extends Application {

    private String diskCacheDir;
    public static MyApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        diskCacheDir = FileUtils.getDiskCacheDir(app);
        Log.e("---------TAG", "diskCacheDir: " + diskCacheDir);

        FileUtils.copyFileAsync(app.getApplicationContext(), "style.data");
        FileUtils.copyFileAsync(app.getApplicationContext(), "style_extra.data");

    }


}
