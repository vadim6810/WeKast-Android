package com.wekast.wekastandroidclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity{
    TextView tvWelcome;
    ArrayList<String> arrayList = new ArrayList<>(); //= { "Бразилия", "Аргентина", "Колумбия", "Чили", "Уругвай"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView)findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + getIntent().getStringExtra("login"));

        parseJSONArray(getIntent().getStringExtra("answer"));

        // получаем элемент ListView
        ListView countriesList = (ListView) findViewById(R.id.presenterList);

        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1, arrayList);

        // устанавливаем для списка адаптер
        countriesList.setAdapter(adapter);
    }

    private void parseJSONArray(String answer) {
        try {
            JSONArray jsonArray = new JSONArray(answer);
            for( int i = 0; i < jsonArray.length(); i++){
                JSONObject index = jsonArray.getJSONObject(i);
                arrayList.add(index.getString("name"));
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void btnBack_Click(View v){
      finish();
    }
}
