package com.mogujie.tt.utils;

/**
 * Created by zhujian on 15/1/14.
 */

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * 获取屏幕,分辨率相关
 */
public class ScreenUtil {
    private Context mCtx;
    private static ScreenUtil mScreenTools;

    public static ScreenUtil instance(Context ctx){
        if(null == mScreenTools){
            mScreenTools = new ScreenUtil(ctx);
        }
        return mScreenTools;
    }

    private ScreenUtil(Context ctx){
        mCtx = ctx.getApplicationContext();
    }

    public int getScreenWidth(){
        return mCtx.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    public int dip2px(int dip){
        float density = getDensity(mCtx);
        return (int)(dip * density + 0.5);
    }

    public int px2dip(int px){
        float density = getDensity(mCtx);
        return (int)((px - 0.5) / density);
    }

    private  float getDensity(Context ctx){
        return ctx.getResources().getDisplayMetrics().density;
    }

    /**
     * ５40 的分辨率上是85 （
     * @return
     */
    public int getScal(){
        return (int)(getScreenWidth() * 100 / 480);
    }

    /**
     * 宽全屏, 根据当前分辨率　动态获取高度
     * height 在８００*４８０情况下　的高度
     * @return
     */
    public int get480Height(int height480){
        int width = getScreenWidth();
        return (height480 * width / 480);
    }

    /**
     * 获取状态栏高度
     * @return
     */
    public int getStatusBarHeight(){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = mCtx.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    public int getScreenHeight(){
        return mCtx.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getPxByDp(float dp,Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}