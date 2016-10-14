package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;


import com.wekast.wekastandroidclient.activity.list.FragmentListPresentations;
import com.wekast.wekastandroidclient.activity.list.SettingsActivity;
import com.wekast.wekastandroidclient.activity.slider.FragmentSlider;
import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.CustomPhoneStateListener;
import com.wekast.wekastandroidclient.model.ProccesCall;
import com.wekast.wekastandroidclient.model.Sender;
import com.wekast.wekastandroidclient.model.SenderTasksToDongle;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.models.AccessPoint;
import com.wekast.wekastandroidclient.models.DongleReconfig;
import com.wekast.wekastandroidclient.models.Wifi;

import org.json.JSONObject;

import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity implements FragmentListPresentations.onSomeEventListener {
    private static final String TAG = "wekastlog";
    private TextView tvWelcome;
    Context context = this;
    private int activityState;
    private BroadcastReceiver processCall;
    FragmentListPresentations fragmentListPresentations;
    FragmentTransaction fragmentTransaction;
    private static long back_pressed;

    WifiManager wifiManager = null;
    WifiController wifiController = null;
    AccessPointController accessPointController = null;
    AccessPoint accessPoint = null;

    String curPresPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + getFieldSP(context, "login"));

        fragmentListPresentations = new FragmentListPresentations();
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmContainer, fragmentListPresentations);
//        fragmentTransaction.addToBackStack(null);
        activityState = PRESENTATION_LIST;
        fragmentTransaction.commit();

        //какойто код )))
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiController = new WifiController(wifiManager);
        accessPointController = new AccessPointController(wifiManager);

//        AsyncTask.execute(new Runnable() {
//            public void run() {
//                // TODO: think where better to place networkManipulations, maybe at the end
//                // Manipulations with network
//                networkManipulations();
//            }
//        });

        Sender sender = new Sender(context);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void someEvent(String presPath) {
        processCall = new ProccesCall();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(processCall, intentFilter);

        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmContainer, new FragmentSlider());
//        fragmentTransaction.addToBackStack(null);
        activityState = SLIDER;
        fragmentTransaction.commit();
        uploadPresentationToDongle(presPath);
    }

    private void uploadPresentationToDongle(String presPath) {
        String curPresPath = presPath;
        JSONObject task = Utils.createJsonTask("uploadFile");
        // TODO: why ip 192.168.1.1? must be 192.168.43.48
//        String curDongleIp = Utils.getFieldSP(context, "DONGLE_IP");
        String curDongleIp = "192.168.43.48";
//        String curDongleIp = "192.168.43.248";
        String curDonglePort = Utils.getFieldSP(context, "DONGLE_PORT");
        Utils.setFieldSP(context, "EZS_TO_DONGLE_PATH", presPath);
        SenderTasksToDongle dongleSenderTasks = new SenderTasksToDongle(curDongleIp, curDonglePort, task , context);
        dongleSenderTasks.start();
        int i = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(processCall);

        // TODO: think if needed
        accessPoint.destroyAccessPoint();
        restoreWifiAdapterState();
    }

    @Override
    public void onBackPressed() {
        switch (activityState) {
            case PRESENTATION_LIST:
                if (back_pressed + 2000 > System.currentTimeMillis())
                    finishAffinity();
                else
                    toastShow(context, "Please click BACK again to exit!");
                back_pressed = System.currentTimeMillis();
                break;
            case SLIDER:
                CustomPhoneStateListener.blockingCall = false;
                unregisterReceiver(processCall);
                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmContainer, fragmentListPresentations);
//                fragmentTransaction.addToBackStack(null);
                activityState = PRESENTATION_LIST;
                fragmentTransaction.commit();
                break;
        }
    }

    /**
     * Manipulations with network
     */
    private void networkManipulations() {
        // Saving wifi and access point states before launching application
        saveWifiAdapterState();

        // Connecting to default Dongle Access Point
        Wifi wifi = new Wifi(this);
        wifi.connectToAccessPoint();

        // Saving to SharedPreferences current ip of dongle (access point)
        saveDongleIp();

        // Send to dongle new ssid and pass
        DongleReconfig reconfigDongle = new DongleReconfig(this);
        reconfigDongle.reconfigure();

        waitWileDongleReceiveNewConfig();

        // Create and run Access Point with new ssid and pass
        accessPoint = new AccessPoint(this);
        accessPoint.createAccessPoint();

        // Saving to SharedPreferences current ip of dongle (wifi client)
        // TODO: wait while donle will connect to Access Point of Client
        saveDongleIp();
    }

    private void waitWileDongleReceiveNewConfig() {
        String isConfigSended = "";
        boolean received = false;
        while (!received) {
            isConfigSended = Utils.getFieldSP(context, "IS_CONFIG_SENDED");
            if (isConfigSended.equals("1")) {
                received = true;
                Utils.setFieldSP(context, "IS_CONFIG_SENDED", "0");
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

    /**
     * Saving to SharedPreferences current ip of dongle
     */
    private void saveDongleIp() {
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String curDongleIp = wifiController.getIpAddr(dhcpInfo.serverAddress);
        Utils.setFieldSP(context, "DONGLE_IP", curDongleIp);
        Utils.setFieldSP(context, "DONGLE_PORT", "8888");
    }

    private void saveWifiAdapterState() {
        // TODO: save current state of wifi and access point. If enabled save current working wifi
        Boolean isWifiEnabled = wifiController.isWifiOn(this);
        // TODO: save access point state to isAccessPointEnabled
        if(isWifiEnabled) {
            Utils.setFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP", isWifiEnabled.toString());
            Utils.setFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP", "false");
            // TODO: save connected wifi ssid
        } else {
            Utils.setFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP", isWifiEnabled.toString());
            Utils.setFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP", "false");
        }
    }

    private void restoreWifiAdapterState() {
        // TODO: finish this method
        String isWifiEnabled = getFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP");
//        if (isWifiEnabled.equals("true")) {
        wifiController.turnOnOffWifi(context, true);
//        }

//        String isAccessPointEnabled = Utils.getFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP");
//        accessPointController.setAccessPointEnabled(context, isAccessPointEnabled);
    }

}
