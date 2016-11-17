package com.wekast.wekastandroidclient.controllers;

import android.util.Log;

import com.wekast.wekastandroidclient.activity.WelcomeActivity;
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

    public static final String TAG = "DongleSocket";
    private Socket socket;
    private String dstAddr;
    private int dstPort;
    public boolean FILE_UPLOADED = false;
    private WelcomeActivity activity = WelcomeActivity.welcomeActivity;
    private DongleService dongleService;

    public SocketController(DongleService dongleService) {
        this.dongleService = dongleService;
    }

    public void initDstAddrPort(String dstAddr, String dstPort) {
        this.dstAddr = dstAddr;
        this.dstPort = Integer.valueOf(dstPort);
    }

    public boolean sendTask(String command) throws IOException {
        if (command.equals("{\"command\":\"ping\"}")) {
            if (this.dstAddr.equals("")) {
                if (!reconfigDevices()) {
                    showMessage("Error reconfig Devices");
                    return false;
                }
                return true;
            }
            if (socket == null)
                socket = new Socket(this.dstAddr, this.dstPort);
        } else
            socket = new Socket(this.dstAddr, this.dstPort);

        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter printWriter = new PrintWriter(outputStream, true);
            printWriter.println(command);
            showMessage("request: " + command);

            while (true) {
                String task = br.readLine();
                if (task == null || task.equals("")) {
                    socket.close();
                    break;
                }
                showMessage("response: " + task);

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
                        Log.i("SocketController", "Request received OK");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.close();

            }
        } catch (SocketException e) {
            Log.i(TAG, "Socket closed: interrupting");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        return true;
    }

    public void sendFile(String filePath) throws IOException {

        socket = new Socket(this.dstAddr, 9999);
        // sendfile
        File myFile = new File (filePath);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        OutputStream os = socket.getOutputStream();
        os.write(mybytearray,0,mybytearray.length);
        os.flush();
        showMessage("send file: " + filePath);

        //RESPONSE FROM THE SERVER
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        in.ready();
        String response = in.readLine();
        showMessage("response: " + response);
//        System.out.println("Response from server..." + response);

        dongleService.reconfigDevice();
        socket.close();
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("SocketController", "Error closing socket");
            }
        }
    }

    public void showMessage(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Utils.toastShow(activity, message);
            }
        });
    }


    private boolean reconfigDevices() {
        if (!dongleService.connectToDefaultAP()) {
            showMessage("Error connect to default AP");
            return false;
        }
        dongleService.sendConfigToDongle();
        dongleService.reconfigDevice();
        return true;
    }

}
