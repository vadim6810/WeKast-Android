package com.wekast.wekastandroidclient.controllers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class SocketController {

    public static final String TAG = "DongleSocket";
    private CommandController commandController;
    private Socket socket;
    private String dstAddr;
    private int dstPort;

    public SocketController(CommandController commandController) {
        this.commandController = commandController;
    }

    public void initDstAddrPort(String dstAddr, String dstPort) {
        this.dstAddr = dstAddr;
        this.dstPort = Integer.valueOf(dstPort);
    }

    public void sendTask(JSONObject jsonObject) throws IOException {
//        if (socket != null)
//            socket.close();

        socket = new Socket(this.dstAddr, this.dstPort);
        try {
//            while (true) {
//                Socket socket = socket.accept();
//                InetAddress clientInetAddress = socket.getInetAddress();

//                InetAddress someAddr = socket.getInetAddress();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(outputStream, true);
                printWriter.println(jsonObject.toString());
//                WelcomeAnswer answer = new WelcomeAnswer();
//                printWriter.println(answer);
                while (true) {
                    String task = br.readLine();
                    if (task == null || task.equals("")) {
                        socket.close();
                        break;
                    }
                    // parse response and get message (if "ok")
                    try {
                        JSONObject jsonObject1 = new JSONObject(task);
                        String message = jsonObject1.getString("message");
                        if (message.equals("ok")) {
                            Log.i("SocketController", "Request received OK");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    socket.close();

//                    printWriter.println(commandController.processTask(task));
//                    if (Thread.interrupted()) {
//                        return;
//                    }
                }
//            }
        } catch (SocketException e) {
            Log.i(TAG, "Socket closed: interrupting");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (!socket.isClosed())
            socket.close();
    }

//    public boolean waitForFile() {
//        return false;
//    }

}
