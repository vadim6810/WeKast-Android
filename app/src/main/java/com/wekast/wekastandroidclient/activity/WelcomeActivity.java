package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity {
    private TextView tvWelcome;
    private ArrayList<String> filesServer = new ArrayList<>();
    private ArrayList<String> filesLocal = new ArrayList<>();
    private HashMap<String, String> mapList = new HashMap<>();
    Context context = this;
    AccessServiceAPI m_AccessServiceAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + Utils.getFieldSP(context, "login"));

        String answer = getIntent().getStringExtra("answer");

        //для адаптера (возможно будет убрано)
        filesServer = Utils.parseJSONArray(context, answer);
        filesLocal =  Utils.getAllFilesLocal();

        //для загрузки
        mapList = Utils.parseJSONArrayMap(context, answer);

        // получаем элемент ListView
        ListView presenterList = (ListView) findViewById(R.id.presenterList);

        // создаем адаптер
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, filesServer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, filesLocal);

        // устанавливаем для списка адаптер
        presenterList.setAdapter(adapter);
    }

    public void btnClearPref_Click(View v) {
        Utils.clearSP(context);
        Utils.toastShow(context, "Preference cleared");
    }

    public void btnDownload_Click(View v) {
        String login = Utils.getFieldSP(context, "login");
        String password = Utils.getFieldSP(context, "password");
        if(mapList.size() > 0)
        m_AccessServiceAPI.taskDownload(login, password, mapList, context);
        else Utils.toastShow(context, "No presentation on SERVER!");
    }
}
