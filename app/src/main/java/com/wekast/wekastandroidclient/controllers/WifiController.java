package com.wekast.wekastandroidclient.controllers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by YEHUDA on 8/1/2016.
 */
public class WifiController {

    private static final String TAG = "wekastlog";
    public WifiManager wifiManager;
    public WifiConfiguration wifiConfig;

    public WifiController(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    /**
     * Function check whether wifi is enabled
     *
     * @param context
     * @return whether wifi is enabled
     */
    public boolean isWifiOn(Context context) {
        boolean isWifiOn = wifiManager.isWifiEnabled();
        Log.d(TAG, "WifiController.isWifiOn(): " + isWifiOn);
        return isWifiOn;
    }

    /**
     * Function turns on or turns off wifi
     *
     * @param context
     * @param b
     */
    public void turnOnOffWifi(Context context, boolean b) {
        wifiManager.setWifiEnabled(b);
        Log.d(TAG, "WifiController.turnOnOffWifi(): " + b);
    }

    /**
     * Function that configures WifiConfiguration for access point
     */
    public void configureWifiConfig(String ssid, String pass) {
        wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"".concat(ssid).concat("\"");
        wifiConfig.preSharedKey = "\"".concat(pass).concat("\"");
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        Log.d(TAG, "WifiController.configureWifiConfig():\n" + wifiConfig);
    }

//    /**
//     * Function that returns current configured configuration
//     *
//     * @return current configured wificonfiguration
//     */
//    public WifiConfiguration getWifiConfig() {
//        return wifiConfig;
//    }


    /* Function to disconnect from the currently connected WiFi AP.
    * @return true  if disconnection succeeded
    * 				 false if disconnection failed
    */
    public void disconnectFromWifi() {
        if (!wifiManager.disconnect()) {
            Log.d("TAG", "Failed to disconnect from network!");
        }
    }

    /**
     * Add WiFi configuration to list of recognizable networks
     *
     * @return networkId
     */
    public int addWifiConfiguration() {
        int networkId = wifiManager.addNetwork(wifiConfig);
        if (networkId == -1) {
            Log.d("TAG", "Failed to add network configuration!");
            return -1;
        }
        return networkId;
    }

    /**
     * Enable network to be connected
     */
    public void enableDisableWifiNetwork(int networkId, boolean b) {
        if (!wifiManager.enableNetwork(networkId, b)) {
            Log.d("TAG", "Failed to enable network!");
        }
    }

    /**
     * Connect to network
     */
    public void reconnectToWifi() {
        if (!wifiManager.reconnect()) {
            Log.d("TAG", "Failed to connect!");
        }
    }

    private void isWifiEnabled(Context context) {
        if(!isWifiOn(context)) {
            turnOnOffWifi(context, true);
            //job completed. Rest for 5 second before doing another one
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //do job again
            isWifiEnabled(context);
        }
    }

    /**
     * Converts int ip address to readable string
     *
     * @param ipAddr
     * @return readable ip address string
     */
    public String getIpAddr(int ipAddr) {
        String ipString = String.format(
                "%d.%d.%d.%d",
                (ipAddr & 0xff),
                (ipAddr >> 8 & 0xff),
                (ipAddr >> 16 & 0xff),
                (ipAddr >> 24 & 0xff));
        return ipString;
    }

    public void waitWhileWifiLoading() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "AccessPointController.waitWifi():  " + e);
        }
    }

    public void waitWhileWifiLoading(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "AccessPointController.waitWifi():  " + e);
        }
    }

    public boolean isWifiConnected(Context context) {
        boolean isWifiConnected = true;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
        if (mWifi == null) {
            return false;
        } else {
            if (mWifi.isConnected())
                return isWifiConnected;
        }
        // TODO: check when application came here. Or refactor
        return isWifiConnected;
    }

}