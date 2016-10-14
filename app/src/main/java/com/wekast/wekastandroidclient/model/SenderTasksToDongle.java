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
            String curCommand = Utils.getTaskCommand(jsonObject);

            if (curCommand.equals("accessPointConfig")) {
                Utils.setFieldSP(context, "IS_CONFIG_SENDED", "1");
            }

            // that means that dongle received request for receiving EZS file
//            if (curCommand.equals("uploadFile") && curResponseStatus.equals("ok")) {
//                // sending EZS file
//                printWriter.println(jsonObject.toString());
//            }
            if (curCommand.equals("uploadFile")) {
                // sending EZS file

                String curEzsPath = Utils.getFieldSP(context, "EZS_TO_DONGLE_PATH");
                // send file


                FileInputStream fileInputStream = null;
                BufferedInputStream bufferedInputStream = null;

                File myFile = new File(curEzsPath);
                byte [] mybytearray  = new byte [(int)myFile.length()];

                printWriter.println((int)myFile.length());

                fileInputStream = new FileInputStream(myFile);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(mybytearray,0,mybytearray.length);
                outputStream = socket.getOutputStream();
                System.out.println("Sending " + curEzsPath + "(" + mybytearray.length + " bytes)");
//                outputStream.write(mybytearray,0,mybytearray.length);
//                outputStream.write(mybytearray);
//                outputStream.flush();
//                printWriter.println(mybytearray);

                if (bufferedInputStream != null)
                    bufferedInputStream.close();

                // if outputStream closed here, not all bytes sended
                outputStream.close();



//                printWriter.println("FILE FILE FILE FILE FILE FILE FILE FILE FILE FILE");
            }

            inputStreamBytes = inputStream.available();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String curMessage = br.readLine();




//

//            outputStream.write(jsonObject.toString().getBytes());
//            outputStream.flush();
//            outputStream.close();

//            inputStreamBytes = inputStream.available();
//            inputStreamReader = new InputStreamReader(inputStream);
//            BufferedReader r = new BufferedReader(inputStreamReader);
//            String str = null;
//            StringBuilder sb = new StringBuilder(8192);
//            while ((str = r.readLine()) != null) {
//                sb.append(str);
//            }






//            inputStream = socket.getInputStream();
//            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//            BufferedReader r = new BufferedReader(inputStreamReader);
//            String str = null;
//            StringBuilder sb = new StringBuilder(8192);
//            while ((str = r.readLine()) != null) {
//                sb.append(str);
//            }
//              inputStreamBytes = inputStream.available();

            Log.d(TAG, "SenderTasksToDongle.send() JSON: " + jsonObject.toString());
            response = jsonObject.toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, "SenderTasksToDongle.send() UnknownHostException: " + e.getMessage());
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "SenderTasksToDongle.send() IOException: " + e.getMessage());
            response = "IOException: " + e.toString();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "SenderTasksToDongle.send(): Finished");
//        Utils.toastShow(context, "TASK sended, inputStreamBytes: " + inputStreamBytes);
    }

//    private class ReceiveTaskFromDongleThread extends Thread {
//        private Socket hostThreadSocket;
//
//        ReceiveTaskFromDongleThread(Socket socket) {
//            hostThreadSocket = socket;
//        }
//
//        @Override
//        public void run() {
//            OutputStream outputStream;
//            try {
//                outputStream = hostThreadSocket.getOutputStream();
//                PrintStream printStream = new PrintStream(outputStream);
//                printStream.print(response);
//                printStream.close();
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.d(TAG, "DongleService.SocketDongleServerReplyThread.run() IOException " + e.getMessage());
////                log.createLogger("DongleService.SocketDongleServerReplyThread.run() IOException " + e.getMessage());
//            }
//        }
//    }

}
