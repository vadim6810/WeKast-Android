package com.wekast.wekastandroidclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.models.DongleReconfig;

import static com.wekast.wekastandroidclient.model.Utils.SLIDE;
import static com.wekast.wekastandroidclient.model.Utils.UPLOAD;


/**
 * Created by RDL on 20.10.2016.
 */

public class DongleService extends Service {
    final String TAG = "DongleService";


    WifiController wifiController;


    @Override
    public void onCreate() {
        super.onCreate();

        wifiController = new WifiController(getApplicationContext());
        //какойто код )))
//        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
//        wifiControllerOld = new WifiControllerOld(wifiManager);
//        accessPointController = new AccessPointController(wifiManager);

//        AsyncTask.execute(new Runnable() {
//            public void run() {
//                // TODO: think where better to place networkManipulations, maybe at the end
//                // Manipulations with network
//                networkManipulations();
//            }
//        });
//
//        Sender sender = new Sender(getApplicationContext());

        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        readIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void readIntent(Intent intent) {
        switch (intent.getIntExtra("command", 0)){
            case UPLOAD:
                Log.d(TAG, "readIntent: UPLOAD " +intent.getStringExtra("UPLOAD"));
                //wifiController.changeState(WifiController.WifiState.WIFI_STATE_CONNECT);
                wifiController.connectToDefault();
                // send config
                // change state
                // determine IP address dongle (list wifi clients OR socket)
                // Send file command (receive port number)
                // Send file
                // pending intent to activity when upload ready
                break;
            case SLIDE:
                Log.d(TAG, "readIntent: SLIDE " +intent.getStringExtra("SLIDE"));
                // send command slide
                break;
            default:
                Log.d(TAG, "readIntent:  NO COMMAND" );
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Manipulations with network
     */
    private void networkManipulations() {
        // Saving wifi and access point states before launching application
        saveWifiAdapterState();

        // Connecting to default Dongle Access Point
//        Wifi wifi = new Wifi(this);
//        wifi.connectToAccessPoint();

        // Saving to SharedPreferences current ip of dongle (access point)
        saveDongleIp();

        // Send to dongle new ssid and pass
//        DongleReconfig reconfigDongle = new DongleReconfig(this);
//        reconfigDongle.reconfigure();

        waitWileDongleReceiveNewConfig();

        // Create and run Access Point with new ssid and pass
//        accessPoint = new AccessPoint(this);
//        accessPoint.createAccessPoint();

        // Saving to SharedPreferences current ip of dongle (wifi client)
        // TODO: wait while donle will connect to Access Point of Client
        saveDongleIp();
    }

    /**
     * Saving to SharedPreferences current ip of dongle
     */
    private void saveDongleIp() {
//        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//        String curDongleIp = wifiControllerOld.getIpAddr(dhcpInfo.serverAddress);
//        Utils.setFieldSP(getApplicationContext(), "DONGLE_IP", curDongleIp);
//        Utils.setFieldSP(getApplicationContext(), "DONGLE_PORT", "8888");
    }

    private void saveWifiAdapterState() {
        // TODO: save current state of wifi and access point. If enabled save current working wifi
//        Boolean isWifiEnabled = wifiControllerOld.isWifiOn(this);
        // TODO: save access point state to isAccessPointEnabled
//        if(isWifiEnabled) {
//            Utils.setFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP", isWifiEnabled.toString());
//            Utils.setFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP", "false");
//             TODO: save connected wifi ssid
//        } else {
//            Utils.setFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP", isWifiEnabled.toString());
//            Utils.setFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP", "false");
//        }
    }

    private void waitWileDongleReceiveNewConfig() {
        String isConfigSended = "";
        boolean received = false;
        while (!received) {
            isConfigSended = Utils.getFieldSP(getApplicationContext(), "IS_CONFIG_SENDED");
            if (isConfigSended.equals("1")) {
                received = true;
                Utils.setFieldSP(getApplicationContext(), "IS_CONFIG_SENDED", "0");
            } else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "WelcomeActivity.waitWileDongleReceiveNewConfig():  " + e);
                }
            }
        }
    }
}
