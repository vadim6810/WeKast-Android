package com.wekast.wekastandroidclient.services;

import android.app.Service;
import android.content.Intent;
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
    private ServiceThread2 thread2;

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

            //wait while AP is loading and Client connecting
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wifiController.saveConnectedDeviceIp();
            dstAddress = Utils.getFieldSP(getApplicationContext(), "DONGLE_IP");
            socketController.initDstAddrPort(dstAddress, dstPort);

            jsonObject = Utils.createJsonTaskFile();
            try {
                socketController.sendTask(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServiceThread2 extends Thread {

        private JSONObject jsonObject;

        ServiceThread2(JSONObject jsonObject) {
            setDaemon(true);
            setName("DongleServiceThread2");
            this.jsonObject = jsonObject;
        }

        @Override
        public void run() {
            String dstAddress = Utils.getFieldSP(getApplicationContext(), "DONGLE_IP");
            String dstPort = DONGLE_SOCKET_PORT;
            socketController.initDstAddrPort(dstAddress, dstPort);
            try {
                socketController.sendTask(jsonObject);
//                socketController.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    Thread.sleep(5000);
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
                Log.d(TAG, "readIntent: SLIDE " + intent.getStringExtra("SLIDE"));
                String curSlide = intent.getStringExtra("SLIDE");
                JSONObject jsonObject = Utils.createJsonTaskSlide(curSlide);
                thread2 = new ServiceThread2(jsonObject);
                thread2.start();
                break;
            default:
                Log.d(TAG, "readIntent:  NO COMMAND" );
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
        char tempChar;
        for (int i = 0; i < MAX_LENGTH; i++){
            tempChar = (char) (random.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

//        private void uploadPresentationToDongle(String presPath) {
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
