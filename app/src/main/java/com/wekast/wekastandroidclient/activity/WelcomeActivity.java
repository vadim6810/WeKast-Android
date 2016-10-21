package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;


import com.wekast.wekastandroidclient.activity.list.FragmentListPresentations;
import com.wekast.wekastandroidclient.activity.slider.FragmentSlider;
import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiControllerOld;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.CustomPhoneStateListener;
import com.wekast.wekastandroidclient.model.ProccesCall;
import com.wekast.wekastandroidclient.model.SenderTasksToDongle;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.models.AccessPoint;
import com.wekast.wekastandroidclient.services.DongleService;

import org.json.JSONObject;

import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity implements FragmentListPresentations.onSomeEventListener {
    private static final String TAG = "wekastlog";

    private TextView tvWelcome;
    Context context = this;
    private int activityState;
    private BroadcastReceiver processCall;
    FragmentListPresentations fragmentListPresentations;
    FragmentTransaction fragmentTransaction;
    private static long back_pressed;

//    WifiManager wifiManager = null;
//    WifiControllerOld wifiControllerOld = null;
//    AccessPointController accessPointController = null;
//    AccessPoint accessPoint = null;

//    String curPresPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + getFieldSP(context, "login"));

        fragmentListPresentations = new FragmentListPresentations();
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmContainer, fragmentListPresentations);
//        fragmentTransaction.addToBackStack(null);
        activityState = PRESENTATION_LIST;
        fragmentTransaction.commit();

        startService(new Intent(this, DongleService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void someEvent(String presPath) {
        initProccesCall();
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmContainer, new FragmentSlider());
//        fragmentTransaction.addToBackStack(null);
        activityState = SLIDER;
        fragmentTransaction.commit();
        // uploadPresentationToDongle(presPath);
        uploadToDongle(presPath);
   }

    private void uploadToDongle(String presPath) {
        Intent i = new Intent(this, DongleService.class);
        i.putExtra("command", UPLOAD);
        i.putExtra("UPLOAD", presPath);
        startService(i);
    }

    private void initProccesCall() {
        Log.d(TAG, getClass().getSimpleName() + ":initProccesCall()");
        processCall = new ProccesCall();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(processCall, intentFilter);
    }

//    private void uploadPresentationToDongle(String presPath) {
//        String curPresPath = presPath;
//        JSONObject task = Utils.createJsonTask("uploadFile");
//        // TODO: why ip 192.168.1.1? must be 192.168.43.48
////        String curDongleIp = Utils.getFieldSP(context, "DONGLE_IP");
//        String curDongleIp = "192.168.43.48";
////        String curDongleIp = "192.168.43.248";
//        String curDonglePort = Utils.getFieldSP(context, "DONGLE_PORT");
//        Utils.setFieldSP(context, "EZS_TO_DONGLE_PATH", presPath);
//        SenderTasksToDongle dongleSenderTasks = new SenderTasksToDongle(curDongleIp, curDonglePort, task , context);
//        dongleSenderTasks.start();
//        int i = 0;
//    }

    @Override
    protected void onStart() {
        super.onStart();
        if(activityState == SLIDER) {
            Log.d(TAG, getClass().getSimpleName() + ":onStart:processCall");
            initProccesCall();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(activityState == SLIDER) {
            Log.d(TAG, getClass().getSimpleName() + ":onStop:processCall");
            CustomPhoneStateListener.blockingCall = false;
            unregisterReceiver(processCall);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        // TODO: think if needed
//        accessPoint.destroyAccessPoint();
//        restoreWifiAdapterState();
    }

    @Override
    public void onBackPressed() {
        switch (activityState) {
            case PRESENTATION_LIST:
                if (back_pressed + 2000 > System.currentTimeMillis())
                    finishAffinity();
                else
                    toastShow(context, "Please click BACK again to exit!");
                back_pressed = System.currentTimeMillis();
                break;
            case SLIDER:
                CustomPhoneStateListener.blockingCall = false;
                unregisterReceiver(processCall);
                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmContainer, fragmentListPresentations);
//                fragmentTransaction.addToBackStack(null);
                activityState = PRESENTATION_LIST;
                fragmentTransaction.commit();
                break;
        }
    }







//    private void restoreWifiAdapterState() {
//        // TODO: finish this method
//        String isWifiEnabled = getFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP");
////        if (isWifiEnabled.equals("true")) {
//        wifiControllerOld.turnOnOffWifi(context, true);
////        }
//
////        String isAccessPointEnabled = Utils.getFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP");
////        accessPointController.setAccessPointEnabled(context, isAccessPointEnabled);
//    }

}
