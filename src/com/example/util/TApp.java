package com.example.util;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class TApp extends Application {

    public static ArrayList<Activity> activities = new ArrayList<>();
    /**
     * 全局上下文对象
     */
    public static TApp mContext;

    /**
     * 退出程序
     */
    public static void appDestroy() {
        for (Activity a : activities) {
            a.finish();
        }
        System.exit(0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    /**
     * 获取应用版本
     *
     * @return
     */
    public static String getAppVersion() {
        String version = "未知版本";
        PackageManager pkm=mContext.getPackageManager();
        try {
            PackageInfo info = pkm.getPackageInfo(mContext.getPackageName(), 0);
            version=info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

}
