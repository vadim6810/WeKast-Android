package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.wekast.wekastandroidclient.controllers.AccessPointController;
import com.wekast.wekastandroidclient.controllers.WifiController;
import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.models.AccessPoint;
import com.wekast.wekastandroidclient.models.DongleReconfig;
import com.wekast.wekastandroidclient.models.Wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvWelcome;
    private ArrayList<String> filesLocal = new ArrayList<>();
    private HashMap<String, String> mapDownload = new HashMap<>();
    Context context = this;
    AccessServiceAPI m_AccessServiceAPI;
    private static long back_pressed;
    private String answer;
    ListView presenterList;
    ArrayAdapter<String> adapter;

    WifiManager wifiManager = null;
    WifiController wifiController = null;
    AccessPointController accessPointController = null;
    AccessPoint accessPoint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + Utils.getFieldSP(context, "login"));
        m_AccessServiceAPI = new AccessServiceAPI();
        presenterList = (ListView) findViewById(R.id.presenterList);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        answer = getIntent().getStringExtra("answer");
       //список презентаций
        initPresenterList();

        //синхронизация с сервером
        initDownload();

        //какойто код )))
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiController = new WifiController(wifiManager);
        accessPointController = new AccessPointController(wifiManager);

        new Thread(new Runnable() {
            public void run() {
                // TODO: think where better to place networkManipulations, maybe at the end
                // Manipulations with network
                networkManipulations();
            }
        }).start();

//        new Thread(() ->  networkManipulations()).start();

        presenterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Utils.toastShow(context, ((TextView) view).getText().toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

       // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_clear:
                Utils.clearSP(context);
                Utils.toastShow(context, "Preference cleared");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        initDownload();
    }

    private void initPresenterList() {
        //для адаптера
        filesLocal =  Utils.getAllFilesLocal();
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, filesLocal);
        presenterList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void initDownload() {
        mapDownload = Utils.parseJSONArrayMap(answer);
        filesLocal =  Utils.getAllFilesLocal();
        mapDownload = Utils.mappingPresentations(mapDownload, filesLocal);
        String login = Utils.getFieldSP(context, "login");
        String password = Utils.getFieldSP(context, "password");
        if(mapDownload.size() > 0)
            new TaskDownload().execute(login, password);
        else Utils.toastShow(context, "You havn't new presentations on server!");
    }

    public class TaskDownload extends AsyncTask<String, Void, Integer> {
        String LOG_TAG = "WelcomeActivity = ";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            initPresenterList();
        }

        @Override
        protected Integer doInBackground(String... params) {
            HashMap<String, String> param = new HashMap<>();
            param.put("login", params[0]);
            param.put("password", params[1]);
            for (Map.Entry<String, String> item : mapDownload.entrySet()) {
                try {
                    byte[] content = m_AccessServiceAPI.getDownloadWithParam_POST(Utils.SERVICE_API_URL_DOWNLOAD + item.getKey(), param);
                    Utils.writeFile(content, item.getValue(), LOG_TAG);
                    publishProgress();
                } catch (Exception e) {
                    return Utils.RESULT_ERROR;
                }
            }
            return Utils.RESULT_SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            initPresenterList();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == Utils.RESULT_SUCCESS) {
                Utils.toastShow(context, "Download completed.");
            } else {
                Utils.toastShow(context, "Download fail!!!");
            }
            initPresenterList();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: think if needed
        accessPoint.destroyAccessPoint();
        restoreWifiAdapterState();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            finishAffinity();
        else
            Utils.toastShow(context, "Press once again to exit!");
        back_pressed = System.currentTimeMillis();
    }

    /**
     * Manipulations with network
     */
    private void networkManipulations() {
        // Saving wifi and access point states before launching application
        saveWifiAdapterState();

        // Connecting to default Dongle Access Point
        Wifi wifi = new Wifi(this);
        wifi.connectToAccessPoint();

        // Saving to SharedPreferences current ip of dongle (access point)
        saveDongleIp();

        // Send to dongle new ssid and pass
        DongleReconfig reconfigDongle = new DongleReconfig(this);
        reconfigDongle.reconfigure();

        // Create and run Access Point with new ssid and pass
        accessPoint = new AccessPoint(this);
        accessPoint.createAccessPoint();

        // Saving to SharedPreferences current ip of dongle (wifi client)
        saveDongleIp();
    }

    /**
     * Saving to SharedPreferences current ip of dongle
     */
    private void saveDongleIp() {
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String curDongleIp = wifiController.getIpAddr(dhcpInfo.serverAddress);
        Utils.setFieldSP(context, "DONGLE_IP", curDongleIp);
        Utils.setFieldSP(context, "DONGLE_PORT", "8888");
    }

    private void saveWifiAdapterState() {
        // TODO: save current state of wifi and access point. If enabled save current working wifi
        Boolean isWifiEnabled = wifiController.isWifiOn(this);
        // TODO: save access point state to isAccessPointEnabled
        if(isWifiEnabled) {
            Utils.setFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP", isWifiEnabled.toString());
            Utils.setFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP", "false");
            // TODO: save connected wifi ssid
        } else {
            Utils.setFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP", isWifiEnabled.toString());
            Utils.setFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP", "false");
        }
    }

    private void restoreWifiAdapterState() {
        // TODO: finish this method
        String isWifiEnabled = Utils.getFieldSP(this, "WIFI_STATE_BEFORE_LAUNCH_APP");
//        if (isWifiEnabled.equals("true")) {
        wifiController.turnOnOffWifi(context, true);
//        }

//        String isAccessPointEnabled = Utils.getFieldSP(this, "ACCESS_POINT_STATE_BEFORE_LAUNCH_APP");
//        accessPointController.setAccessPointEnabled(context, isAccessPointEnabled);
    }

}
