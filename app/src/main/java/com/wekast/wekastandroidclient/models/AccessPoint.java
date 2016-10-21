package com.wekast.wekastandroidclient.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiControllerOld;
import com.wekast.wekastandroidclient.model.Utils;

/**
 * Created by YEHUDA on 8/20/2016.
 */
public class AccessPoint {

    private static final String TAG = "wekastlog";
    private Context mainActivityContext = null;
    private Activity mainActivity = null;
    private WifiManager wifiManager = null;
    private WifiControllerOld wifiControllerOld = null;
    private AccessPointController accessPointController = null;

    public AccessPoint(Activity activity){
        this.mainActivity = activity;
        this.mainActivityContext = mainActivity.getApplicationContext();
        this.wifiManager = (WifiManager) mainActivityContext.getSystemService(mainActivityContext.WIFI_SERVICE);
        this.wifiControllerOld = new WifiControllerOld(wifiManager);
        this.accessPointController = new AccessPointController(wifiManager);
    }

    /**
     * Function that creates and turn on AccessPoint with default ssid and pass
     *
     * @return true at the end if access point is started
     */
    public boolean createAccessPoint(){
        // Configure access point
        String newSsid = Utils.getFieldSP(mainActivityContext, "ACCESS_POINT_SSID_NEW");
        String newPass = Utils.getFieldSP(mainActivityContext, "ACCESS_POINT_PASS_NEW");
        accessPointController.configureWifiConfig(newSsid, newPass);

        // TODO: check how it works on android 5, 6 first load. Do user have to grant rights to application?
        // Compatibility with android 5, android 6
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(mainActivity)) {
                Log.d(TAG, "AccessPoint.createAccessPoint() Settings.System.canWrite(context)? true");
            } else {
                Intent grantIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                mainActivity.startActivity(grantIntent);
                Log.d(TAG, "AccessPoint.createAccessPoint() Settings.System.canWrite(context)? false");
            }
        }

        // Turn off wifi before enabling Access Point
        if (wifiControllerOld.isWifiOn(mainActivity)) {
            wifiControllerOld.turnOnOffWifi(mainActivity, false);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Turn on access point
        accessPointController.setAccessPointEnabled(mainActivity, true);

        // If not to wait application crashes
        accessPointController.waitAccessPoint();
        return true;
    }

    public boolean destroyAccessPoint()
    {
        accessPointController.setAccessPointEnabled(mainActivity, false);
        // TODO: restore wifi module settings before applications was start
        // this can be that wifi was enebled or disabled, or Access point was enebled
        return true;
    }
}
