package com.wekast.wekastandroidclient.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.wekast.wekastandroidclient.activity.list.FragmentListPresentations;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.wekast.wekastandroidclient.model.AccessServiceAPI.convertJSONString2Obj;
import static com.wekast.wekastandroidclient.model.AccessServiceAPI.getDownloadWithParam_POST;
import static com.wekast.wekastandroidclient.model.AccessServiceAPI.getJSONStringWithParam_POST;
import static com.wekast.wekastandroidclient.model.Utils.AP_PASS_KEY;
import static com.wekast.wekastandroidclient.model.Utils.AP_SSID_KEY;
import static com.wekast.wekastandroidclient.model.Utils.CHECK;
import static com.wekast.wekastandroidclient.model.Utils.DELETE;
import static com.wekast.wekastandroidclient.model.Utils.DIRECTORY;
import static com.wekast.wekastandroidclient.model.Utils.DIRECTORY_PREVIEW;
import static com.wekast.wekastandroidclient.model.Utils.DOWNLOAD;
import static com.wekast.wekastandroidclient.model.Utils.ERROR_DOWNLOAD;
import static com.wekast.wekastandroidclient.model.Utils.LOGIN;
import static com.wekast.wekastandroidclient.model.Utils.PASSWORD;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_DELETE;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_DOWNLOAD;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_GETSETTINGS;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_LIST;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_PREVIEW;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_SETSETTINGS;
import static com.wekast.wekastandroidclient.model.Utils.SETTINGS;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_FINISH_ALL;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_FINISH_ONE;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_FINISH_PREVIEW;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_START;
import static com.wekast.wekastandroidclient.model.Utils.UPDATE;
import static com.wekast.wekastandroidclient.model.Utils.clearDirectory;
import static com.wekast.wekastandroidclient.model.Utils.getAllFilesList;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.mapEzsForDeleted;
import static com.wekast.wekastandroidclient.model.Utils.mapEzsForDownload;
import static com.wekast.wekastandroidclient.model.Utils.parseJSONArrayMap;
import static com.wekast.wekastandroidclient.model.Utils.setFieldSP;

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";
    private HashMap<String, String> hashMap = new HashMap<>();

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getIntExtra("command", 0)) {
            case DOWNLOAD:
                download();
                break;
            case DELETE:
                delete(intent.getStringArrayListExtra("serverEzsDel"));
                break;
            case SETTINGS:
                settings(intent.getIntExtra("settings", 0));
                break;
            default:
                Log.d(TAG, "onHandleIntent: NO COMMAND");
        }
    }

    private void settings(int settings) {
        String ssid = getFieldSP(this, AP_SSID_KEY);
        String pass = getFieldSP(this, AP_PASS_KEY);
        Log.d(TAG, "client settings: ssid = " + ssid + " pass = " + pass);
        switch (settings) {
            case CHECK:
                if (ssid.equals("")) {
                    String[] res = getServerSettings();
                    if (!res[0].equals("")) {
                        setFieldSP(this, AP_SSID_KEY, res[0]);
                        setFieldSP(this, AP_PASS_KEY, res[1]);
                        Log.d(TAG, "settings saved on client");
                    }
                }
                break;
            case UPDATE:
                setServerSettings(ssid, pass);
                break;
        }
    }

    private void setServerSettings(String ssid, String pass) {
        HashMap<String, String> param = new HashMap<>();
        param.put(LOGIN, getFieldSP(this, LOGIN));
        param.put(PASSWORD, getFieldSP(this, PASSWORD));
        param.put("sid", ssid);
        param.put("pass", pass);
        for (int i = 0; i < 10; i++) {
            Log.e(TAG, "timeout: " + i);
            try {
                String response = getJSONStringWithParam_POST(SERVICE_API_URL_SETSETTINGS, param);
                JSONObject jsonObject = convertJSONString2Obj(response);
                if (jsonObject.getInt("status") == 0) {
                    Log.d(TAG, "setServerSettings: " + jsonObject.getString("answer"));
                    break;
                } else {
                    Log.d(TAG, "ERROR status");
                    break;
                }
            } catch (Exception e) {
                Log.e(TAG, "set settings ERROR");

            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] getServerSettings() {
        HashMap<String, String> param = new HashMap<>();
        param.put(LOGIN, getFieldSP(this, LOGIN));
        param.put(PASSWORD, getFieldSP(this, PASSWORD));
        String[] serSettings = new String[]{"",""};
        try {
            String response = getJSONStringWithParam_POST(SERVICE_API_URL_GETSETTINGS, param);
            JSONObject jsonObject = convertJSONString2Obj(response);
            if (jsonObject.getInt("status") == 0) {
                serSettings[0] = jsonObject.getJSONObject("answer").getString("sid");
                serSettings[1] = jsonObject.getJSONObject("answer").getString("pass");
            } else {
                Log.d(TAG, "ERROR status");
            }
        } catch (Exception e) {
            Log.e(TAG, "get settings ERROR");
        }
        Log.d(TAG, "server settings: " + serSettings[0] + ":" + serSettings[1]);
        return serSettings;
    }

    private void delete(ArrayList<String> serverEzsDel) {

        HashMap<String, String> param = new HashMap<>();
        param.put(LOGIN, getFieldSP(this, LOGIN));
        param.put(PASSWORD, getFieldSP(this, PASSWORD));
        //get EZS on Server
        try {
            String response = getJSONStringWithParam_POST(SERVICE_API_URL_LIST, param);
            JSONObject jsonObject = convertJSONString2Obj(response);
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
        if (!hashMap.isEmpty()) {
            for (Map.Entry<String, String> item : hashMap.entrySet()) {
                try {
                    String response2 = getJSONStringWithParam_POST(SERVICE_API_URL_DELETE + item.getKey(), param);
                    JSONObject jsonObject = convertJSONString2Obj(response2);
                    if (jsonObject.getInt("status") != 0) {
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
            String response = getJSONStringWithParam_POST(SERVICE_API_URL_LIST, param);
            JSONObject jsonObject = convertJSONString2Obj(response);

            if (jsonObject.getInt("status") == 0) {
                response = jsonObject.getString("answer");
                hashMap = mapEzsForDownload(parseJSONArrayMap(response), getAllFilesList());
            } else {
                Log.d(TAG, "download: ERROR status" + jsonObject.getInt("status"));
            }
        } catch (Exception e) {
            Log.e(TAG, "download: ERROR " + e.getMessage());
        }

        //download from server
        if (!hashMap.isEmpty()) {
            try {
                for (Map.Entry<String, String> item : hashMap.entrySet()) {
                    //download preview
                    actionDownload(SERVICE_API_URL_PREVIEW + item.getKey(), param, item.getValue(), DIRECTORY_PREVIEW);
                    intent.putExtra("status", STATUS_FINISH_PREVIEW);
                    sendBroadcast(intent);

                    //download EZS
                    actionDownload(SERVICE_API_URL_DOWNLOAD + item.getKey(), param, item.getValue(), DIRECTORY);
                    clearDirectory(new File(DIRECTORY_PREVIEW, item.getValue()));
                    intent.putExtra("status", STATUS_FINISH_ONE);
                    sendBroadcast(intent);
                }
            } catch (Exception e) {
                Log.d(TAG, "download: ERROR " + e.getMessage());
                intent.putExtra("status", ERROR_DOWNLOAD);
                sendBroadcast(intent);
            }
            Log.d(TAG, "download: ALL OK");
        }
        // сообщаем об окончании задачи
        intent.putExtra("status", STATUS_FINISH_ALL);
        sendBroadcast(intent);
    }

    private void actionDownload(String URL, HashMap<String, String> param, String fileName, File pathSave)
            throws IOException {
        File tmpFile = new File(pathSave, fileName + ".tmp");
        Log.d(TAG, "actionDownload: " + tmpFile);
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            getDownloadWithParam_POST(URL, param, fos);
            tmpFile.renameTo(new File(pathSave, fileName));
        }
    }
}
