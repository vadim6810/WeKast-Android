package com.wekast.wekastandroidclient.activity;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.wekast.wekastandroidclient.activity.list.FragmentListPresentations;
import com.wekast.wekastandroidclient.activity.slider.FragmentSlider;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.CustomPhoneStateListener;
import com.wekast.wekastandroidclient.model.ProccesCall;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.services.DongleService;
import com.wekast.wekastandroidclient.services.DownloadService;

import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends AppCompatActivity implements FragmentListPresentations.onSomeEventListener {
    private static final String TAG = "WelcomeActivity";

    public static WelcomeActivity welcomeActivity;
    Context context = this;
    private int activityState;
    private BroadcastReceiver processCall;
    FragmentListPresentations fragmentListPresentations;
    FragmentTransaction fragmentTransaction;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeActivity = this;

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setLogo(R.drawable.logo);
//        getSupportActionBar().setDisplayUseLogoEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //sync SSID & PASS with SERVER
        startSyncSettings();

        fragmentListPresentations = new FragmentListPresentations();
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmContainer, fragmentListPresentations);
//        fragmentTransaction.addToBackStack(null);
        activityState = PRESENTATION_LIST;
        fragmentTransaction.commit();


//        testRemoveConnectionWithDongle();
//        showSharedPreferencesVariables();
        clearSharedPreferencesValues();
        // TODO: CHECK on 4.4.2 when application tryed to reinstall was error (with requestSettingsPermissions)
        if (requestSettingsPermissions()) {
            Log.d(TAG, "onCreate: requestSettingsPermissions TRUE");
//            startService(new Intent(this, DongleService.class));
        }
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
        uploadPresentationToDongle(presPath);
    }

    private void uploadPresentationToDongle(String presPath) {
        Intent i = new Intent(this, DongleService.class);
        i.putExtra("command", UPLOAD);
        i.putExtra("UPLOAD", presPath);
        startService(i);
    }

    private void initProccesCall() {
        Log.d(TAG, "initProccesCall()");
        processCall = new ProccesCall();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(processCall, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (activityState == SLIDER) {
            Log.d(TAG, "onStart:processCall()");
            initProccesCall();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (activityState == SLIDER) {
            Log.d(TAG, "onStop:processCall()");
            CustomPhoneStateListener.blockingCall = false;
            unregisterReceiver(processCall);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DongleService.class));
        super.onDestroy();
    }

    private void startSyncSettings() {
        Intent i = new Intent(this, DownloadService.class);
        i.putExtra("command", SETTINGS);
        i.putExtra("settings", CHECK);
        startService(i);
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
                stopPresentation();
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

    private void stopPresentation() {
        Intent i = new Intent(this, DongleService.class);
        i.putExtra("command", STOP);
        i.putExtra("stop", "1");
        startService(i);
    }

    private void clearSharedPreferencesValues() {
        Utils.removeFromSharedPreferences(context, "DONGLE_IP");
    }

    private boolean requestSettingsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);
                return false;
            }
        }
        return true;
    }

//    private void showSharedPreferencesVariables() {
//        SharedPreferences sp = Utils.getSharedPreferences(context);
//        Map<String, ?> mapSP = sp.getAll();
//        for (Map.Entry<String, ?> entry : mapSP.entrySet()) {
//            System.out.println(entry.getKey() + "/" + entry.getValue());
//        }
//    }

//    private void testRemoveConnectionWithDongle() {
//        Utils.removeFromSharedPreferences(context, "ACCESS_POINT_SSID_ON_APP");
//        Utils.removeFromSharedPreferences(context, "ACCESS_POINT_PASS_ON_APP");
//    }

}
