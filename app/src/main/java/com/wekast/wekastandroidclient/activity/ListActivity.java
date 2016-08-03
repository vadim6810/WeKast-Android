package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.WifiApControl;

/**
 * Created by Meztiros on 31.07.2016.
 */
public class ListActivity extends Activity {
    Context context;
    WifiApControl apControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initViewElements();
    }

    public void btnWelcome_Click(View view) {
        Intent i = new Intent(ListActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void initViewElements() {
        // определяем массив типа String
        final String[] catNames = new String[]{
                "Презентация1", "Презентация2", "Презентация3", "Презентация4", "Презентация5",
                "Презентация6", "Презентация7", "Презентация8", "Презентация9", "Презентация10",
                "Презентация11", "Презентация12", "Презентация13"
        };

        // получаем экземпляр элемента ListView
        ListView listView = (ListView) findViewById(R.id.listView);

        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, catNames);
        listView.setAdapter(adapter);
        // xxxx

        context = this;
        apControl = new WifiApControl((WifiManager) this.getSystemService(Context.WIFI_SERVICE));

        Button buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createWiFiHotspot();

            }
        });
    }

    private void createWiFiHotspot() {
        // TODO: add to SharedPreferences dongle IP
        apControl.configureWifiConfig(getText(R.string.ssid).toString(), getText(R.string.pass).toString());
        apControl.turnOnOffHotspot(context, true, apControl);
    }
}
