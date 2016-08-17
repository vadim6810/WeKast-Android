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
public class SendTaskToDongle extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "wekastClient";
    String dstAddress;
    String dstPort;
    String response = "";
    TextView msg;
    JSONObject jsonObject;
    Context context;
    int inputStreamBytes = 0;

    public SendTaskToDongle(String addr, String port, JSONObject jsonObject, Context context) {
        dstAddress = addr;
        dstPort = port;
        this.jsonObject = jsonObject;
        this.context = context;
        Log.d(TAG, addr + ":" + port);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Thread.currentThread().setName("SendTaskToDongle");
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            socket = new Socket(dstAddress, Integer.valueOf(dstPort));
            inputStream = socket.getInputStream();
//            int isAvailable = inputStream.available();
            inputStreamBytes = inputStream.available();
            // Send TASK to Dongle
            outputStream = socket.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.flush();

            Log.d(TAG, "JSON: " + jsonObject.toString());

            response = "JSON: " + jsonObject.toString();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } catch (Exception e) {
                e.printStackTrace();
                response = "Exception: " + e.toString();
            }
            Log.i("SendTaskToDongle", "doInBackground: Finished");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d(TAG, "response: " + response);
        Utils.toastShow(context, "TASK sended, inputStreamBytes: " + inputStreamBytes);
    }
}
