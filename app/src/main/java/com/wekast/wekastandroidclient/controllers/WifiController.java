package com.wekast.wekastandroidclient.controllers;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


import com.wekast.wekastandroidclient.model.Utils;

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
    private static final String TAG = "dongle.wekast";

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

    private final boolean wifiEnabled;
    private final WifiManager wifiManager;
    private Context context;
    private WifiConfiguration oldConfig;

    public WifiController(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Сохраняем старые настройки точки доступа
        oldConfig = getWifiApConfiguration(wifiManager);
        // Сохраняем состояние Wifi
        wifiEnabled = wifiManager.isWifiEnabled();
    }

    private WifiConfiguration configureAP(String ssid, String pass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
//        wifiConfig.SSID = "wekast";
//        wifiConfig.preSharedKey = "12345678";
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
     * Start Access Point on Dongle with default settings
     *
     * @return
     */
    private boolean startAP() {
        String curSsid = Utils.getFieldSP(context, AP_SSID_KEY);
        String curPass = Utils.getFieldSP(context, AP_PASS_KEY);
        return isWifiApEnabled(wifiManager) || setWifiApEnabled(wifiManager, configureAP(curSsid, curPass), true);
//        return isWifiApEnabled(wifiManager) || setWifiApEnabled(wifiManager, configureWifi(), true);
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
        //todo in progress
        if (wifiState == WifiState.WIFI_STATE_CONNECT) {
//            stopAP();
//            startConnection();
            curWifiState = WifiState.WIFI_STATE_CONNECT;
        } else if (wifiState == WifiState.WIFI_STATE_AP) {
//            startAP();
            curWifiState = WifiState.WIFI_STATE_AP;
        }
//        else if (wifiState == WifiState.WIFI_STATE_OFF) {
//            stopAP();
//            curWifiState = WifiState.WIFI_STATE_AP;
//        }
    }

    public enum WifiState {
        WIFI_STATE_OFF,
        WIFI_STATE_AP,
        WIFI_STATE_CONNECT
    }

//    public int getWifiModuleState() {
//        return wifiManager.getWifiState();
//    }

    public boolean isWifiEnabled() {
        // TODO: refactor to less rows
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        WifiInfo info = wifiManager.getConnectionInfo();
        String curSsid  = info.getSSID();
        boolean isConnected = networkInfo.isConnected();
        String curSsidFromSP = Utils.getFieldSP(context, AP_SSID_KEY);
        int i = 0;
        if (curSsid.equals("\"" + curSsidFromSP + "\"") && isConnected)
            return true;
        return false;
    }

    public void saveGatewayIP() {
        DhcpInfo info = wifiManager.getDhcpInfo();
        String strIp = getIpAddr(info.serverAddress);
        Utils.setFieldSP(context, "DONGLE_IP", strIp);
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

//    public String getDongleIp() {
//        WifiP2pManager wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
//        List list = peers;
//
//        return "";
//    }

    private List peers = new ArrayList();
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
//            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
//            if (peers.size() == 0) {
//                Log.d(WiFiDirectActivity.TAG, "No devices found");
//                return;
//            }
        }
    };
}
