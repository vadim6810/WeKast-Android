package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Client;
import com.wekast.wekastandroidclient.model.Server;
import com.wekast.wekastandroidclient.model.WifiApControl;

/**
 * Created by ELAD on 7/27/2016.
 */
public class TestActivity extends Activity {
    private static final String TAG = "wekastClient";
    TextView infoip;
    Button buttonClear;
    Button buttonUpdateService;
    public TextView msg;
    WifiApControl apControl;
    Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//
//        createWiFiHotspot();
        initViewElements();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TestActivity.onDestroy()");
        // server.onDestroy();
        // TODO: save wifi status on startup. If wifi active -> don't switch off wifi
        //        apControl.setWifiApEnabled(false);
    }

//    private void createWiFiHotspot() {
//        // TODO: add to SharedPreferences dongle IP
//        apControl.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
//        apControl.configureWifiConfig(getText(R.string.ssid).toString(), getText(R.string.pass).toString());
//        apControl.turnOnOffHotspot(this, true, apControl);
//    }

    private void initViewElements() {
        infoip = (TextView) findViewById(R.id.infoip);
        buttonClear = (Button) findViewById(R.id.clearButton);
        buttonUpdateService = (Button) findViewById(R.id.updateServiceButton);
        msg = (TextView) findViewById(R.id.msg);
        server = new Server(this);

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.setText("");
                server.message = "";
            }
        });

        buttonUpdateService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                msg.setText("");
//                Client myClient = new Client(editTextAddress.getText().toString(),
//                        Integer.parseInt(editTextPort.getText().toString()), response);
                Client myClient = new Client("192.168.43.48", 8888, msg);
                myClient.execute();
            }
        });

//        while(!apControl.isWifiApEnabled()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        infoip.setText(server.getIpAddress()+":"+server.getPort());
    }

    public void btnBackClick(View view) {
        Intent i = new Intent(this, ListActivity.class);
        startActivity(i);
    }
}
