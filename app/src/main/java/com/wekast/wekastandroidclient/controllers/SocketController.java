package com.wekast.wekastandroidclient.controllers;

import android.util.Log;

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

    public void initDstAddrPort(String dstAddr, String dstPort) {
        this.dstAddr = dstAddr;
        this.dstPort = Integer.valueOf(dstPort);
    }

    public void sendTask(String command) throws IOException {
        socket = new Socket(this.dstAddr, this.dstPort);
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter printWriter = new PrintWriter(outputStream, true);
            printWriter.println(command);

            while (true) {
                String task = br.readLine();
                if (task == null || task.equals("")) {
                    socket.close();
                    break;
                }

                // TODO: think about command response
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
            }
        } catch (SocketException e) {
            Log.i(TAG, "Socket closed: interrupting");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        //RESPONSE FROM THE SERVER
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        in.ready();
        String userInput = in.readLine();
        System.out.println("Response from server..." + userInput);

        socket.close();
    }

    public void close() throws IOException {
        if (!socket.isClosed())
            socket.close();
    }

}
