package com.example.util;

import android.widget.Toast;

public class ToastUtil {
    /**
     * 显示短时间Toast
     * @param msg 显示信息
     */
    public static void showShortToast(String msg){
        Toast.makeText(TApp.mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示长时间Toast
     * @param msg 显示信息
     */
    public static void showLongToast(String msg){
        Toast.makeText(TApp.mContext, msg, Toast.LENGTH_LONG).show();
    }
}
