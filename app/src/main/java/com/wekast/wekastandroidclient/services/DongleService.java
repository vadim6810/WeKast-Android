package com.wekast.wekastandroidclient.services;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.wekast.wekastandroidclient.controllers.CommandController;
import com.wekast.wekastandroidclient.controllers.SocketController;
import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import static com.wekast.wekastandroidclient.model.Utils.DONGLE_AP_PASS_DEFAULT;
import static com.wekast.wekastandroidclient.model.Utils.DONGLE_AP_SSID_DEFAULT;
import static com.wekast.wekastandroidclient.model.Utils.DONGLE_SOCKET_PORT;
import static com.wekast.wekastandroidclient.model.Utils.SLIDE;
import static com.wekast.wekastandroidclient.model.Utils.UPLOAD;

/**
 * Created by RDL on 20.10.2016.
 */

public class DongleService extends Service {

    private ServiceThread thread;

    class ServiceThread extends Thread {

        ServiceThread() {
            setDaemon(true);
            setName("DongleServiceThread");
        }

        @Override
        public void run() {
            String dstAddress = Utils.getFieldSP(getApplicationContext(), "DONGLE_IP");
            String dstPort = DONGLE_SOCKET_PORT;

            String[] ssidPass = generateRandomSsidPass();

            JSONObject jsonObject = Utils.createJsonTaskSendSsidPass("config", "wekastrandom", "87654321");
            socketController.initDstAddrPort(dstAddress, dstPort);

            waitWifiConnection();

            try {
                socketController.sendTask(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }

            wifiController.saveWifiConfig(ssidPass[0], ssidPass[1]);
            wifiController.switchFromWifiToAP();
            wifiController.changeState(WifiController.WifiState.WIFI_STATE_AP);

//            wifiController.getDongleIp();
        }
    }

    private final String TAG = "DongleService";
    private WifiController wifiController;
    private SocketController socketController;
    private CommandController commandController;

    public WifiController getWifiController() {
        return wifiController;
    }

    public SocketController getSocketController() {
        return socketController;
    }

