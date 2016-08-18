package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity {
    private TextView tvWelcome;
    private ArrayList<String> filesLocal = new ArrayList<>();
    private HashMap<String, String> mapDownload = new HashMap<>();
    Context context = this;
    AccessServiceAPI m_AccessServiceAPI;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + Utils.getFieldSP(context, "login"));
        m_AccessServiceAPI = new AccessServiceAPI();
        ListView presenterList = (ListView) findViewById(R.id.presenterList);

        String answer = getIntent().getStringExtra("answer");

        //для загрузки
        mapDownload = Utils.parseJSONArrayMap(context, answer);

        //для адаптера
        filesLocal =  Utils.getAllFilesLocal();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, filesLocal);
        presenterList.setAdapter(adapter);

        mappingPresentations();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            finishAffinity();
        else
            Utils.toastShow(context, "Press once again to exit!");
        back_pressed = System.currentTimeMillis();
    }

    public void btnClearPref_Click(View v) {
        Utils.clearSP(context);
        Utils.toastShow(context, "Preference cleared");
    }

    public void btnDownload_Click(View v) {
        String login = Utils.getFieldSP(context, "login");
        String password = Utils.getFieldSP(context, "password");
        if(mapDownload.size() > 0)
        m_AccessServiceAPI.taskDownload(login, password, mapDownload, context);
        else Utils.toastShow(context, "You havn't presentations on server!");
    }

    private void mappingPresentations() {
        if (mapDownload.size() > 0) {
            for (String s: filesLocal) {
                for(Iterator<HashMap.Entry<String, String>> it = mapDownload.entrySet().iterator(); it.hasNext(); ) {
                    HashMap.Entry<String, String> entry = it.next();
                    if (entry.getValue().equals(s)) {
                        it.remove();
                    }
                }
            }
        }
    }
}
