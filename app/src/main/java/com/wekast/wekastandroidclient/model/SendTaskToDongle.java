package com.wekast.wekastandroidclient.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by ELAD on 7/27/2016.
 */
//public class SendTaskToDongle extends AsyncTask<Void, Void, Void> {
public class SendTaskToDongle extends AsyncTask<Void, Void, String> {
    private static final String TAG = "wekastlog";
    String dstAddress;
    String dstPort;
//    String response = "";
    TextView msg;
    JSONObject jsonObject;
    Context context;
    int inputStreamBytes = 0;

    public SendTaskToDongle(String addr, String port, JSONObject jsonObject, Context context) {
        dstAddress = addr;
        dstPort = port;
        this.jsonObject = jsonObject;
        this.context = context;
        Log.d(TAG, "SendTaskToDongle " + addr + ":" + port + " - " + jsonObject);
    }

    @Override
    protected String doInBackground(Void... args) {
        Thread.currentThread().setName("SendTaskToDongle");
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;

        String response;
        try {
            socket = new Socket(dstAddress, Integer.valueOf(dstPort));

            // Send TASK to Dongle
            outputStream = socket.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.flush();

            inputStream = socket.getInputStream();
//            int isAvailable = inputStream.available();
            inputStreamBytes = inputStream.available();

            Log.d(TAG, "SendTaskToDongle.doInBackground() JSON: " + jsonObject.toString());

            response = "JSON: " + jsonObject.toString();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, "SendTaskToDongle.doInBackground() UnknownHostException: " + e.getMessage());
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "SendTaskToDongle.doInBackground() IOException: " + e.getMessage());
            response = "IOException: " + e.toString();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "SendTaskToDongle.doInBackground() IOException: " + e.getMessage());
                response = "IOException: " + e.toString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "SendTaskToDongle.doInBackground() Exception: " + e.getMessage());
                response = "Exception: " + e.toString();
            }
            Log.d(TAG, "SendTaskToDongle.doInBackground(): Finished");
        }
//        return null;
//        return;
//        return null;
        return response;
    }

    @Override
//    protected void onPostExecute(Void result) {
    protected void onPostExecute(String response) {
//        super.onPostExecute(result);
        Log.d(TAG, "SendTaskToDongle.onPostExecute() response: " + response);
        Utils.toastShow(context, "TASK sended, inputStreamBytes: " + inputStreamBytes);

    }
}
