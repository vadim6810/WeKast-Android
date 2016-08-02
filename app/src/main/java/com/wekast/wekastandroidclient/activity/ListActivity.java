package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wekast.wekastandroidclient.R;

/**
 * Created by Meztiros on 31.07.2016.
 */
public class ListActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        // получаем экземпляр элемента ListView
        ListView listView = (ListView) findViewById(R.id.listView);

        // определяем массив типа String
        final String[] catNames = new String[]{
                "Презентация1", "Презентация2", "Презентация3", "Презентация4", "Презентация5",
                "Презентация6", "Презентация7", "Презентация8", "Презентация9", "Презентация10",
                "Презентация11", "Презентация12", "Презентация13"
        };

        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, catNames);

        listView.setAdapter(adapter);
    }


    public void btnWelcome_Click(View view) {
        Intent i = new Intent(ListActivity.this, LoginActivity.class);
        startActivity(i);
    }
}
