package com.wekast.wekastandroidclient.models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.wekast.wekastandroidclient.model.SendTaskToDongle;
import com.wekast.wekastandroidclient.model.SenderTasksToDongle;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONObject;

/**
 * Created by YEHUDA on 8/20/2016.
 */
public class DongleReconfig {
    private static final String TAG = "wekastlog";
    private Context mainActivityContext = null;
    private Activity mainActivity = null;


    public DongleReconfig(Activity activity) {
        this.mainActivity = activity;
        this.mainActivityContext = mainActivity.getApplicationContext();
    }

    public boolean reconfigure() {
        // in curDongleIp 192.168.1.1 -> must be 192.168.43.1
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
//        SendTaskToDongle dongleSendTask = new SendTaskToDongle(curDongleIp, curDonglePort, task , mainActivityContext);
//        dongleSendTask.execute();

        SenderTasksToDongle dongleSenderTasks = new SenderTasksToDongle(curDongleIp, curDonglePort, task , mainActivityContext);
        dongleSenderTasks.start();



//        Thread socketServerThread = new Thread(new SocketDongleServerThread());
//        socketServerThread.setName("DongleSocketServer");
//        socketServerThread.start();

        // wait while dongle receive new AP config
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Log.d(TAG, "DongleReconfig.reconfigure():  " + e.getMessage());
//        }
        return true;
    }

}
