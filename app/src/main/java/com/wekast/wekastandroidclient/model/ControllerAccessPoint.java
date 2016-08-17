package com.wekast.wekastandroidclient.model;


import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is use to handle all Hotspot related information.
 */
public class ControllerAccessPoint {
    private static final String TAG = "wekastClient";
    private static Method getWifiApState;
    private static Method isWifiApEnabled;
    private static Method setWifiApEnabled;
    private static Method getWifiApConfiguration;

//    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
//    public static final int WIFI_AP_STATE_DISABLED = WifiManager.WIFI_STATE_DISABLED;
//    public static final int WIFI_AP_STATE_DISABLING = WifiManager.WIFI_STATE_DISABLING;
//    public static final int WIFI_AP_STATE_ENABLED = WifiManager.WIFI_STATE_ENABLED;
//    public static final int WIFI_AP_STATE_ENABLING = WifiManager.WIFI_STATE_ENABLING;
//    public static final int WIFI_AP_STATE_FAILED = WifiManager.WIFI_STATE_UNKNOWN;
//    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = WifiManager.EXTRA_PREVIOUS_WIFI_STATE;
//    public static final String EXTRA_WIFI_AP_STATE = WifiManager.EXTRA_WIFI_STATE;

    public WifiManager wifiManager;
    public WifiConfiguration wifiConfig;
//    public ControllerWifi wifiController;

    static {
        // lookup methods and fields not defined publicly in the SDK.
        Class<?> cls = WifiManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.equals("getWifiApState")) {
                getWifiApState = method;
            } else if (methodName.equals("isAccessPointEnabled")) {
                isWifiApEnabled = method;
            } else if (methodName.equals("setAccessPointEnabled")) {
                setWifiApEnabled = method;
            } else if (methodName.equals("getWifiApConfiguration")) {
                getWifiApConfiguration = method;
            }
        }
    }

    public static boolean isApSupported() {
        return (getWifiApState != null && isWifiApEnabled != null
                && setWifiApEnabled != null && getWifiApConfiguration != null);
    }

    public ControllerAccessPoint(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public static ControllerAccessPoint getApControl(WifiManager wifiManager) {
        if (!isApSupported())
            return null;
        return new ControllerAccessPoint(wifiManager);
    }

    public boolean isAccessPointEnabled() {
        try {
//            return (Boolean) isWifiApEnabled.invoke(wifiManager);
            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isAccessPointEnabled");
           return !(Boolean)isWifiApEnabledmethod.invoke(wifiManager);
        } catch (Exception e) {
            Log.d(TAG, "ControllerAccessPoint.isAccessPointEnabled(): " + e.toString(), e); // shouldn't happen
            return false;
        }
    }

    public int getWifiApState() {
        try {
            return (Integer) getWifiApState.invoke(wifiManager);
        } catch (Exception e) {
            Log.d(TAG, "ControllerAccessPoint.getWifiApState(): " + e.toString(), e); // shouldn't happen
            return -1;
        }
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            return (WifiConfiguration) getWifiApConfiguration.invoke(wifiManager);
        } catch (Exception e) {
            Log.d(TAG, "ControllerAccessPoint.getWifiApConfiguration(): " + e.toString(), e); // shouldn't happen
            return null;
        }
    }

    public boolean setAccessPointEnabled(Context context, boolean enabled)  {
        try {
//             boolean curStatus = (Boolean) setWifiApEnabled.invoke(wifiManager, wifiConfig, enabled);
            Method setWifiApMethod = null;
            try {
                setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            boolean curStatus = (Boolean) setWifiApMethod.invoke(wifiManager, wifiConfig, enabled);
            Log.d(TAG, "ControllerAccessPoint.setAccessPointEnabled(): " + curStatus);
            return curStatus;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.d(TAG, "ControllerAccessPoint.setAccessPointEnabled(): IllegalAccessException" + e);
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.d(TAG, "ControllerAccessPoint.setAccessPointEnabled(): InvocationTargetException" + e);
            return false;
        }
//        catch (NoSuchMethodException e) {
//            e.printStackTrace();
//            Log.d(TAG, "ControllerAccessPoint.setAccessPointEnabled(): NoSuchMethodException" + e);
//        }
    }

    /**
     * Function that configuretes WifiConfiguration for connecting to hotspot
     *
     * @return configured WifiConfiguration
     */
    public void configureWifiConfig(String ssid, String pass) {
        wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = ssid;
        wifiConfig.preSharedKey = pass;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        Log.d(TAG, "ControllerAccessPoint.configureWifiConfig():\n" + wifiConfig);
    }

    /**
     * Turn on or off Hotspot.
     *
     * //@param context
     * @param isTurnToOn
     */
//    public void turnOnOffHotspot(Context context, boolean isTurnToOn, ControllerAccessPoint apControl) {
//        Log.d(TAG, "ControllerAccessPoint.turnOnOffHotspot(): started ");
////        apControl = ControllerAccessPoint.getApControl(wifiManager);
//        if (apControl != null) {
//            // Turn off wifi before enable hotspot
//            if (isWifiOn(context)) {
//                turnOnOffWifi(context, false);
//            }
//            apControl.setAccessPointEnabled(context, isTurnToOn);
//        }
//        while(!apControl.isAccessPointEnabled()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    public void turnOnOffHotspot(Context context, boolean isTurnToOn) {
        Log.d(TAG, "ControllerAccessPoint.turnOnOffHotspot(): started ");
        // Turn off wifi before enabling Access Point
//        if (wifiController.isWifiOn(context)) {
//            wifiController.turnOnOffWifi(context, false);
//        }
        setAccessPointEnabled(context, isTurnToOn);
        // Wait till wifi will work
        while(!this.isAccessPointEnabled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "ControllerAccessPoint.turnOnOffAccessPoint():  " + e);
            }
        }
    }

    /**
     * Function check whether wifi is enabled
     *
     * @param context
     * @return
     */
    private boolean isWifiOn(Context context) {
        boolean isWifiOn = wifiManager.isWifiEnabled();
        Log.d(TAG, "MainActivity.isWifiOn(): " + isWifiOn);
        return isWifiOn;
    }

    /**
     * Function turns on or turns off wifi
     *
     * @param context
     * @param b
     */
    private void turnOnOffWifi(Context context, boolean b) {
        wifiManager.setWifiEnabled(b);
        Log.d(TAG, "MainActivity.turnOnOffWifi(): " + b);
    }
}
