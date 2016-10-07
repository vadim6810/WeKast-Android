package com.wekast.wekastandroidclient.activity.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.activity.LoginActivity;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RDL on 03.10.2016.
 */

public class SettingsActivity extends Activity  implements AdapterView.OnItemClickListener{

    private ArrayList<HashMap<String, Object>> mSettingsList;
    private String[] sTitle;
    private String[] sSubtitle;
    private Object[] sIcon;
    private static final String TITLE = "title";
    private static final String SUBTITLE = "subtitle";
    private static final String ICON = "icon";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ListView listView = (ListView) findViewById(R.id.listSettings);

        sTitle = getResources().getStringArray(R.array.settings_title);
        sSubtitle = getResources().getStringArray(R.array.settings_subtitle);
        sIcon = new Object[] {
                R.drawable.ic_phone,
                R.drawable.ic_curt,
                R.drawable.ic_share,
                R.drawable.ic_help,
                R.drawable.ic_mail,
                R.drawable.ic_info,
                R.drawable.ic_sync,
                R.drawable.ic_settings
        };

        mSettingsList = new ArrayList<>();
        HashMap<String, Object> hm;
        for(int i = 0; i < sTitle.length; i++){
            hm = new HashMap<>();
            hm.put(TITLE, sTitle[i]); // Название
            hm.put(SUBTITLE, sSubtitle[i]); // Описание
            hm.put(ICON, sIcon[i]); // Картинка
            mSettingsList.add(hm);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mSettingsList,
                R.layout.activity_settingsitems, new String[]{TITLE, SUBTITLE, ICON},
                new int[]{R.id.text1, R.id.text2, R.id.img});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(i);
                break;
            case 8:
                Utils.clearSP(this);
                Utils.toastShow(this, "All settings cleared.");
                break;
            default:
                Utils.toastShow(this, "Pushed item " + position);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    public void onClickClose(MenuItem item) {
        finish();
    }
}
