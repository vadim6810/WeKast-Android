package com.wekast.wekastandroidclient.controllers;

import android.util.Log;

import com.wekast.wekastandroidclient.activity.WelcomeActivity;
import com.wekast.wekastandroidclient.commands.ICommand;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.services.DongleService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class SocketController {

    public static final String TAG = "SocketController";
    private Socket socket;
    private String dstAddr;
    private int dstPort;
    public boolean FILE_UPLOADED = false;
    private WelcomeActivity activity = WelcomeActivity.welcomeActivity;
    private DongleService dongleService;
    private CommandController commandController;

    public SocketController(DongleService dongleService, CommandController commandController) {
        this.dongleService = dongleService;
        this.commandController = commandController;
    }

    public void initDstAddrPort(String dstAddr, String dstPort) {
        this.dstAddr = dstAddr;
        this.dstPort = Integer.valueOf(dstPort);
    }

    public boolean sendTask(ICommand command) throws IOException {
//    public boolean sendTask(String command) throws IOException {

        Log.e(TAG, "curCommand: " + command);
        Log.e(TAG, "curCommand name: " + command.getCommand());
        Log.e(TAG, "curDstAddr: " + this.dstAddr);

        if (command == null)
            System.out.println("command == null");

//        if (command.equals("{\"command\":\"ping\"}")) {
        if (command.getCommand().equals("ping")) {
            if (this.dstAddr.equals("")) {
                if (!reconfigDevices()) {
//                    showMessage("Error reconfig Devices");
                    Log.e(TAG, "Error reconfig Devices");
                    return false;
                }
                // TODO: check if needed
                return true;
            }
            if (socket == null)
                socket = new Socket(this.dstAddr, this.dstPort);
        } else {

            if (this.dstAddr.equals(""))
                return false;
            else
                socket = new Socket(this.dstAddr, this.dstPort);
        }

        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter printWriter = new PrintWriter(outputStream, true);
//            printWriter.println(command);
            printWriter.println(command.getJsonString());
//            showMessage("request: " + command);
//            Log.i("SocketController", "sendTask command=" + command);
            Log.e(TAG, "sendTask command=" + command.getJsonString());

            while (true) {
                String task = br.readLine();
                if (task == null || task.equals("")) {
                    socket.close();
                    Log.e(TAG, "socket.close()");
                    break;
                }
//                showMessage("response: " + task);
                Log.e(TAG, "sendTask response=" + task);

                // TODO: think about command response
                // parse response and get message (if "ok")
                try {
                    JSONObject jsonObject1 = new JSONObject(task);
                    String type = jsonObject1.getString("type");
                    String message = jsonObject1.getString("message");

                    if (type.equals("ping")) {
                        if (!message.equals("ok"))
                            reconfigDevices();
                    }
                    if (message.equals("ok")) {
                        Log.e(TAG, "Request received OK");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception1 message: " + e.getMessage());
                    e.printStackTrace();
                }

                socket.close();
                break;
            }
        } catch (SocketException e) {
            Log.e(TAG, "Exception2 message: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Exception3 message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        return true;
    }

    public void sendFile(String filePath) throws IOException {
        socket = new Socket(this.dstAddr, 9999);
//        showMessage("Sending presentation...");
        Log.e(TAG, "Sending presentation...");
        File myFile = new File (filePath);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        OutputStream os = socket.getOutputStream();
        os.write(mybytearray,0,mybytearray.length);
        os.flush();
//        showMessage("send file: " + filePath);

        //RESPONSE FROM THE SERVER
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        in.ready();
        String response = in.readLine();

        try {
            JSONObject jsonObject1 = new JSONObject(response);
            String type = jsonObject1.getString("type");
            String message = jsonObject1.getString("message");
            if (type.equals("file")) {
                if (message.equals("ok")) {
//                    showMessage("Dongle received file");
                    Log.e(TAG, "Dongle received file");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Exception5 message: " + e.getMessage());
            e.printStackTrace();
        }

        if (!dongleService.reconfigDevice()) {
            // error reconfig
        }
        socket.close();
    }

    public void close() {
        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception4 message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

//    public void showMessage(final String message) {
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                Utils.toastShow(activity, message);
//            }
//        });
//    }


    private boolean reconfigDevices() {
        //TODO: if SharedPreferences contain key
//        showSharedPreferencesVariables();
        Boolean ClientSsidExist = Utils.getContainsSP(dongleService.getWifiController().getContext(), "ACCESS_POINT_SSID_ON_APP");
        if (!ClientSsidExist) {
            if (!dongleService.connectToDefaultAP()) {
//                showMessage("Error connect to default AP");
                Log.e(TAG, "Error connect to default AP");
                return false;
            }
            dongleService.sendConfigToDongle();
        } else {
            dongleService.generateRandomSsidPass();
        }
        if (!dongleService.reconfigDevice())
            return false;
        return true;
    }

//    private void showSharedPreferencesVariables() {
//        SharedPreferences sp = Utils.getSharedPreferences(dongleService.getApplicationContext());
//        Map<String, ?> mapSP = sp.getAll();
//        for (Map.Entry<String, ?> entry : mapSP.entrySet()) {
//            System.out.println(entry.getKey() + "/" + entry.getValue());
//        }
//    }

}
