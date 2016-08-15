package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.ControllerWifi;
import com.wekast.wekastandroidclient.model.SendTaskToDongle;
import com.wekast.wekastandroidclient.model.Server;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.model.WiFiDirectBroadcastReceiver;
import com.wekast.wekastandroidclient.model.ControllerAccessPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meztiros on 31.07.2016.
 */
public class ListActivity extends Activity {
    private static final String TAG = "wekastClient";
    Context context = this;
    ControllerAccessPoint apControl;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private List peers = new ArrayList();
    boolean isWifiEnabled;

    WifiManager wifiManager;
    ControllerWifi wifiController;
    ControllerAccessPoint accessPointController;

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers.clear();
            System.out.println("Peers cleared : " + peers);
            peers.addAll(peerList.getDeviceList());

            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
//            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            Log.d(TAG, "ListActivity  Peers : " + peers);
            if (peers.size() == 0) {
                Log.d(TAG, " ListActivity No devices found");
                return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        // определяем массив типа String
        final String[] catNames = new String[]{
                "Презентация1", "Презентация2", "Презентация3", "Презентация4", "Презентация5",
                "Презентация6", "Презентация7", "Презентация8", "Презентация9", "Презентация10",
                "Презентация11", "Презентация12", "Презентация13"
        };

        // получаем экземпляр элемента ListView
        ListView listView = (ListView) findViewById(R.id.listView);

        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, catNames);
        listView.setAdapter(adapter);

//        context = this;
        apControl = new ControllerAccessPoint((WifiManager) this.getSystemService(Context.WIFI_SERVICE));
//        apControl = new ControllerAccessPoint((WifiManager) this.getSystemService(Context.WIFI_SERVICE));



        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiController = new ControllerWifi(wifiManager);
        accessPointController = new ControllerAccessPoint(wifiManager);

