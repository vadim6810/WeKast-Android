package com.wekast.wekastandroidclient;

import android.os.Environment;

import java.io.File;

/**
 * Created by RDL on 15.07.2016.
 */
public class Common {

    static final String SERVICE_API_URL = "http://78.153.150.254";
    static final String SERVICE_API_URL_LIST = SERVICE_API_URL+"/list";
    static final String SERVICE_API_URL_REGISTER = SERVICE_API_URL+"/register";
    static final String SERVICE_API_URL_DOWNLOAD = SERVICE_API_URL+"/download/";
    static final int RESULT_SUCCESS = 0;
    static final int RESULT_ERROR = -1;
    static final String SHAREDPREFERNCE = "WeKastPreference";
    static final String DEFAULT_PATH_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    static final String WORK_DIRECTORY = "WeKast/";
    static final File DIRECTORY = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY);
}
