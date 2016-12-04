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
import com.wekast.wekastandroidclient.controllers.CommandController;
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
import static com.wekast.wekastandroidclient.model.Utils.TIME_TO_TRYING_SEND_PRESENTATION;
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
            WifiController.WifiState curState = curState = wifiController.getSavedWifiState();
            switch (curServiceTask) {
                case UPLOAD:
                    // TODO: ping if answered then continue else reconfigDevice before
                    // If connection with dongle not exist -> reconfig devices
                    setDstAddrAndPort();            // remove becouse in sending also exist setDstAddrAndPort()

                    if (!sendTaskToDongle(new PingCommand().getJsonString())) {
                        wifiController.showMessage("Error uploading presentation");
//                        Utils.toastShow(wifiController.getContext(), "Error upload presentation");
                        break;
                        // show message that error uploading presentation
                    }
                    // wait for response from dongle that he received task

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
                    // TODO: check if connection established? then send slide
                    if (curState.equals(WifiController.WifiState.WIFI_STATE_CONNECT)) {
                        boolean fileUploaded = checkIfFileUploaded();
                        if (fileUploaded)
                            sendTaskToDongle(new SlideCommand(curSlide, curMedia).getJsonString());
//                    sendTaskToDongle(Utils.createJsonTaskSlide(curSlide));
//                    setDstAddrAndPort();
                        break;
                    }
                case STOP:
                    if (curState.equals(WifiController.WifiState.WIFI_STATE_CONNECT)) {
                        sendTaskToDongle(new StopCommand().getJsonString());
                    }
                default:
                    Log.d(TAG, "COMMAND NOT FOUND");
            }
            this.interrupt();
        }

        long startTimeCheckIfFileUploaded = 0L;
        private boolean checkIfFileUploaded() {
            if (startTimeCheckIfFileUploaded == 0L)
                startTimeCheckIfFileUploaded = System.currentTimeMillis();

            int passedTime = (int) (System.currentTimeMillis() - startTimeCheckIfFileUploaded) / 1000;
            if (passedTime > TIME_TO_TRYING_SEND_PRESENTATION)
                return false;

//            String fileUploadStatus = Utils.getFieldSP(getApplicationContext(), "FILE_UPLOAD");
            boolean isFileUploaded = socketController.FILE_UPLOADED;
            if (!isFileUploaded) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!checkIfFileUploaded())
                    return false;
            }
            return true;
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
        Thread.currentThread().setName("DongleService");
        wifiController = new WifiController(getApplicationContext());
        commandController = new CommandController(this);
        socketController = new SocketController(this, commandController);
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
        Utils.removeFromSharedPreferences(getApplicationContext(), "FILE_UPLOAD");
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

    public String[] generateRandomSsidPass() {
        Random random = new Random();
        String[] ssidPass = new String[2];
        ssidPass[0] = randomSsid(random);
        ssidPass[1] = String.valueOf(random.nextInt(99999999 - 10000000) + 10000000);
        ssidPass[0] = "wekastrandom";
        ssidPass[1] = "87654321";
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
                Utils.toastShow(activity, message);
            }
        });
    }

    // Connecting to Dongle default Access Point
    public boolean connectToDefaultAP() {
        wifiController.saveWifiConfig(DONGLE_AP_SSID_DEFAULT, DONGLE_AP_PASS_DEFAULT);
        if (!wifiController.connectToAccessPoint()) {
            showMessage("Error connecting to dongle");
            return false;
        }

        if (wifiController.saveGatewayIP()){
//            showMessage("Dongle IP saved");
        } else {
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

    public boolean reconfigDevice() {
        wifiController.switchFromWifiToAP();
        wifiController.changeState(WifiController.WifiState.WIFI_STATE_AP);
        // TODO: broadcast receiver
        if (!wifiController.saveConnectedDeviceIp())
            return false;
        return true;
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
//                showMessage("Error sending task: " + command);
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
            Log.e("WEKAST.DONGLE", "Error sending presentation to dongle");
        }
    }

}