        Server server = new Server(this);

    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void btnWelcome_Click(View view) {
//        Intent i = new Intent(this, LoginActivity.class);
//        startActivity(i);

        String curDongleIp = Utils.getFieldSP(context, "dongleIP");
        String curDonglePort = Utils.getFieldSP(context, "donglePort");


//        SendTaskToDongle dongleSendTask = new SendTaskToDongle(curDongleIp, curDonglePort, msg, createJsonTask("show", 1), context);
        // TODO: Generate rundom ssid and pass IF client don't have his own ssid
        String newSsid = "wekastrandom";
        String newPass = "87654321";
        JSONObject task = createJsonTaskSendSsidPass("accessPointConfig", newSsid, newPass);
        SendTaskToDongle dongleSendTask = new SendTaskToDongle(curDongleIp, curDonglePort, task , context);
        dongleSendTask.execute();

        // wait while dongle receive new AP config
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "ListActivity.btnWelcome_Click():  " + e);
        }

        startAccessPoint(newSsid, newPass);
    }

    public void btnConnect_Click(View view) {

        connectToDefaultDongleAccessPoint();



//        createWiFiHotspot();
//        Intent i = new Intent(this, TestActivity.class);
//        startActivity(i);


        // Picking the first device found on the network.
//        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
//
//        WifiP2pConfig config = new WifiP2pConfig();
//        config.deviceAddress = device.deviceAddress;
//        config.wps.setup = WpsInfo.PBC;
//
//        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Log.d(TAG, "ListActivity.onCreate(): discoverPeers onSucess() ");
//            }
//
//            @Override
//            public void onFailure(int reasonCode) {
//                Log.d(TAG, "ListActivity.onCreate(): discoverPeers onFailure() ");
//            }
//        });
    }


    private void createWiFiHotspot() {
        // TODO: add to SharedPreferences dongle IP

//        if (!Utils.getContainsSP(context, "dongleIP")) {
//            Utils.setFieldSP(context, "dongleIP", "192.168.43.48");
//        }
//        if (!Utils.getContainsSP(context, "donglePort")) {
//            Utils.setFieldSP(context, "donglePort", "8888");
//        }

//        apControl.configureWifiConfig(getText(R.string.ssid).toString(), getText(R.string.pass).toString());
//
//
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (Settings.System.canWrite(context)) {
//                    Log.d(TAG, "Settings.System.canWrite(context)? true");
//                    apControl.turnOnOffHotspot(context, true, apControl);
//                } else {
//                    Log.d(TAG, "Settings.System.canWrite(context)? false");
//                    Intent grantIntent = new   Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                    startActivity(grantIntent);
//                    apControl.turnOnOffHotspot(context, true, apControl);
//                }
//
//            } else {
//                apControl.turnOnOffHotspot(context, true, apControl);
//            }
//        } catch (Exception e) {
//            Log.d(TAG, "ListActivity.createWiFiHotspot(): " + e.toString(), e); // shouldn't happen
//        }

//        apControl.turnOnOffHotspot(context, true, apControl);
//        apControl.turnOnOffHotspot(context, true);
    }

//    private void createWifiP2p() {
//        WifiP2pManager wifiP2pManager = new WifiP2pManager();
//    }



    public void setIsWifiP2pEnabled(boolean isEnabled) {
        isWifiEnabled = isEnabled;
    }


    public void connectToDefaultDongleAccessPoint() {
        // TODO: Save current state of wifi (enabled/disabled) if enabled save current working wifi
        //          or access point

        // Check if access point is disabled
//        try {
//            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isAccessPointEnabled");
//            boolean isEn = (Boolean)isWifiApEnabledmethod.invoke(wifiManager);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
        accessPointController.setAccessPointEnabled(context, false);

        // Wait while access point is disabling
        waitAccessPoint();


        // Check if wifi is switched off
        if (!wifiController.isWifiOn(context)) {
            wifiController.turnOnOffWifi(context, true);
        }

        // Wait while wifi is loading
        waitWifi();

        // Configure WifiConfiguration for default dongle access point
        String defaultDongleSsid = getText(R.string.ssid).toString();
        String defaultDonglePass = getText(R.string.pass).toString();
        wifiController.configureWifiConfig(defaultDongleSsid, defaultDonglePass);

        // Remove wifi configuration with default dongle access point SSID if exists
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID.equals(wifiController.wifiConfig.SSID)) {
                wifiManager.removeNetwork(i.networkId);
            }
        }

        // Connect to default dongle access point
        int netId = wifiController.addWifiConfiguration();
        if (netId != -1) {
            List<WifiConfiguration> list2 = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list2) {
                if (i.SSID != null && i.SSID.equals(wifiController.wifiConfig.SSID)) {
                    wifiController.disconnectFromWifi();
                    wifiController.enableDisableWifiNetwork(i.networkId, true);
                    wifiController.reconnectToWifi();
                    Log.d(TAG, "TestConnWithClientActivity.connectToWifiHotspot(): connected to "
                            + wifiController.wifiConfig.SSID + " with netId " + netId);
                    break;
                }
            }
        }

        // Saving to SharedPreferences current ip of dongle
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String curDongleIp = wifiController.getIpAddr(dhcpInfo.serverAddress);
        Utils.setFieldSP(context, "dongleIP", curDongleIp);
        Utils.setFieldSP(context, "donglePort", "8888");
    }

    private JSONObject createJsonTaskSendSsidPass(String task, String ssid, String pass) {
        // TODO: create rundom ssid and pass
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonTask = new JSONArray();
        JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("command", task);
            jsonCommand.put("ssid", ssid);
            jsonCommand.put("pass", pass);
            jsonTask.put(jsonCommand);
            jsonObject.put("task", jsonTask);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Starts access point
     */
    private void startAccessPoint(String ssid, String pass) {
        accessPointController.configureWifiConfig(ssid, pass);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                Log.d(TAG, "TestConnWithClientActivity.startAccessPoint() Settings.System.canWrite(context)? true");
            } else {
                Intent grantIntent = new   Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(grantIntent);
                Log.d(TAG, "TestConnWithClientActivity.startAccessPoint() Settings.System.canWrite(context)? false");
            }
        }

        // Turn off wifi before enabling Access Point
        if (wifiController.isWifiOn(context)) {
            wifiController.turnOnOffWifi(context, false);
        }


/////////////////////////////////////////////////////////////////////////////////
//        WifiConfiguration netConfigOrig = new WifiConfiguration();
//
//        WifiConfiguration netConfig = new WifiConfiguration();
//        netConfig = new WifiConfiguration();
//        netConfig.SSID = ssid;
//        netConfig.preSharedKey = pass;
//        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//        try{
//
//            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
//            netConfigOrig = (WifiConfiguration)getWifiApConfigurationMethod.invoke(wifiManager);
//
//            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig,true);
//
//            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isAccessPointEnabled");
//            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
//
//            }
//
//            Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
//            int apstate = (Integer)getWifiApStateMethod.invoke(wifiManager);
//
//            Log.e("CLIENT", "\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");
//
//        } catch (Exception e) {
//            Log.e(this.getClass().toString(), "", e);
//        }
/////////////////////////////////////////////////////////////////////////////////



        // Turn on access point
        accessPointController.setAccessPointEnabled(context, true);

        // If not to wait application crashes
        waitAccessPoint();
    }

    private void waitAccessPoint() {
        // Wait till access point will work
//        while(!accessPointController.isAccessPointEnabled()) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "ControllerAccessPoint.waitAccessPoint():  " + e);
        }
//        }
    }


    private void waitWifi() {
        // Wait till access point will work
//        while(!accessPointController.isAccessPointEnabled()) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "ControllerAccessPoint.waitWifi():  " + e);
        }
//        }
    }


}
