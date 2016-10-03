package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wekast.wekastandroidclient.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RDL on 03.10.2016.
 */

public class SettingsActivity extends Activity  implements AdapterView.OnItemClickListener{

    private ArrayList<HashMap<String, Object>> mSettingsList;
    private static final String TITLE = "title";
    private static final String SUBTITLE = "subtitle";
    private static final String ICON = "icon";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ListView listView = (ListView) findViewById(R.id.listSettings);

        // создаем массив списков
        mSettingsList = new ArrayList<>();
        HashMap<String, Object> hm;

        hm = new HashMap<>();
        hm.put(TITLE, "Рыжик"); // Название
        hm.put(SUBTITLE, "Рыжий и хитрый"); // Описание
        hm.put(ICON, R.drawable.ic_curt); // Картинка
        mSettingsList.add(hm);

        hm = new HashMap<>();
        hm.put(TITLE, "Васька");
        hm.put(SUBTITLE, "Слушает да ест");
        hm.put(ICON, R.drawable.ic_share);
        mSettingsList.add(hm);

        hm = new HashMap<>();
        hm.put(TITLE, "Мурзик");
        hm.put(SUBTITLE, "Спит и мурлыкает");
        hm.put(ICON, R.drawable.ic_share);
        mSettingsList.add(hm);

        hm = new HashMap<>();
        hm.put(TITLE, "Барсик");
        hm.put(SUBTITLE, "Болеет за Барселону");
        hm.put(ICON, R.drawable.ic_share);
        mSettingsList.add(hm);

        SimpleAdapter adapter = new SimpleAdapter(this, mSettingsList,
                R.layout.activity_settingsitems, new String[]{TITLE, SUBTITLE, ICON},
                new int[]{R.id.text1, R.id.text2, R.id.img});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String, Object> itemHashMap = (HashMap <String, Object>) parent.getItemAtPosition(position);
            String titleItem = itemHashMap.get(TITLE).toString();
            String descriptionItem = itemHashMap.get(SUBTITLE).toString();
            int imageItem = (int)itemHashMap.get(ICON);
            Toast.makeText(getApplicationContext(),
                    titleItem + " : " + descriptionItem, Toast.LENGTH_SHORT).show();
    }



}
