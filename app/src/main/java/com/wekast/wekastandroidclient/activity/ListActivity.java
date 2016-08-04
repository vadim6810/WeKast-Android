package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.WiFiDirectBroadcastReceiver;
import com.wekast.wekastandroidclient.model.WifiApControl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meztiros on 31.07.2016.
 */
public class ListActivity extends Activity {
    private static final String TAG = "wekastClient";
    Context context;
    WifiApControl apControl;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private List peers = new ArrayList();
    boolean isWifiEnabled;


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
        initViewElements();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);



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
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void btnConnect_Click(View view) {
        createWiFiHotspot();
        Intent i = new Intent(this, TestActivity.class);
        startActivity(i);


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

    private void initViewElements() {
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

        context = this;
        apControl = new WifiApControl((WifiManager) this.getSystemService(Context.WIFI_SERVICE));
//        apControl = new WifiApControl((WifiManager) this.getSystemService(Context.WIFI_SERVICE));
    }

    private void createWiFiHotspot() {
        // TODO: add to SharedPreferences dongle IP
        apControl.configureWifiConfig(getText(R.string.ssid).toString(), getText(R.string.pass).toString());


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Log.d(TAG, "Settings.System.canWrite(context)? true");
                    apControl.turnOnOffHotspot(context, true, apControl);
                } else {
                    Log.d(TAG, "Settings.System.canWrite(context)? false");
                    Intent grantIntent = new   Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivity(grantIntent);
                    apControl.turnOnOffHotspot(context, true, apControl);
                }

            } else {
                apControl.turnOnOffHotspot(context, true, apControl);
            }
        } catch (Exception e) {
            Log.d(TAG, "ListActivity.createWiFiHotspot(): " + e.toString(), e); // shouldn't happen
        }

//        apControl.turnOnOffHotspot(context, true, apControl);
//        apControl.turnOnOffHotspot(context, true);
    }

//    private void createWifiP2p() {
//        WifiP2pManager wifiP2pManager = new WifiP2pManager();
//    }



    public void setIsWifiP2pEnabled(boolean isEnabled) {
        isWifiEnabled = isEnabled;
    }

}
