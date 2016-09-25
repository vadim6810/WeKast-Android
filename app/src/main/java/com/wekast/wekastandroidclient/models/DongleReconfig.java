package com.wekast.wekastandroidclient.models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.wekast.wekastandroidclient.model.SendTaskToDongle;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONObject;

/**
 * Created by YEHUDA on 8/20/2016.
 */
public class DongleReconfig {
    private static final String TAG = "wekastdongle";
    private Context mainActivityContext = null;
    private Activity mainActivity = null;


    public DongleReconfig(Activity activity) {
        this.mainActivity = activity;
        this.mainActivityContext = mainActivity.getApplicationContext();
    }

    public boolean reconfigure() {
        String curDongleIp = Utils.getFieldSP(mainActivityContext, "DONGLE_IP");
        String curDonglePort = Utils.getFieldSP(mainActivityContext, "DONGLE_PORT");
        // work
//        String curDongleIp = Utils.getFieldSP(mainActivityContext, "dongleIP");
//        String curDonglePort = Utils.getFieldSP(mainActivityContext, "donglePort");

        //        SendTaskToDongle dongleSendTask = new SendTaskToDongle(curDongleIp, curDonglePort, msg, createJsonTask("show", 1), context);
        // TODO: Generate rundom ssid and pass IF client don't have his own ssid
        String newSsid = "wekastrandom";
        String newPass = "87654321";

        // save new ssid and pass
        Utils.setFieldSP(mainActivityContext, "ACCESS_POINT_SSID_NEW", newSsid);
        Utils.setFieldSP(mainActivityContext, "ACCESS_POINT_PASS_NEW", newPass);

        JSONObject task = Utils.createJsonTaskSendSsidPass("accessPointConfig", newSsid, newPass);
        SendTaskToDongle dongleSendTask = new SendTaskToDongle(curDongleIp, curDonglePort, task , mainActivityContext);
        dongleSendTask.execute();

        // wait while dongle receive new AP config
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Log.d(TAG, "ListActivity.btnWelcome_Click():  " + e);
//        }
        return true;
    }

}