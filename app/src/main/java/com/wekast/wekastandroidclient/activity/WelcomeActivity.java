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

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity {
    private TextView tvWelcome;
    private ArrayList<String> arrayList = new ArrayList<>();
    private HashMap<String, String> mapList = new HashMap<>();
    Context context = this;
    AccessServiceAPI m_AccessServiceAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + getIntent().getStringExtra("login"));

        //для адаптера (возможно будет убрано)
        arrayList = Utils.parseJSONArray(context, getIntent().getStringExtra("answer"));

        //для загрузки
        mapList = Utils.parseJSONArrayMap(context, getIntent().getStringExtra("answer"));

        // получаем элемент ListView
        ListView presenterList = (ListView) findViewById(R.id.presenterList);

        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);

        // устанавливаем для списка адаптер
        presenterList.setAdapter(adapter);
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public void btnDownload_Click(View v) {
        String login = Utils.getFieldSP(context, "login");
        String password = Utils.getFieldSP(context, "password");
        m_AccessServiceAPI.taskDownload(login, password, mapList, context);
    }
}
