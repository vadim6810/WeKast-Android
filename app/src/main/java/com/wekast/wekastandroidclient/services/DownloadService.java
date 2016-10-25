package com.wekast.wekastandroidclient.services;

import android.app.IntentService;

import android.content.Intent;
import android.util.Log;

import com.wekast.wekastandroidclient.activity.list.FragmentListPresentations;
import com.wekast.wekastandroidclient.model.AccessServiceAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
        switch (intent.getIntExtra("command", 0)){
            case DOWNLOAD:
                download();
                break;
            case DELETE:
                delete(intent.getStringArrayListExtra("serverEzsDel"));
                break;
            default:
                Log.d(TAG, "onHandleIntent: NO COMMAND");
        }
    }

    private void delete(ArrayList<String> serverEzsDel) {

        HashMap<String, String> param = new HashMap<>();
        param.put(LOGIN, getFieldSP(this, LOGIN));
        param.put(PASSWORD, getFieldSP(this, PASSWORD));
            //getEZSOnServer
            try {
                String response = m_AccessServiceAPI.getJSONStringWithParam_POST(SERVICE_API_URL_LIST, param);
                JSONObject jsonObject = m_AccessServiceAPI.convertJSONString2Obj(response);
                if (jsonObject.getInt("status") == 0) {
                    response = jsonObject.getString("answer");
                    hashMap = mapEzsForDeleted(parseJSONArrayMap(response), serverEzsDel);
                } else {
                    Log.d(TAG, "delete ERROR status");
                }
            } catch (Exception e) {
                Log.d(TAG, "delete ERROR getEZSOnServer");
            }

            //delete EZS on server
        if(!hashMap.isEmpty()){
            for (Map.Entry<String, String> item : hashMap.entrySet()) {
                try {
                    String response2 = m_AccessServiceAPI.getJSONStringWithParam_POST(SERVICE_API_URL_DELETE + item.getKey(), param);
                    JSONObject jsonObject = m_AccessServiceAPI.convertJSONString2Obj(response2);
                    if (jsonObject.getInt("status") != 0){
                        Log.d(TAG, jsonObject.toString());
                    }
                } catch (Exception e) {
                    Log.d(TAG, "delete ERROR");
                }
            }
            Log.d(TAG, "delete OK");
        }
    }


    private void download() {
        // сообщаем о старте задачи
        Intent intent = new Intent(FragmentListPresentations.BROADCAST_ACTION);
        intent.putExtra("command", DOWNLOAD);
        intent.putExtra("status", STATUS_START);
        sendBroadcast(intent);

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
                Log.d(TAG, "download: ERROR " +e.getMessage());
            }

        //download from server
        if(!hashMap.isEmpty()) {
            try {
                for (Map.Entry<String, String> item : hashMap.entrySet()) {
                    //download preview
                    actionDownload(SERVICE_API_URL_PREVIEW + item.getKey(), param, item.getValue(), DIRECTORY_PREVIEW);
                    intent.putExtra("status", STATUS_FINISH_PREVIEW);
                    sendBroadcast(intent);

                    //download EZS
                    actionDownload(SERVICE_API_URL_DOWNLOAD + item.getKey(), param, item.getValue(), DIRECTORY);
                    intent.putExtra("status", STATUS_FINISH_ONE);
                    sendBroadcast(intent);
                }
            } catch (Exception e) {
                Log.d(TAG, "download: ERROR " +e.getMessage());
            }
            Log.d(TAG, "download: ALL OK");
        }
        // сообщаем об окончании задачи
        intent.putExtra("status", STATUS_FINISH_ALL);
        sendBroadcast(intent);
    }

    private void actionDownload(String URL, HashMap<String, String> param, String fileName, File pathSave)  {
        File tmpFile = new File(pathSave, fileName + ".tmp");
        Log.d(TAG, "actionDownload: " + tmpFile);
        try (FileOutputStream fos = new FileOutputStream(tmpFile)){
            m_AccessServiceAPI.getDownloadWithParam_POST(URL, param, fos);
            tmpFile.renameTo(new File(pathSave, fileName));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
