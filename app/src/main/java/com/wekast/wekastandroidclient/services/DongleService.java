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

import java.io.File;
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

        private int curServiceTask;
        private String curSlide;
        private String presentationPath;

        ServiceThread(int task) {
            setDaemon(true);
            setName("DongleServiceThread");
            this.curServiceTask = task;
            this.curSlide = "";
        }

        public void setCurSlide(String slide) {
            this.curSlide = slide;
        }

        public void setPresentationPath(String presentationPath) {
            this.presentationPath = presentationPath;
        }

        @Override
        public void run() {
            switch (curServiceTask) {
                case UPLOAD:
                    connectToDefaultAP();
                    sendConfigToDongle();
                    reconfigDevice();

                    File presentationFile = new File(presentationPath);
                    int fileSize = (int) presentationFile.length();

                    sendTaskToDongle(Utils.createJsonTaskFile(String.valueOf(fileSize)));
                    sendFileToDongle(presentationPath);
                    // Send file
                    // pending intent to activity when upload ready
                    socketController.FILE_UPLOADED = true;
                    break;
                case SLIDE:
                    checkIfFileUploaded();
                    sendTaskToDongle(Utils.createJsonTaskSlide(curSlide));
                    break;
                default:
                    Log.d(TAG, "COMMAND NOT FOUND");
            }
        }

        private void sendConfigToDongle() {
            setDstAddrAndPort();
            String[] ssidPass = generateRandomSsidPass();
            sendTaskToDongle(Utils.createJsonTaskSendSsidPass("config", ssidPass[0], ssidPass[1]));
        }

        private void sendTaskToDongle(JSONObject jsonObject) {
            setDstAddrAndPort();
            try {
                socketController.sendTask(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFileToDongle(String filePath) {
            try {
                socketController.sendFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setDstAddrAndPort() {
            String dstAddress = Utils.getFieldSP(wifiController.getContext(), "DONGLE_IP");
            String dstPort = DONGLE_SOCKET_PORT;
            socketController.initDstAddrPort(dstAddress, dstPort);
        }

        private void reconfigDevice() {
            wifiController.switchFromWifiToAP();
            wifiController.changeState(WifiController.WifiState.WIFI_STATE_AP);
            wifiController.saveConnectedDeviceIp();
        }

        private void connectToDefaultAP() {
            // Connecting to Dongle default Access Point
            wifiController.connectToAccessPoint();
            wifiController.saveGatewayIP();
            wifiController.changeState(WifiController.WifiState.WIFI_STATE_CONNECT);
        }

        private void checkIfFileUploaded() {
//            String fileUploadStatus = Utils.getFieldSP(getApplicationContext(), "FILE_UPLOAD");
            boolean isFileUploaded = socketController.FILE_UPLOADED;
//            if (!fileUploadStatus.equals("UPLOADED")) {
            if (!isFileUploaded) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkIfFileUploaded();
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        cleanSharedPreferences();

        switch (intent.getIntExtra("command", 0)){
            case UPLOAD:
                Log.d(TAG, "readIntent: UPLOAD " +intent.getStringExtra("UPLOAD"));
                thread = new ServiceThread(UPLOAD);
                thread.setPresentationPath(intent.getStringExtra("UPLOAD"));
                thread.start();
                break;
            case SLIDE:
                Log.d(TAG, "readIntent: SLIDE " + intent.getStringExtra("SLIDE"));
                String curSlide = intent.getStringExtra("SLIDE");
                thread = new ServiceThread(SLIDE);
                thread.setCurSlide(curSlide);
                thread.start();
                break;
            default:
                Log.d(TAG, "readIntent:  NO COMMAND" );
        }
    }

    private void cleanSharedPreferences() {
        Utils.setFieldSP(getApplicationContext(), "FILE_UPLOAD", "");
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
//        ssidPass[0] = randomSsid(random);
//        ssidPass[1] = String.valueOf(random.nextInt(99999999 - 10000000) + 10000000);
        ssidPass[0] = "wekastrandom";
        ssidPass[1] = "87654321";
        // TODO: check if it need
        wifiController.saveWifiConfig(ssidPass[0], ssidPass[1]);
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

}
