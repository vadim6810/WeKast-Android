package com.wekast.wekastandroidclient.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RDL on 03.10.2016.
 */

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

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
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            sSubtitle[6] = "Ver " + versionName + "  |  Latest Version";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SettingsActivity", "versionName: " + e.toString());
        }

        sIcon = new Object[]{
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
        for (int i = 0; i < sTitle.length; i++) {
            hm = new HashMap<>();
            hm.put(TITLE, sTitle[i]); // Название
            hm.put(SUBTITLE, sSubtitle[i]); // Описание
            hm.put(ICON, sIcon[i]); // Картинка
            mSettingsList.add(hm);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mSettingsList,
                R.layout.settings_item, new String[]{TITLE, SUBTITLE, ICON},
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
            case 1:
                openlink("http://wekast.com/order/#.WDcjEPmLRPZ");
                break;
            case 2:
                String textTitle = "Share WeKast";
                String textMessage = "http://wekast.com/";
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, textMessage);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, textTitle));
                break;
            case 3:
                openlink("http://wekast.com/faqs/");
                break;
            case 4:
                openlink("http://wekast.com/contactus/");
                break;
            case 5:
                openlink("http://wekast.com/privacy-policy-2/");
                break;
            case 7:
                Utils.clearSP(this);
                Utils.toastShow(this, "All settings cleared.");
                break;
            default:
                Utils.toastShow(this, "Pushed item " + position);
                break;
        }
    }

    private void openlink(String url) {
        Uri address = Uri.parse(url);
        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openlinkIntent);
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
