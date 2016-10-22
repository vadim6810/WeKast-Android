package com.wekast.wekastandroidclient.controllers;

import android.util.Log;

import com.wekast.wekastandroidclient.commands.WelcomeAnswer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
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
//                while (true) {
                    String task = br.readLine();
                    if (task == null || task.equals("")) {
                        socket.close();
//                        break;
                    }
//                    printWriter.println(commandController.processTask(task));
//                    if (Thread.interrupted()) {
//                        return;
//                    }
//                }
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
