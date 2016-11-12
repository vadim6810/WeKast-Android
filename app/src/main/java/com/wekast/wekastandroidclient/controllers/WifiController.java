package com.wekast.wekastandroidclient.controllers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.wekast.wekastandroidclient.model.ClientScanResult;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.model.WifiApManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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

    private final boolean wifiEnabled;
    private final WifiManager wifiManager;
    private Context context;
    private WifiConfiguration oldConfig;
    private WifiApManager wifiApManager;

    public Context getContext() {
        return context;
    }

    public WifiController(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Сохраняем старые настройки точки доступа
        oldConfig = getWifiApConfiguration(wifiManager);
        // Сохраняем состояние Wifi
        wifiEnabled = wifiManager.isWifiEnabled();
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
        return isWifiApEnabled(wifiManager) || setWifiApEnabled(wifiManager, configureAP(curSsid, curPass), true);
    }

    private boolean stopAP() {
        return setWifiApEnabled(wifiManager, oldConfig, false);
    }

    public boolean switchFromWifiToAP() {
        wifiManager.setWifiEnabled(false);
        startAP();
        return true;
    }

    /**
     * Connect to Access Point on Client (Android or iOs)
     *
     * @return
     */
    public boolean startConnection() {
        stopAP();
        wifiManager.setWifiEnabled(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
        WifiConfiguration wifiConfig = configureWifi(curSsid, curPass);

        int networkId = wifiManager.addNetwork(wifiConfig);
        if (networkId < 0) {
            throw new RuntimeException("coudn't add network " + curSsid);
        }
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();

        return true;
    }

    public boolean connectToAccessPoint() {
        stopAP();
        wifiManager.setWifiEnabled(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
        WifiConfiguration wifiConfig = configureWifi(curSsid, curPass);

        int networkId = wifiManager.addNetwork(wifiConfig);
        if (networkId < 0) {
            throw new RuntimeException("coudn't add network " + curSsid);
        }
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();

        return true;
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
        wifiManager.setWifiEnabled(wifiEnabled);
        setWifiApConfiguration(wifiManager, oldConfig);
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

    public boolean isWifiEnabled() {
        // TODO: refactor to less rows
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        WifiInfo info = wifiManager.getConnectionInfo();
        String curSsid = info.getSSID();
        boolean isConnected = networkInfo.isConnected();
        String curSsidFromSP = Utils.getFieldSP(context, AP_SSID_KEY);
        int i = 0;
        if (curSsid.equals("\"" + curSsidFromSP + "\"") && isConnected)
            return true;
        return false;
    }

    public void saveGatewayIP() {
        DhcpInfo info = null;
        boolean isIpReceived = false;
        while (!isIpReceived) {
            info = wifiManager.getDhcpInfo();
            String receivedIp = getIpAddr(info.ipAddress);
            if (!receivedIp.equals("0.0.0.0"))
                isIpReceived = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String dongleIp = getIpAddr(info.serverAddress);
        Utils.setFieldSP(context, "DONGLE_IP", dongleIp);
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
                break;
            }
        }
    }

}
