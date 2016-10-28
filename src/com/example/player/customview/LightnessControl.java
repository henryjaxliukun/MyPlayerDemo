package com.example.player.customview;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings.System;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 屏幕亮度相关方法 需要获取权限
 * <uses-permission android:name="android.permission.WRITE_SETTINGS" />
 *
 * @author makai 2015年2月4日下午1:32:36
 */
public class LightnessControl {
    /**
     * 判断是否开启了自动亮度调节
     *
     * @param act
     * @return
     */
    public static boolean isAutoBrightness(Activity act) {
        boolean automicBrightness = false;
        ContentResolver aContentResolver = act.getContentResolver();
        try {
            automicBrightness = System.getInt(aContentResolver,
                    System.SCREEN_BRIGHTNESS_MODE) == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Exception e) {
            Toast.makeText(act, "无法获取亮度", Toast.LENGTH_SHORT).show();
        }
        return automicBrightness;
    }

    /**
     * 改变亮度
     * @param act
     * @param value
     */
    public static void setLightness(Activity act, int value) {
        try {
            System.putInt(act.getContentResolver(), System.SCREEN_BRIGHTNESS,
                    value);
            WindowManager.LayoutParams lp = act.getWindow().getAttributes();
            lp.screenBrightness = (value <= 0 ? 1 : value) / 255f;
            act.getWindow().setAttributes(lp);
        } catch (Exception e) {
            Toast.makeText(act, "无法改变亮度", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取亮度
     * @param act
     * @return
     */
    public static int getLightness(Activity act) {
        return System.getInt(act.getContentResolver(),
                System.SCREEN_BRIGHTNESS, -1);
    }

    /**
     * 停止自动亮度调节
     * @param activity
     */
    public static void stopAutoBrightness(Activity activity) {
        System.putInt(activity.getContentResolver(),
                System.SCREEN_BRIGHTNESS_MODE,
                System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     * @param activity
     */
    public static void startAutoBrightness(Activity activity) {
        System.putInt(activity.getContentResolver(),
                System.SCREEN_BRIGHTNESS_MODE,
                System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
}