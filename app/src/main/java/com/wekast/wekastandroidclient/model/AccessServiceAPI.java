package com.wekast.wekastandroidclient.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by RDL on 15.07.2016.
 */
public class AccessServiceAPI {
    private static final String TAG = "AccessServiceAPI";

    /**
     * Convert json string to json object
     *
     * @param jsonString
     * @return JSONObject
     */
    public static JSONObject convertJSONString2Obj(String jsonString) {
        JSONObject jObj = null;
        try {
            Log.w("convertJSONString2Obj", "JsonString=" + jsonString);
            jObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e("convertJSONString2Obj ", e.getMessage());
        }
        return jObj;
    }

    /**
     * Get json string from URL with method POST
     *
     * @param serviceUrl
     * @param params     post data
     * @return json string
     */
    public static String getJSONStringWithParam_POST(String serviceUrl, Map<String, String> params) {
        String jsonString = null;
        HttpURLConnection conn = null;
        String line;
        URL url = null;
        try {
            url = new URL(serviceUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "getJSONStringWithParam_POST: " + e.getMessage());
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        String body = bodyBuilder.toString();
        Log.w("getJSONStringWithParam", "param => " + body);
        byte[] bytes = body.getBytes();
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();

            Log.w("getJSONStringWithParam", "Response Status = " + status);
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();


            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }

            jsonString = stringBuilder.toString();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException");
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException");
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return jsonString;
    }

    /**
     * Get binary data from URL with method POST
     *
     * @param serviceUrl
     * @param params
     * @return byte[]
     * @throws IOException
     */
    public static boolean getDownloadWithParam_POST(String serviceUrl, Map<String, String> params,
                                                    FileOutputStream file) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(serviceUrl);
            StringBuilder bodyBuilder = new StringBuilder();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            // constructs the POST body using the parameters
            while (iterator.hasNext()) {
                Map.Entry<String, String> param = iterator.next();
                bodyBuilder.
                        append(param.getKey()).
                        append('=').
                        append(param.getValue());
                if (iterator.hasNext()) {
                    bodyBuilder.append('&');
                }
            }

            String body = bodyBuilder.toString();
            Log.d("body POST", body);
            byte[] bytes = body.getBytes();
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            int status = conn.getResponseCode();
            Log.d(TAG, "Response = " + status);
            if (status != 200) {
                Log.e(TAG, "Bad response server: " + status);
                throw new IOException("Bad response server: " + status);
            }
//            int len = Integer.valueOf(conn.getHeaderField("Content-Length"));

            InputStream stream = conn.getInputStream();
            int bufferLength;
            byte[] buffer = new byte[1024 * 10];
            while ((bufferLength = stream.read(buffer)) > 0) {
                file.write(buffer, 0, bufferLength);
            }
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            throw new IOException("Error download EZS");
        } finally {
            conn.disconnect();
        }
        return true;
    }
}
