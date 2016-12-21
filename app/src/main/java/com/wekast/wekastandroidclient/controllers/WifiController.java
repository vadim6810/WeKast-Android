package com.wekast.wekastandroidclient.controllers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.wekast.wekastandroidclient.activity.WelcomeActivity;
import com.wekast.wekastandroidclient.model.ClientScanResult;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.model.WifiApManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by ELAD on 10/14/2016.
 */

public class WifiController {
    public static final String TAG = "WifiController";

//    private static final String AP_SSID_KEY = "ACCESS_POINT_SSID_ON_APP";
//    private static final String AP_PASS_KEY = "ACCESS_POINT_PASS_ON_APP";

    private static Method setWifiApEnabled;
    private static Method isWifiApEnabled;
    private static Method getWifiApConfiguration;
    private static Method setWifiApConfiguration;

    private WifiState curWifiState = WifiState.WIFI_STATE_OFF;

    private static boolean setWifiApEnabled(WifiManager wifiManager, WifiConfiguration wifiConfiguration, boolean enabled) {
        try {
            return (boolean) setWifiApEnabled.invoke(wifiManager, wifiConfiguration, enabled);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isWifiApEnabled(WifiManager wifiManager) {
        try {
            return (boolean) isWifiApEnabled.invoke(wifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static WifiConfiguration getWifiApConfiguration(WifiManager wifiManager) {
        try {
            return (WifiConfiguration) getWifiApConfiguration.invoke(wifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean setWifiApConfiguration(WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
        try {
            return (boolean) setWifiApConfiguration.invoke(wifiManager, wifiConfiguration);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    static {
        // lookup methods and fields not defined publicly in the SDK.
        Class<?> cls = WifiManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.equals("setWifiApEnabled")) {
                setWifiApEnabled = method;
                setWifiApEnabled.setAccessible(true);
            }
            if (methodName.equals("isWifiApEnabled")) {
                isWifiApEnabled = method;
                isWifiApEnabled.setAccessible(true);
            }
            if (methodName.equals("getWifiApConfiguration")) {
                getWifiApConfiguration = method;
                getWifiApConfiguration.setAccessible(true);
            }
            if (methodName.equals("setWifiApConfiguration")) {
                setWifiApConfiguration = method;
                setWifiApConfiguration.setAccessible(true);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final WifiManager wifiManager;
    private Context context;
    private WelcomeActivity activity;
    private boolean isAPEnabledOnBoot;
    private WifiConfiguration APConfigOnBoot;
    private boolean isWifiEnabledOnBoot;
    private boolean isWifiConnectedOnBoot;
    private int wifiNetworkIdOnBoot;
    private WifiApManager wifiApManager;

    public Context getContext() {
        return context;
    }

    public WifiController(Context context) {
        this.context = context;
        activity = WelcomeActivity.welcomeActivity;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        saveApConfigOnStartup();
        saveWifiConfigOnStartup();
        wifiApManager = new WifiApManager(context);
    }

    /**
     * Function saves Access Point status (enabled/disabled) and configuration (ssid, pass) for
     * restoring
     */
    private void saveApConfigOnStartup() {
        APConfigOnBoot = getWifiApConfiguration(wifiManager);
        isAPEnabledOnBoot = isWifiApEnabled(wifiManager);
        if (isAPEnabledOnBoot)
            stopAP();
    }

    /**
     * Function saves Wifi status (enabled/disabled) and configuration network for restoring
     */
    private void saveWifiConfigOnStartup() {
        isWifiEnabledOnBoot = wifiManager.isWifiEnabled();
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            isWifiConnectedOnBoot = true;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifiNetworkIdOnBoot = wifiInfo.getNetworkId();
        } else
            isWifiConnectedOnBoot = false;
    }

    private WifiConfiguration configureAP(String ssid, String pass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = ssid;
        wifiConfig.preSharedKey = pass;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        return wifiConfig;
    }

    private WifiConfiguration configureWifi(String ssid, String pass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.preSharedKey = "\"" + pass + "\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        return wifiConfig;
    }

    /**
     * Start Access Point
     *
     * @return true if Access Point started
     */
    private boolean startAP() {
        if (isWifiApEnabled(wifiManager))
            return true;

        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
//        boolean result = isWifiApEnabled(wifiManager) || setWifiApEnabled(wifiManager, configureAP(curSsid, curPass), true);
        boolean result = setWifiApEnabled(wifiManager, configureAP(curSsid, curPass), true);
        if (result) {
            showMessage("AP started");
            Log.e(TAG, "AP started");
        } else {
            showMessage("AP didn't start");
            Log.e(TAG, "AP didn't start");
        }
        return result;
    }

    private boolean stopAP() {
        return setWifiApEnabled(wifiManager, APConfigOnBoot, false);
    }

    public boolean switchFromWifiToAP() {
        wifiManager.setWifiEnabled(false);
        startAP();
        return true;
    }

    public boolean connectToAccessPoint() {
        stopAP();
        wifiManager.setWifiEnabled(true);
        waitWifiLoading();

        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
        WifiConfiguration wifiConfig = configureWifi(curSsid, curPass);
        removeConfigIfExistSsid(wifiManager, curSsid);

        int networkId = wifiManager.addNetwork(wifiConfig);
        if (networkId < 0) {
            Log.e(TAG, "couldn't add network " + curSsid);
            return false;
        }
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();

        return true;
    }

    private boolean removeConfigIfExistSsid(WifiManager wifiManager, String ssid) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.removeNetwork(existingConfig.networkId);
                return true;
            }
        }
        return false;
    }

    public WifiState getWifiState() {
        return curWifiState;
    }

//    public void setWifiState(WifiState wifiState) {
//        this.curWifiState = wifiState;
//    }

    public void saveWifiConfig(String ssid, String pass) {
        Utils.setFieldSP(context, AP_SSID_KEY, ssid);
        Utils.setFieldSP(context, AP_PASS_KEY, pass);
    }

    /**
     * Function restores Access Point and Wifi status and settings back to state before application started
     */
    public void restore() {
        restoreAP();
        restoreWifi();
    }

    private void restoreAP() {
        if (isWifiApEnabled(wifiManager))
            stopAP();
        setWifiApConfiguration(wifiManager, APConfigOnBoot);
        if (isAPEnabledOnBoot)
            setWifiApEnabled(wifiManager, APConfigOnBoot, true);
    }

    private void restoreWifi() {
        if (isWifiEnabledOnBoot) {
            wifiManager.setWifiEnabled(true);
            waitWifiLoading();
            if (isWifiConnectedOnBoot) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(wifiNetworkIdOnBoot, true);
                wifiManager.reconnect();
            }
        }
    }

    public void changeState(WifiState wifiState) {
        // TODO: maybe pass here functions
        if (wifiState == WifiState.WIFI_STATE_CONNECT) {
            curWifiState = WifiState.WIFI_STATE_CONNECT;
        } else if (wifiState == WifiState.WIFI_STATE_AP) {
            curWifiState = WifiState.WIFI_STATE_AP;
        } else if (wifiState == WifiState.WIFI_STATE_OFF) {
            curWifiState = WifiState.WIFI_STATE_OFF;
        }
    }

    public enum WifiState {
        WIFI_STATE_OFF,
        WIFI_STATE_AP,
        WIFI_STATE_CONNECT
    }

    public boolean saveGatewayIP() {
        DhcpInfo info = null;
        boolean isIpReceived = false;
//        Long timeStart = System.currentTimeMillis();
        while (!isIpReceived) {
            info = wifiManager.getDhcpInfo();
            String receivedIp = getIpAddr(info.ipAddress);
            if (!receivedIp.equals("0.0.0.0")) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                String ssid = connectionInfo.getSSID();
                if (ssid.equals("\"wekast\"")) {
                    showMessage("Connected to AP dongle");
                    Log.e(TAG, "Connected to AP dongle");
                    isIpReceived = true;
                }
            }

//            Long timeCurrent = System.currentTimeMillis();
//            Long timeResult = (timeCurrent - timeStart) / 1000;

//            if (timeResult > 60)
//                return false;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Exception message: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        String dongleIp = getIpAddr(info.serverAddress);
        Utils.setFieldSP(context, "DONGLE_IP", dongleIp);
        return true;
    }

    public String getIpAddr(int ipAddr) {
        String ipString = String.format(
                "%d.%d.%d.%d",
                (ipAddr & 0xff),
                (ipAddr >> 8 & 0xff),
                (ipAddr >> 16 & 0xff),
                (ipAddr >> 24 & 0xff));
        return ipString;
    }

    // TODO: check if clients more than one think about new solution
    public boolean saveConnectedDeviceIp() {
        long startTime = System.currentTimeMillis();
        while (true) {
            ArrayList<ClientScanResult> clients = wifiApManager.getClientList(false);

            int passedTime = (int) (System.currentTimeMillis() - startTime) / 1000;
            if (passedTime > TIME_TO_TRYING_SEND_PRESENTATION) {
                Log.e(TAG, "Time to trying send presentation PASSED");
                return false;
            }

            if (clients.size() > 0) {
                ClientScanResult clientScanResult = clients.get(0);
                String ip = clientScanResult.getIpAddr();

                String curIPSS = ip.substring(0,3);
                if (ip.equals("192.168.43.1") || ip.equals("192.168.1.1") || !ip.substring(0,3).equals("192")) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Exception message: " + e.getMessage());
                    }
                    Log.e(TAG, "No connected dongle");
                    continue;
                }
                changeState(WifiState.WIFI_STATE_CONNECT);
                Utils.setFieldSP(context, "DONGLE_IP", ip);
                showMessage("DONGLE CONNECTED");
                Log.e(TAG, "Connected dongle with ip: " + ip);
                return true;
            }
        }
    }

    public void showMessage(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Utils.toastShow(activity, message);
            }
        });
    }

    private boolean waitWifiLoading() {
        while (!wifiManager.isWifiEnabled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

}
