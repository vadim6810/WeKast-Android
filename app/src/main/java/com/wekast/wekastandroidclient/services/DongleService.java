package com.wekast.wekastandroidclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.wekast.wekastandroidclient.activity.WelcomeActivity;
import com.wekast.wekastandroidclient.commands.ConfigCommand;
import com.wekast.wekastandroidclient.commands.FileCommand;
import com.wekast.wekastandroidclient.commands.PingCommand;
import com.wekast.wekastandroidclient.commands.SlideCommand;
import com.wekast.wekastandroidclient.commands.StopCommand;
import com.wekast.wekastandroidclient.controllers.SocketController;
import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.model.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static com.wekast.wekastandroidclient.model.Utils.DONGLE_AP_PASS_DEFAULT;
import static com.wekast.wekastandroidclient.model.Utils.DONGLE_AP_SSID_DEFAULT;
import static com.wekast.wekastandroidclient.model.Utils.DONGLE_SOCKET_PORT;
import static com.wekast.wekastandroidclient.model.Utils.SLIDE;
import static com.wekast.wekastandroidclient.model.Utils.UPLOAD;
import static com.wekast.wekastandroidclient.model.Utils.STOP;

/**
 * Created by RDL on 20.10.2016.
 */

public class DongleService extends Service {

    private WelcomeActivity activity = WelcomeActivity.welcomeActivity;

    private ServiceThread serviceThread;

    class ServiceThread extends Thread {

        private int curServiceTask;
        private String curSlide;
        private String curMedia;
        private String presentationPath;

        ServiceThread(int task) {
            setDaemon(true);
            setName("DonServThread");
            this.curServiceTask = task;
            this.curSlide = "";
        }

        public void setCurSlide(String slide)        {
            this.curSlide = slide;
        }

        public void setCurMedia(String media) {
            this.curMedia = media;
        }

        public void setPresentationPath(String presentationPath) {
            this.presentationPath = presentationPath;
        }

        @Override
        public void run() {
            switch (curServiceTask) {
                case UPLOAD:
                    // TODO: ping if answered then continue else reconfigDevice before
                    // If connection with dongle not exist -> reconfig devices
                    setDstAddrAndPort();
                    sendTaskToDongle(new PingCommand().getJsonString());
//                    connectToDefaultAP();
//                    sendConfigToDongle();
//                    reconfigDevice();

                    socketController.FILE_UPLOADED = false;
                    File presentationFile = new File(presentationPath);
                    int fileSize = (int) presentationFile.length();

//                    sendTaskToDongle(Utils.createJsonTaskFile(String.valueOf(fileSize)));
                    sendTaskToDongle(new FileCommand(String.valueOf(fileSize)).getJsonString());
                    sendFileToDongle(presentationPath);
                    // Send file
                    // pending intent to activity when upload ready
                    socketController.FILE_UPLOADED = true;
                    break;
                case SLIDE:
                    checkIfFileUploaded();
//                    sendTaskToDongle(Utils.createJsonTaskSlide(curSlide));
//                    setDstAddrAndPort();
                    sendTaskToDongle(new SlideCommand(curSlide, curMedia).getJsonString());
                    break;
                case STOP:
                    sendTaskToDongle(new StopCommand().getJsonString());
                default:
                    Log.d(TAG, "COMMAND NOT FOUND");
            }
            this.interrupt();
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
//    private CommandController commandController;

    public WifiController getWifiController() {
        return wifiController;
    }

    public SocketController getSocketController() {
        return socketController;
    }

//    public CommandController getCommandController() {
//        return commandController;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.currentThread().setName("DongleService");
        wifiController = new WifiController(getApplicationContext());
        wifiController.saveWifiConfig(DONGLE_AP_SSID_DEFAULT, DONGLE_AP_PASS_DEFAULT);
        socketController = new SocketController(this);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        if (serviceThread != null)
            serviceThread.interrupt();
        wifiController.restore();
        if (socketController != null)
            socketController.close();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        readIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void readIntent(Intent intent) {
        cleanSharedPreferences();

        switch (intent.getIntExtra("command", 0)) {
            case UPLOAD:
                Log.d(TAG, "readIntent: UPLOAD " + intent.getStringExtra("UPLOAD"));
                serviceThread = new ServiceThread(UPLOAD);
                serviceThread.setPresentationPath(intent.getStringExtra("UPLOAD"));
                serviceThread.start();
                break;
            case SLIDE:
                Log.d(TAG, "readIntent: SLIDE " + intent.getStringExtra("SLIDE"));
                String curSlide = intent.getStringExtra("SLIDE");
                String curMedia = intent.getStringExtra("MEDIA");
                serviceThread = new ServiceThread(SLIDE);
                serviceThread.setCurSlide(curSlide);
                serviceThread.setCurMedia(curMedia);
                serviceThread.start();
                break;
            case STOP:
                serviceThread = new ServiceThread(STOP);
                serviceThread.start();
            default:
                Log.d(TAG, "readIntent:  NO COMMAND");
        }
    }

    private void cleanSharedPreferences() {
        Utils.setFieldSP(getApplicationContext(), "FILE_UPLOAD", "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    private void waitWifiConnection() {
//        if (!wifiController.isWifiEnabled()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            waitWifiConnection();
//        }
//    }

    private String[] generateRandomSsidPass() {
        Random random = new Random();
        String[] ssidPass = new String[2];
        ssidPass[0] = randomSsid(random);
        ssidPass[1] = String.valueOf(random.nextInt(99999999 - 10000000) + 10000000);
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
        for (int i = 0; i < MAX_LENGTH; i++) {
            tempChar = (char) (random.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void showMessage(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
//                Utils.toastShow(activity, message);
            }
        });
    }

    // Connecting to Dongle default Access Point
    public boolean connectToDefaultAP() {
        if (!wifiController.connectToAccessPoint()) {
            showMessage("Error connecting to dongle");
            return false;
        }

        if (wifiController.saveGatewayIP())
            showMessage("Dongle IP saved");
        else {
            showMessage("Dongle not reached");
            return false;
        }

        wifiController.changeState(WifiController.WifiState.WIFI_STATE_CONNECT);
        return true;
    }

    public void sendConfigToDongle() {
        setDstAddrAndPort();
        String[] ssidPass = generateRandomSsidPass();
//            ConfigCommand configCommand = new ConfigCommand(ssidPass[0], ssidPass[1]);
//            sendTaskToDongle(Utils.createJsonTaskSendSsidPass("config", ssidPass[0], ssidPass[1]));
        sendTaskToDongle(new ConfigCommand(ssidPass[0], ssidPass[1]).getJsonString());
    }

    public void reconfigDevice() {
        wifiController.switchFromWifiToAP();
        wifiController.changeState(WifiController.WifiState.WIFI_STATE_AP);
        // TODO: broadcast receiver
        wifiController.saveConnectedDeviceIp();
    }

    private void setDstAddrAndPort() {
        String dstAddress = Utils.getFieldSP(wifiController.getContext(), "DONGLE_IP");
        String dstPort = DONGLE_SOCKET_PORT;
        socketController.initDstAddrPort(dstAddress, dstPort);
    }

    private boolean sendTaskToDongle(String command) {
        setDstAddrAndPort();
        try {
            if (!socketController.sendTask(command)) {
                showMessage("Error sending task: " + command);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void sendFileToDongle(String filePath) {
        try {
            socketController.sendFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