    public CommandController getCommandController() {
        return commandController;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiController = new WifiController(getApplicationContext());
        wifiController.saveWifiConfig(DONGLE_AP_SSID_DEFAULT, DONGLE_AP_PASS_DEFAULT);
        commandController = new CommandController(this);
        socketController = new SocketController(commandController);

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
                // Connecting to Dongle default Access Point
                wifiController.connectToAccessPoint();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wifiController.saveGatewayIP();
                wifiController.changeState(WifiController.WifiState.WIFI_STATE_CONNECT);

                thread = new ServiceThread();
                thread.start();

                // send config
//                sendConfigToDongle();

//                wifiController.switchFromWifiToAP();
//                wifiController.changeState(WifiController.WifiState.WIFI_STATE_AP);

//                wifiController.getDongleIp();

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
//    private void networkManipulations() {
        // Saving wifi and access point states before launching application
//        saveWifiAdapterState();

        // Connecting to default Dongle Access Point
//        Wifi wifi = new Wifi(this);
//        wifi.connectToAccessPoint();

        // Saving to SharedPreferences current ip of dongle (access point)
//        saveDongleIp();

        // Send to dongle new ssid and pass
//        DongleReconfig reconfigDongle = new DongleReconfig(this);
//        reconfigDongle.reconfigure();

//        waitWileDongleReceiveNewConfig();

        // Create and run Access Point with new ssid and pass
//        accessPoint = new AccessPoint(this);
//        accessPoint.createAccessPoint();

        // Saving to SharedPreferences current ip of dongle (wifi client)
        // TODO: wait while donle will connect to Access Point of Client
//        saveDongleIp();
//    }

    /**
     * Saving to SharedPreferences current ip of dongle
     */
//    private void saveDongleIp() {
//        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//        String curDongleIp = wifiControllerOld.getIpAddr(dhcpInfo.serverAddress);
//        Utils.setFieldSP(getApplicationContext(), "DONGLE_IP", curDongleIp);
//        Utils.setFieldSP(getApplicationContext(), "DONGLE_PORT", "8888");
//    }

//    private void saveWifiAdapterState() {
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
//    }

//    private void waitWileDongleReceiveNewConfig() {
//        String isConfigSended = "";
//        boolean received = false;
//        while (!received) {
//            isConfigSended = Utils.getFieldSP(getApplicationContext(), "IS_CONFIG_SENDED");
//            if (isConfigSended.equals("1")) {
//                received = true;
//                Utils.setFieldSP(getApplicationContext(), "IS_CONFIG_SENDED", "0");
//            } else {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    Log.d(TAG, "WelcomeActivity.waitWileDongleReceiveNewConfig():  " + e);
//                }
//            }
//        }
//    }

    private void waitWifiConnection() {
        if (!wifiController.isWifiEnabled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitWifiConnection();
        }
    }

//    private boolean isWifiConnected() {
//        if (!wifiController.isWifiEnabled()) {
//
//        }
//        return false
//    }

    private String[] generateRandomSsidPass() {
        Random random = new Random();
        String[] ssidPass = new String[2];
        ssidPass[0] = randomSsid(random);
        ssidPass[1] = String.valueOf(random.nextInt(99999999 - 10000000) + 10000000);
        ssidPass[0] = "wekastrandom";
        ssidPass[1] = "87654321";
        return ssidPass;
    }

    public static String randomSsid(Random random) {
        int MAX_LENGTH = 8;
        StringBuilder randomStringBuilder = new StringBuilder();
//        int randomLength = random.nextInt(MAX_LENGTH - 10) +10;
        char tempChar;
        for (int i = 0; i < MAX_LENGTH; i++){
            tempChar = (char) (random.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

//    private void sendConfigToDongle() {
//        Socket socket = null;
        // TODO: save dongle IP when connected to default dongle AP
//        String dstAddress = Utils.getFieldSP(getApplicationContext(), "DONGLE_CUR_IP");
//        String dstAddress = "192.168.43.1";
//        String dstAddress = Utils.getFieldSP(getApplicationContext(), "DONGLE_IP");
//        String dstPort = DONGLE_SOCKET_PORT;

//        OutputStream outputStream = null;
//        InputStream inputStream = null;
//        JSONObject jsonObject = Utils.createJsonTaskSendSsidPass("config", "wekastrandom", "87654321");
//        int inputStreamBytes = 0;

//        SenderTasksToDongle senderTasksToDongle = new SenderTasksToDongle(dstAddress, dstPort, jsonObject, getApplicationContext());
//        senderTasksToDongle.start();


//        try {
//            socketController.initDstAddrPort(dstAddress, dstPort);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        socketController.sendTask(jsonObject);

//        try {
//            socket = new Socket(dstAddress, Integer.valueOf(dstPort));
//
//            // Send TASK to Dongle
//            outputStream = socket.getOutputStream();
//            outputStream.write(jsonObject.toString().getBytes());
//            outputStream.flush();
//
//            inputStream = socket.getInputStream();
////            int isAvailable = inputStream.available();
//            inputStreamBytes = inputStream.available();
//
//            Log.d(TAG, "SendTaskToDongle.doInBackground() JSON: " + jsonObject.toString());
//
////            response = "JSON: " + jsonObject.toString();
//
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            Log.d(TAG, "SendTaskToDongle.doInBackground() UnknownHostException: " + e.getMessage());
////            response = "UnknownHostException: " + e.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d(TAG, "SendTaskToDongle.doInBackground() IOException: " + e.getMessage());
////            response = "IOException: " + e.toString();
//        } finally {
//            try {
//                inputStream.close();
//                outputStream.close();
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.d(TAG, "SendTaskToDongle.doInBackground() IOException: " + e.getMessage());
////                response = "IOException: " + e.toString();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.d(TAG, "SendTaskToDongle.doInBackground() Exception: " + e.getMessage());
////                response = "Exception: " + e.toString();
//            }
//            Log.d(TAG, "SendTaskToDongle.doInBackground(): Finished");
//        }
//    }

    //    private void uploadPresentationToDongle(String presPath) {
//        String curPresPath = presPath;
//        JSONObject task = Utils.createJsonTask("uploadFile");
//        // TODO: why ip 192.168.1.1? must be 192.168.43.48
////        String curDongleIp = Utils.getFieldSP(context, "DONGLE_IP");
//        String curDongleIp = "192.168.43.48";
////        String curDongleIp = "192.168.43.248";
//        String curDonglePort = Utils.getFieldSP(context, "DONGLE_PORT");
//        Utils.setFieldSP(context, "EZS_TO_DONGLE_PATH", presPath);
//        SenderTasksToDongle dongleSenderTasks = new SenderTasksToDongle(curDongleIp, curDonglePort, task , context);
//        dongleSenderTasks.start();
//        int i = 0;
//    }

}
