package com.wekast.wekastandroidclient.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Meztiros on 01.08.2016.
 */
public class Utils {

    public static final String SERVICE_API_URL = "http://78.153.150.254";
    public static final String EZS_LIST = SERVICE_API_URL + "/list";
    public static final String USER_REGISTER = SERVICE_API_URL + "/register";
    public static final String SMS_CONFIRM = SERVICE_API_URL + "/code";
    public static final String SMS_REMIND = SERVICE_API_URL + "/request";
    public static final String EZS_DOWNLOAD = SERVICE_API_URL + "/download/";
    public static final String EZS_PREVIEW = SERVICE_API_URL + "/preview/";
    public static final String EZS_DELETE = SERVICE_API_URL + "/delete/";
    public static final String SERVER_GETSETTINGS = SERVICE_API_URL + "/settings";
    public static final String SERVER_SETSETTINGS = SERVICE_API_URL + "/settings/set";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String CODESMS = "code";
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_CONFIRM = 13;
    public static final int RESULT_ERROR = -1;
    public static final String SHAREDPREFERNCE = "WeKastPreference";
    public static final String DEFAULT_PATH_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String WORK_DIRECTORY = "WeKast/";
    public static final String CASH_DIRECTORY = "Cash/";
    public static final String PREVIEW_DIRECTORY = "Preview/";
    public static final String ANIMATIONS_DIRECTORY = "animations/";
    public static final String CASH_ABSOLUTE_PATH = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY;
    public static final String ANIMATIONS_ABSOLUTE_PATH = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY + ANIMATIONS_DIRECTORY;
    public static final String PREVIEW_ABSOLUTE_PATH = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + PREVIEW_DIRECTORY;
    public static final String FORMAT = ".ezs";
    public static final String infoXML = CASH_ABSOLUTE_PATH + "/info.xml";
    public static File DIRECTORY = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
    public static File DIRECTORY_PREVIEW = new File(PREVIEW_ABSOLUTE_PATH);
    public static final String AP_SSID_KEY = "ACCESS_POINT_SSID_ON_APP";
    public static final String AP_PASS_KEY = "ACCESS_POINT_PASS_ON_APP";

    /**
     * StateMachine
     **/
    public static final int PRESENTATION_LIST = 0x00000100;
    public static final int SLIDER = 0x00000200;

    /**
     * Command DongleService
     **/
    public static final int UPLOAD = 1;
    public static final int SLIDE = 2;
    public static final int STOP = 3;

    /**
     * Command DownloadService
     **/
    public static final int STATUS_START = 100;
    public static final int STATUS_FINISH_PREVIEW = 125;
    public static final int STATUS_FINISH_ONE = 150;
    public static final int STATUS_FINISH_ALL = 200;
    public static final int DOWNLOAD = 10;
    public static final int DELETE = 20;
    public static final int SETTINGS = 30;
    public static final int CHECK = 31;
    public static final int UPDATE = 32;
    public static final int ERROR_DOWNLOAD = 666;
    public static final int ERROR_CONFIRM = 405;

    /**
     * Commands timer
     */
    public static final int REQUEST_TIMER_CODE = 101;
    public static final String TIME = "TIME";

    /**
     * Default Dongle Access Point
     **/
    public static final String DONGLE_AP_SSID_DEFAULT = "wekast";
    public static final String DONGLE_AP_PASS_DEFAULT = "12345678";
    public static final String DONGLE_SOCKET_PORT = "8888";
    public static final int TIME_TO_TRYING_SEND_PRESENTATION = 20;  // in seconds

    // SharedPreferences params
    // DONGLE_IP        // set when connecting to dongle access point for sending new ssid and pass
    // DONGLE_PORT      // set when connecting to dongle access point for sending new ssid and pass
    // WIFI_STATE_BEFORE_LAUNCH_APP             // save state of wifi module
    // ACCESS_POINT_STATE_BEFORE_LAUNCH_APP     // save state of access point
    // ACCESS_POINT_SSID_NEW         // new value of ssid
    // ACCESS_POINT_PASS_NEW         // new value of pass
    // ACCESS_POINT_SSID_ON_APP
    // ACCESS_POINT_PASS_ON_APP

