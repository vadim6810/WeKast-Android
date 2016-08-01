package com.wekast.wekastandroidclient.model;

import android.os.Environment;
import android.util.Log;

import java.io.File;

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
    public static final File DIRECTORY = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);


    public static void initWorkFolder() {
        File file = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
        if (!file.isDirectory()) {
            file.mkdir();
            Log.d("Create directory", DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
        }
    }
}
