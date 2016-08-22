package com.wekast.wekastandroidclient.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by Meztiros on 01.08.2016.
 */
public class Utils {

    public static final String SERVICE_API_URL = "http://78.153.150.254";
    public static final String SERVICE_API_URL_LIST = SERVICE_API_URL + "/list";
    public static final String SERVICE_API_URL_REGISTER = SERVICE_API_URL + "/register";
    public static final String SERVICE_API_URL_DOWNLOAD = SERVICE_API_URL + "/download/";
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ERROR = -1;
    public static final String SHAREDPREFERNCE = "WeKastPreference";
    public static final String DEFAULT_PATH_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String WORK_DIRECTORY = "WeKast/";
    public static File DIRECTORY = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);


    // SharedPreferences params
    // DONGLE_IP        // set when connecting to dongle access point for sending new ssid and pass
    // DONGLE_PORT      // set when connecting to dongle access point for sending new ssid and pass
    // WIFI_STATE_BEFORE_LAUNCH_APP             // save state of wifi module
    // ACCESS_POINT_STATE_BEFORE_LAUNCH_APP     // save state of access point
    // ACCESS_POINT_SSID_NEW         // new value of ssid
    // ACCESS_POINT_PASS_NEW         // new value of pass


    public static void initWorkFolder() {
        File file = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
        if (!file.isDirectory()) {
            file.mkdir();
            Log.d("Create directory", DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
        }
    }

    public static boolean getContainsSP(Context context, String field) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        return  settingsActivity.contains(field);
    }
    public static String getFieldSP(Context context, String field) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        String login = settingsActivity.getString(field, "");
        return  login;
    }

    public static void clearSP(Context context) {
        SharedPreferences settingsActivity = context.getSharedPreferences(Utils.SHAREDPREFERNCE, context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settingsActivity.edit();
        prefEditor.clear();
        prefEditor.commit();
    }

    public static void setFieldSP(Context context, String field1, String field2) {
        SharedPreferences settingsActivity = context.getSharedPreferences(Utils.SHAREDPREFERNCE, context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settingsActivity.edit();
        prefEditor.putString(field1, field2);
        prefEditor.apply();
    }

    public static void toastShow(Context context, String s) {
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void writeFile(byte[] content, String FILENAME, String LOG_TAG) {
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

    public static HashMap<String, String> parseJSONArrayMap(String answer) {
        HashMap<String, String> mapList = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(answer);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject index = jsonArray.getJSONObject(i);
                mapList.put(index.getString("id"), index.getString("name"));
            }
        } catch (JSONException e) {
            Log.d("parseJSONArrayMap ", "error!!!");
        }
        return mapList;
    }

    public static ArrayList<String> parseJSONArray(Context context, String answer) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(answer);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject index = jsonArray.getJSONObject(i);
                arrayList.add(index.getString("name"));
            }
        } catch (JSONException e) {
            toastShow(context, e.toString());
        }
        return arrayList;
    }

    public static ArrayList<String> getAllFilesLocal() {
        ArrayList<String> fileList = new ArrayList<>();
        File[] filesList = DIRECTORY.listFiles();;
        if (filesList != null && filesList.length > 0) {
            for (int i = 0; i < filesList.length; i++) {
                    fileList.add(filesList[i].getName());
            }
        }
        return fileList;
    }

    public static JSONObject createJsonTaskSendSsidPass(String task, String ssid, String pass) {
        // TODO: create rundom ssid and pass
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonTask = new JSONArray();
        JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("command", task);
            jsonCommand.put("ssid", ssid);
            jsonCommand.put("pass", pass);
            jsonTask.put(jsonCommand);
            jsonObject.put("device", "android");
            jsonObject.put("task", jsonTask);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static HashMap<String, String>  mappingPresentations(HashMap<String, String> mapDownload, ArrayList<String> filesLocal) {
        if (mapDownload.size() > 0) {
            for (String s: filesLocal) {
                for(Iterator<HashMap.Entry<String, String>> it = mapDownload.entrySet().iterator(); it.hasNext(); ) {
                    HashMap.Entry<String, String> entry = it.next();
                    if (entry.getValue().equals(s)) {
                        it.remove();
                    }
                }
            }
        }
        return mapDownload;
    }
}
