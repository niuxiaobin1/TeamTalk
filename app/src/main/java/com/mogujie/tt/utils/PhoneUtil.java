package com.mogujie.tt.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Random;
import java.util.UUID;

public class PhoneUtil {

    private static PhoneUtil instance;

    private TelephonyManager tm;
    private Activity act;

    private PhoneUtil(Activity act) {
        tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
        this.act = act;
    }

    public static PhoneUtil getInstance(Activity act) {
        if (instance == null) {
            instance = new PhoneUtil(act);
        } else if (instance.act != act) {
            instance = new PhoneUtil(act);
        }
        return instance;
    }


    /**
     * 获取手机IMEI
     *
     * @param context
     * @return
     */
    public static final String getIMEI(Context context) {
        try {
            //实例化TelephonyManager对象
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMEI号
            String imei = telephonyManager.getDeviceId();
            //在次做个验证，也不是什么时候都能获取到的啊
            if (imei == null) {
                imei = "";
            }
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 获取手机IMSI
     */
    public static String getIMSI(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMSI号
            String imsi = telephonyManager.getSubscriberId();
            if (null == imsi) {
                imsi = "";
            }
            return imsi;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取手机型号
     */
    public static String getModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机品牌
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机系统版本
     */
    public static String getVersion() {
        return Build.VERSION.RELEASE;
    }


    /**
     * 获取应用包名
     */
    public String getPackageName() {
        return act.getPackageName();
    }


    /**
     * 获取当前本地apk的版本
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }


    /**
     * 生成一个0 到 count 之间的随机数
     *
     * @param endNum
     * @return
     */
    public static int getNum(int endNum) {
        if (endNum > 0) {
            Random random = new Random();
            return random.nextInt(endNum);
        }
        return 0;
    }


    public static  String getMyUUID() {
        UUID uuid = UUID.randomUUID();
        String uniqueId = uuid.toString();
        String uniqueIds = uniqueId.replaceAll("-","");
        return uniqueIds;

    }

    //唯一标志
    public static String getPhoneUniqueID() {
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        return m_szDevIDShort;
    }

    public static String getUUID() {

        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serial = Build.getSerial();
            } else {
                serial = Build.SERIAL;
            }
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }
}
