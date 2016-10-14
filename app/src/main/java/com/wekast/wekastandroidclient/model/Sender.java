package com.wekast.wekastandroidclient.model;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by ELAD on 10/4/2016.
 */

public class Sender {

    private static Context context;

    public Sender(Context context) {
        this.context = context;
    }


    public static void showOnDongle(int currentSlide) {
        JSONObject task = Utils.createJsonTaskShow(currentSlide);
        String curDongleIp = "192.168.43.48";
//        String curDongleIp = "192.168.43.248";

        String curDonglePort = Utils.getFieldSP(context, "DONGLE_PORT");
        SenderTasksToDongle dongleSenderTasks = new SenderTasksToDongle(curDongleIp, curDonglePort, task , context);
        dongleSenderTasks.start();
    }
}
