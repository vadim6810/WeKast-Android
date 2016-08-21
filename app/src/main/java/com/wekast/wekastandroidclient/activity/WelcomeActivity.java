package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.models.AccessPoint;
import com.wekast.wekastandroidclient.models.DongleReconfig;
import com.wekast.wekastandroidclient.models.Wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity {
    private TextView tvWelcome;
    private ArrayList<String> filesLocal = new ArrayList<>();
    private HashMap<String, String> mapDownload = new HashMap<>();
    Context context = this;
    AccessServiceAPI m_AccessServiceAPI;
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
        tvWelcome.setText("Welcome: " + Utils.getFieldSP(context, "login"));
        m_AccessServiceAPI = new AccessServiceAPI();
        ListView presenterList = (ListView) findViewById(R.id.presenterList);

        String answer = getIntent().getStringExtra("answer");

        //для загрузки
        mapDownload = Utils.parseJSONArrayMap(context, answer);

        //для адаптера
        filesLocal =  Utils.getAllFilesLocal();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, filesLocal);
        presenterList.setAdapter(adapter);

        mappingPresentations();

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiController = new WifiController(wifiManager);
        accessPointController = new AccessPointController(wifiManager);

        new Thread(new Runnable() {
            public void run() {
                // TODO: think where better to place networkManipulations, maybe at the end
                // Manipulations with network
                networkManipulations();
            }
        }).start();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: think if needed
        accessPoint.destroyAccessPoint();
        restoreWifiAdapterState();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            finishAffinity();
        else
            Utils.toastShow(context, "Press once again to exit!");
        back_pressed = System.currentTimeMillis();
    }

    public void btnClearPref_Click(View v) {
        Utils.clearSP(context);
        Utils.toastShow(context, "Preference cleared");
    }

    public void btnDownload_Click(View v) {
        String login = Utils.getFieldSP(context, "login");
        String password = Utils.getFieldSP(context, "password");
        if(mapDownload.size() > 0)
        m_AccessServiceAPI.taskDownload(login, password, mapDownload, context);
        else Utils.toastShow(context, "You havn't presentations on server!");
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

    private void mappingPresentations() {
        if (mapDownload.size() > 0) {
            for (String s: filesLocal) {
                for(Iterator<HashMap.Entry<String, String>> it = mapDownload.entrySet().iterator(); it.hasNext(); ) {
                    HashMap.Entry<String, String> entry = it.next();
                    if (entry.getValue().equals(s)) {
                        it.remove();
                    }
                }
            }
        }
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
        String isWifiEnabled = Utils.getFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP");
//        if (isWifiEnabled.equals("true")) {
        wifiController.turnOnOffWifi(context, true);
//        }

//        String isAccessPointEnabled = Utils.getFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP");
//        accessPointController.setAccessPointEnabled(context, isAccessPointEnabled);
    }

}
