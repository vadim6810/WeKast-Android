package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.models.AccessPoint;
import com.wekast.wekastandroidclient.models.DongleReconfig;
import com.wekast.wekastandroidclient.models.Wifi;

import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity implements FragmentListPresentations.onSomeEventListener {
    private TextView tvWelcome;
    Context context = this;
    private int activityState;
    FragmentListPresentations fragmentListPresentations;
    FragmentTransaction fragmentTransaction;
    private static long back_pressed;

    WifiManager wifiManager = null;
    WifiController wifiController = null;
    AccessPointController accessPointController = null;
    AccessPoint accessPoint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
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

//        new Thread(new Runnable() {
//            public void run() {
//                // TODO: think where better to place networkManipulations, maybe at the end
//                // Manipulations with network
//                networkManipulations();
//            }
//        }).start();

//        new Thread(() ->  networkManipulations()).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

       // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_clear:
                Utils.clearSP(context);
                Utils.toastShow(context, "Preference cleared");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void someEvent(String presPath) {
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmContainer, new FragmentSlider());
//        fragmentTransaction.addToBackStack(null);
        activityState = SLIDER;
        fragmentTransaction.commit();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // TODO: think if needed
//        accessPoint.destroyAccessPoint();
//        restoreWifiAdapterState();
//    }

    @Override
    public void onBackPressed() {
        switch (activityState) {
            case PRESENTATION_LIST:
                if (back_pressed + 2000 > System.currentTimeMillis())
                    finishAffinity();
                else
                    Utils.toastShow(context, "Please click BACK again to exit!");
                back_pressed = System.currentTimeMillis();
                break;
            case SLIDER:
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

        // Create and run Access Point with new ssid and pass
        accessPoint = new AccessPoint(this);
        accessPoint.createAccessPoint();

        // Saving to SharedPreferences current ip of dongle (wifi client)
        saveDongleIp();
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
