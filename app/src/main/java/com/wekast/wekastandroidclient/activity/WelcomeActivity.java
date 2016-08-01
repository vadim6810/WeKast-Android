package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity {
    private static final String LOG_TAG = "WelcomeActivity = ";
    private TextView tvWelcome;
    private ArrayList<String> arrayList = new ArrayList<>();
    private Map<String, String> mapList = new HashMap<>();
    private AccessServiceAPI m_AccessServiceAPI;
    private ProgressDialog m_ProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + getIntent().getStringExtra("login"));

        //для адаптера (возможно будет убрано)
        parseJSONArray(getIntent().getStringExtra("answer"));

        //для загрузки
        parseJSONArrayMap(getIntent().getStringExtra("answer"));

        // получаем элемент ListView
        ListView countriesList = (ListView) findViewById(R.id.presenterList);

        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);

        // устанавливаем для списка адаптер
        countriesList.setAdapter(adapter);
    }

    private void parseJSONArrayMap(String answer) {
        try {
            JSONArray jsonArray = new JSONArray(answer);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject index = jsonArray.getJSONObject(i);
                mapList.put(index.getString("id"), index.getString("name"));
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void parseJSONArray(String answer) {
        try {
            JSONArray jsonArray = new JSONArray(answer);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject index = jsonArray.getJSONObject(i);
                arrayList.add(index.getString("name"));
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public void btnDownload_Click(View v) {
        m_AccessServiceAPI = new AccessServiceAPI();
        SharedPreferences settingsActivity = getSharedPreferences(Utils.SHAREDPREFERNCE, MODE_PRIVATE);
        String login = settingsActivity.getString("login", "");
        String password = settingsActivity.getString("password", "");
        new DownloadTask().execute(login, password);
    }

    class DownloadTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Open progress dialog during downloading
            m_ProgressDialog = ProgressDialog.show(WelcomeActivity.this, "Please wait...", "Downloading...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> param = new HashMap<>();
            param.put("login", params[0]);
            param.put("password", params[1]);
            for (Map.Entry<String, String> item : mapList.entrySet()) {
                try {
                    byte[] content = m_AccessServiceAPI.getDownloadWithParam_POST(Utils.SERVICE_API_URL_DOWNLOAD + item.getKey(), param);
                    writeFile(content, item.getValue());
                } catch (IOException e) {
                    return Utils.RESULT_ERROR;
                }
            }
            return Utils.RESULT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            m_ProgressDialog.dismiss();
            if (result == Utils.RESULT_SUCCESS) {
                toastShow("Download completed.");
            } else {
                toastShow("Download fail!!!");
            }
        }

        private void toastShow(String s) {
            Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        private void writeFile(byte[] content, String FILENAME) {
            Log.d(LOG_TAG, "writeToFile");
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(new File(Utils.DIRECTORY, FILENAME));
                fos.write(content);
                fos.flush();
                fos.close();
                Log.d(LOG_TAG, "finish write!!!");
            } catch (IOException e) {
                Log.d(LOG_TAG, "error write!!!");
            }
        }

    }
}
