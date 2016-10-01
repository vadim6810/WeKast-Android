package com.wekast.wekastandroidclient.model;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by YEHUDA on 8/1/2016.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "wekastlog";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
//    private ListActivity listActivity;
    private WifiP2pManager.PeerListListener myPeerListListener;

//    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
//                                       ListActivity activity) {
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Activity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
//        this.listActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
//                listActivity.setIsWifiP2pEnabled(true);
                Log.d(TAG, "WiFiDirectBroadcastReceiver.onReceive(): Wifi P2P is enabled");
            } else {
                // Wi-Fi P2P is not enabled
//                listActivity.setIsWifiP2pEnabled(false);
                Log.d(TAG, "WiFiDirectBroadcastReceiver.onReceive(): Wi-Fi P2P is not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Log.d(TAG, "WiFiDirectBroadcastReceiver.onReceive(): Call WifiP2pManager.requestPeers() to get a list of current peers");

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
                Log.d(TAG, "WiFiDirectBroadcastReceiver.onReceive(): P2P peers changed ");



//                try {
//                    Class<?> wifiManager = Class
//                            .forName("android.net.wifi.p2p.WifiP2pManager");
//                    Method method = wifiManager
//                            .getMethod(
//                                    "enableP2p",
//                                    new Class[] { android.net.wifi.p2p.WifiP2pManager.Channel.class });
//                    method.invoke(mManager, mChannel);
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Log.d(TAG, "WiFiDirectBroadcastReceiver.onReceive() Respond to new connection or disconnections");
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
//                mManager.requestConnectionInfo(mChannel, connectionListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d(TAG, "WiFiDirectBroadcastReceiver.onReceive() Respond to this device's wifi state changing");
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}

//mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        mChannel = mManager.initialize(this, getMainLooper(), null);
//        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
//        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);