package com.wekast.wekastandroidclient.models;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiControllerOld;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.List;

/**
 * Created by YEHUDA on 8/20/2016.
 */
public class Wifi {

    private static final String TAG = "wekastlog";
    private Context mainActivityContext = null;
    private Activity mainActivity = null;
    private WifiManager wifiManager = null;
    private WifiControllerOld wifiControllerOld = null;
    private AccessPointController accessPointController = null;

    public Wifi(Activity activity) {
        this.mainActivity = activity;
        this.mainActivityContext = mainActivity.getApplicationContext();
        this.wifiManager = (WifiManager) mainActivityContext.getSystemService(mainActivityContext.WIFI_SERVICE);
        this.wifiControllerOld = new WifiControllerOld(wifiManager);
        this.accessPointController = new AccessPointController(wifiManager);
    }

    public boolean connectToAccessPoint(){
        // Disable access point
        accessPointController.setAccessPointEnabled(mainActivity, false);

        // TODO: maybe remove
        accessPointController.waitAccessPoint();

        // Configure WifiConfiguration for default dongle access point
        String curSsid = mainActivityContext.getResources().getString(R.string.ssid);
        String curPass = mainActivityContext.getResources().getString(R.string.pass);
        wifiControllerOld.configureWifiConfig(curSsid, curPass);

        // Switch on wifi
        wifiControllerOld.turnOnOffWifi(mainActivity, true);

        // Wait while wifi module is loading
        wifiControllerOld.waitWhileWifiLoading();

        // Remove wifi configuration with default dongle access point SSID if exists
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if(list != null) {
            for (WifiConfiguration i : list) {
                if (i.SSID.equals(wifiControllerOld.wifiConfig.SSID)) {
                    wifiManager.removeNetwork(i.networkId);
                }
            }
        }

        // Connect to access point of application
        int netId = wifiControllerOld.addWifiConfiguration();
        if (netId != -1) {
            List<WifiConfiguration> list2 = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list2) {
                if (i.SSID != null && i.SSID.equals(wifiControllerOld.wifiConfig.SSID)) {
                    wifiControllerOld.disconnectFromWifi();
                    wifiControllerOld.enableDisableWifiNetwork(i.networkId, true);
//                    wifiControllerOld.reconnectToWifi();
                    Log.d(TAG, "MainActivity.connectToWifiHotspot(): connected to "
                            + wifiControllerOld.wifiConfig.SSID + " with netId " + netId);
                    break;
                }
            }
        }

        // TODO: maybe remove
        wifiControllerOld.waitWhileWifiLoading(3000);

        // TODO: maybe remove
        isWifiLoaded();
        showMessage("Connected to WiFi " + curSsid);
        Log.d(TAG, "Wifi.connectToAccessPoint(): end ");
        return true;
    }

    private void isWifiLoaded() {
        if (!wifiControllerOld.isWifiConnected(mainActivity)) {
            wifiControllerOld.waitWhileWifiLoading(1000);
            isWifiLoaded();
        }
        wifiControllerOld.waitWhileWifiLoading(3000);

    }

    private void showMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.toastShow(mainActivityContext, message);
            }
        });
        Log.d(TAG, message);
    }
}