    public static void initWorkFolder() {
        ArrayList<String> workFolder = new ArrayList<>();
        workFolder.add(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
        workFolder.add(CASH_ABSOLUTE_PATH);
        workFolder.add(PREVIEW_ABSOLUTE_PATH);
        workFolder.add(CASH_ABSOLUTE_PATH + "slides");
//        workFolder.add(CASH_ABSOLUTE_PATH + "animations");
//        workFolder.add(CASH_ABSOLUTE_PATH + "audio");
//        workFolder.add(CASH_ABSOLUTE_PATH + "video");

        createFolder(workFolder);
    }

    private static void createFolder(ArrayList<String> workFolder) {
        for (String str : workFolder) {
            File file = new File(str);
            if (!file.isDirectory()) {
                file.mkdir();
                Log.d("Create directory", str);
            }
        }
    }

    public static void clearWorkDirectory(String absolutePath) {
        File[] clearWorkDirectory = (new File(absolutePath)).listFiles();
        if (clearWorkDirectory != null) {
            for (File tmp : clearWorkDirectory) {
                clearDirectory(tmp);
            }
        }
    }

    public static void clearDirectory(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File tmp2 : file.listFiles()) {
                clearDirectory(tmp2);
            }
        } else file.delete();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
    }

    public static boolean removeFromSharedPreferences(Context context, String field) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsActivity.edit();
        editor.remove(field);
        editor.apply();
        return true;
    }

    public static boolean getContainsSP(Context context, String field) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        return settingsActivity.contains(field);
    }

    public static String getFieldSP(Context context, String field) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        String result = settingsActivity.getString(field, "");
        return result;
    }

    public static void clearSP(Context context) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settingsActivity.edit();
        prefEditor.clear();
        prefEditor.commit();
    }

    public static void setFieldSP(Context context, String field1, String field2) {
        SharedPreferences settingsActivity = context.getSharedPreferences(SHAREDPREFERNCE, context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settingsActivity.edit();
        prefEditor.putString(field1, field2);
        prefEditor.apply();
    }

    public static void toastShow(Context context, String s) {
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
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
            Log.d("parseJSONArrayMap ", e.toString());
        }
        return mapList;
    }

    public static ArrayList<String[]> getAllFilesList() {
        ArrayList<String[]> fileList = new ArrayList<>();
        File[] filesList = DIRECTORY.listFiles();
        if (filesList != null && filesList.length > 0) {
            for (int i = 0; i < filesList.length; i++) {
                if (filesList[i].getName().endsWith(FORMAT))
                    fileList.add(new String[]{filesList[i].getName(), filesList[i].getAbsolutePath()});
            }
        }
        return fileList;
    }

    public static ArrayList<String[]> getAllPreviewList() {
        ArrayList<String[]> fileList = new ArrayList<>();
        File[] filesList = DIRECTORY_PREVIEW.listFiles();
        if (filesList != null && filesList.length > 0) {
            for (int i = 0; i < filesList.length; i++) {
//                if(filesList[i].getName().endsWith(FORMAT))
                fileList.add(new String[]{filesList[i].getName(),
                        String.valueOf(filesList[i].getAbsoluteFile())});
            }
        }
        return fileList;
    }

    public static boolean unZipPresentation2(String path) {
        clearWorkDirectory(CASH_ABSOLUTE_PATH);
        long start = System.currentTimeMillis();
        boolean res = false;
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(path))) {
            ZipEntry zipEntry;

            while ((zipEntry = zin.getNextEntry()) != null) {

                if (!zipEntry.isDirectory()) {
                    String fileName = zipEntry.getName();
                    if (zipEntry.getName().endsWith("info.xml") ||
                            fileName.contains("slides")) {
                        File targetDirectory = new File(CASH_ABSOLUTE_PATH);
                        String filePath = targetDirectory + File.separator + fileName;
                        extractFile(zin, filePath, (int) zipEntry.getSize());
                    }
                }
                zin.closeEntry();
            }
            res = true;
        } catch (Exception e) {
            Log.d("UnzipError", e.toString());
        }
        Log.d("Time unzip"," = " + (System.currentTimeMillis() - start));
        return res;
    }

    private static void extractFile(ZipInputStream zipIn, String filePath, int size) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[size];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static byte[] unZipPreview(String path) {
        byte[] result = null;
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(path))) {
            ZipEntry zipEntry;
            while ((zipEntry = zin.getNextEntry()) != null) {
                if (zipEntry.getName().endsWith("preview.jpeg")) {
                    result = new byte[(int) zipEntry.getSize()];
                    for (int c = zin.read(), j = 0; c != -1; c = zin.read(), j++) {
                        result[j] = (byte) c;
                    }
                    break;
                }
                zin.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static HashMap<String, String> mapEzsForDownload(HashMap<String, String> mapDownload, ArrayList<String[]> filesLocal) {
        if (mapDownload.size() > 0) {
            for (String[] s : filesLocal) {
                for (Iterator<HashMap.Entry<String, String>> it = mapDownload.entrySet().iterator(); it.hasNext(); ) {
                    HashMap.Entry<String, String> entry = it.next();
                    if (entry.getValue().equals(s[0])) {
                        it.remove();
                    }
                }
            }
        }
        return mapDownload;
    }

    public static HashMap<String, String> mapEzsForDeleted(HashMap<String, String> mapDeleted, ArrayList<String> serverEzsDel) {
        if (mapDeleted.size() > 0) {
            for (String s : serverEzsDel) {
                for (Iterator<HashMap.Entry<String, String>> it = mapDeleted.entrySet().iterator(); it.hasNext(); ) {
                    HashMap.Entry<String, String> entry = it.next();
                    if (!entry.getValue().equals(s)) {
                        it.remove();
                    }
                }
            }
        }
        return mapDeleted;
    }

    public static void deleteEzsLocal(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

}
