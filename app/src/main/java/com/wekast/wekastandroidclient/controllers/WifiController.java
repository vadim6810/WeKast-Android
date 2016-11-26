package com.wekast.wekastandroidclient.controllers;

import android.content.Context;
import android.net.DhcpInfo;
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

/**
 * Created by ELAD on 10/14/2016.
 */

public class WifiController {

    private static final String AP_SSID_KEY = "ACCESS_POINT_SSID_ON_APP";
    private static final String AP_PASS_KEY = "ACCESS_POINT_PASS_ON_APP";

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

    private final boolean wifiEnabledOnBoot;
    private final WifiManager wifiManager;
    private Context context;
    private WelcomeActivity activity;
    private WifiConfiguration APConfigOnBoot;
    private Boolean isAPEnabledOnBoot;
    private WifiApManager wifiApManager;

    public Context getContext() {
        return context;
    }

    public WifiController(Context context) {
        this.context = context;
        activity = WelcomeActivity.welcomeActivity;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Save old settings of Access Point
        APConfigOnBoot = getWifiApConfiguration(wifiManager);
        isAPEnabledOnBoot = isWifiApEnabled(wifiManager);
        if (isAPEnabledOnBoot)
            stopAP();
        // Save status Wifi
        wifiEnabledOnBoot = wifiManager.isWifiEnabled();
        wifiApManager = new WifiApManager(context);
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
        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
        boolean result = isWifiApEnabled(wifiManager) || setWifiApEnabled(wifiManager, configureAP(curSsid, curPass), true);
        if (result)
            showMessage("AP started");
        else
            showMessage("AP didn't start");
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

//    /**
//     * Connect to Access Point on Client (Android or iOs)
//     *
//     * @return
//     */
//    public boolean startConnection() {
//        stopAP();
//        wifiManager.setWifiEnabled(true);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
//        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
//        WifiConfiguration wifiConfig = configureWifi(curSsid, curPass);
//
//        int networkId = wifiManager.addNetwork(wifiConfig);
//        if (networkId < 0) {
//            throw new RuntimeException("coudn't add network " + curSsid);
//        }
//        wifiManager.disconnect();
//        wifiManager.enableNetwork(networkId, true);
//        wifiManager.reconnect();
//
//        return true;
//    }

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
            Log.e("WifiController", "connectToAccessPoint: " + "couldn't add network " + curSsid);
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

    public WifiState getSavedWifiState() {
        return curWifiState;
    }

    public void saveWifiConfig(String ssid, String pass) {
        Utils.setFieldSP(context, AP_SSID_KEY, ssid);
        Utils.setFieldSP(context, AP_PASS_KEY, pass);
    }

    public void restore() {
        // TODO restore wifi settings back
        if (isWifiApEnabled(wifiManager)) {
            stopAP();
        }
        wifiManager.setWifiEnabled(wifiEnabledOnBoot);
        setWifiApConfiguration(wifiManager, APConfigOnBoot);
        if (isAPEnabledOnBoot)
            setWifiApEnabled(wifiManager, APConfigOnBoot, true);
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

//    public boolean isWifiEnabled() {
//        // TODO: refactor to less rows
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//        WifiInfo info = wifiManager.getConnectionInfo();
//        String curSsid = info.getSSID();
//        boolean isConnected = networkInfo.isConnected();
//        String curSsidFromSP = Utils.getFieldSP(context, AP_SSID_KEY);
//        int i = 0;
//        if (curSsid.equals("\"" + curSsidFromSP + "\"") && isConnected)
//            return true;
//        return false;
//    }

    public boolean saveGatewayIP() {
        DhcpInfo info = null;
        boolean isIpReceived = false;
//        Long timeStart = System.currentTimeMillis();
        while (!isIpReceived) {
            info = wifiManager.getDhcpInfo();
            String receivedIp = getIpAddr(info.ipAddress);
            if (!receivedIp.equals("0.0.0.0")) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo ();
                String ssid  = connectionInfo.getSSID();
                if (ssid.equals("\"wekast\"")) {
                    showMessage("Connected to AP dongle");
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

    // TODO: if clients more than one think about new solution
    public void saveConnectedDeviceIp() {

        while (true) {
            ArrayList<ClientScanResult> clients = wifiApManager.getClientList(false);
            if (clients.size() > 0) {
//                String ip = clients.get(0).getIpAddr();
//                // TODO: check is availible 8080 on this ip
//                Log.i("------------------- IP", ip);
//                Utils.setFieldSP(context, "DONGLE_IP", ip);
//                isSavedDeviceIp = true;
                ClientScanResult clientScanResult = clients.get(0);
                String ip = clientScanResult.getIpAddr();
                if (ip.equals("192.168.43.1") || ip.equals("192.168.1.1")) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {
                    }
                    Log.w("------------------- IP", "No clients");
                    continue;
                }
                Log.i("------------------- IP", ip);
                Utils.setFieldSP(context, "DONGLE_IP", ip);
                showMessage("Dongle " + ip + " connected to AP");
                break;
            }
        }
    }

    private void showMessage(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
//                Utils.toastShow(activity, message);
            }
        });
    }

    private boolean waitWifiLoading() {
        while(!wifiManager.isWifiEnabled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
