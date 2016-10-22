package com.wekast.wekastandroidclient.model;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by ELAD on 10/1/2016.
 */

public class SenderTasksToDongle extends Thread {

    private static final String TAG = "wekastlog";
    String dstAddress;
    String dstPort;
    //    String response = "";
    TextView msg;
    JSONObject jsonObject;
    Context context;
    int inputStreamBytes = 0;

    public SenderTasksToDongle(String addr, String port, JSONObject jsonObject, Context context) {
        dstAddress = addr;
        dstPort = port;
        this.jsonObject = jsonObject;
        this.context = context;
        Log.d(TAG, "SenderTasksToDongle " + addr + ":" + port + " - " + jsonObject);
    }

    @Override
    public void run() {
        String response;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
//        try(Socket socket = new Socket(dstAddress, Integer.valueOf(dstPort));
//            OutputStream outputStream = socket.getOutputStream();
//            InputStream inputStream = socket.getInputStream();) {
        try {
            // TODO: first connection to some another host
            socket = new Socket(dstAddress, Integer.valueOf(dstPort));
            outputStream = socket.getOutputStream();

            inputStream = socket.getInputStream();

            PrintWriter printWriter = new PrintWriter(outputStream, true);
            printWriter.println(jsonObject.toString());
//            printWriter.flush();
//            printWriter.close();

//            inputStreamBytes = inputStream.available();
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//            String curMessage = br.readLine();
            // if ok and task uploadFile -> uploadFile

//            String curResponseStatus = Utils.getResponseStatus(Utils.createJsonTask(curMessage));
//            String curCommand = Utils.getTaskCommand(jsonObject);

//            if (curCommand.equals("accessPointConfig")) {
//                Utils.setFieldSP(context, "IS_CONFIG_SENDED", "1");
//            }




            Log.d(TAG, "SenderTasksToDongle.send() JSON: " + jsonObject.toString());
        } catch (UnknownHostException e) {
            Log.d(TAG, "SenderTasksToDongle.send() UnknownHostException: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "SenderTasksToDongle.send() IOException: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
