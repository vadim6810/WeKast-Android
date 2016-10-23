package com.wekast.wekastandroidclient.services;

import android.app.IntentService;

import android.content.Intent;
import android.util.Log;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.wekast.wekastandroidclient.model.Utils.*;


public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";
    private AccessServiceAPI m_AccessServiceAPI = new AccessServiceAPI();
    private HashMap<String, String> hashMap = new HashMap<>();

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent start" );
        download();
        Log.d(TAG, "onHandleIntent end" );
    }

    private void download() {
        HashMap<String, String> param = new HashMap<>();
        param.put(LOGIN, getFieldSP(this, LOGIN));
        param.put(PASSWORD, getFieldSP(this, PASSWORD));
        //getListOnServer
        try {
            String response = m_AccessServiceAPI.getJSONStringWithParam_POST(SERVICE_API_URL_LIST, param);
            JSONObject jsonObject = m_AccessServiceAPI.convertJSONString2Obj(response);
            if (jsonObject.getInt("status") == 0) {
                response = jsonObject.getString("answer");
                hashMap = mapEzsForDownload(parseJSONArrayMap(response), getAllFilesList());
            } else {
                Log.d(TAG, "download: ERROR status");
            }
        } catch (Exception e) {
            Log.d(TAG, "download: ERROR getListOnServer");
        }

        //download from server
        for (Map.Entry<String, String> item : hashMap.entrySet()) {
            try {
                byte[] content = m_AccessServiceAPI.getDownloadWithParam_POST(SERVICE_API_URL_DOWNLOAD + item.getKey(), param);
                writeFile(content, item.getValue(), TAG);

            } catch (Exception e) {
                Log.d(TAG, "download: ERROR download from server");
            }
        }
        Log.d(TAG, "download: OK");
    }
}
