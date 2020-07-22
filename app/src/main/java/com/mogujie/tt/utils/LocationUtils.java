package com.mogujie.tt.utils;


import com.mogujie.tt.app.IMApplication;

/**
 * Author: Leo
 * Date: 2/26/2020
 * Describe:
 **/
public class LocationUtils {
    private static LocationUtils sInstance;
    private String location = "Location...";

    private LocationUtils() {
    }

    public void startLocate() {
       GPSUtils.getInstance(IMApplication.sApplicationContext).getLocation();
    }

    public static LocationUtils getInstance() {
        if (null == sInstance) {
            synchronized (LocationUtils.class) {
                if (null == sInstance) {
                    sInstance = new LocationUtils();
                }
            }
            return sInstance;
        }

        return sInstance;
    }


    public String getLatitude() {
        if (GPSUtils.getInstance(IMApplication.sApplicationContext).getLocation()==null) return "3.3869949";
        return GPSUtils.getInstance(IMApplication.sApplicationContext).getLocation().getLatitude()+"";
    }

    public String getLongitude() {
        if (GPSUtils.getInstance(IMApplication.sApplicationContext).getLocation()==null) return "6.5167863";
        return GPSUtils.getInstance(IMApplication.sApplicationContext).getLocation().getLongitude()+"";
    }
}
