package com.wekast.wekastandroidclient.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by RDL on 15.07.2016.
 */
public class AccessServiceAPI {


    /**
     * Convert json string to json object
     *
     * @param jsonString
     * @return JSONObject
     */
    public JSONObject convertJSONString2Obj(String jsonString) {
        JSONObject jObj = null;
        try {
            Log.w("convertJSONString2Obj", "JsonString=" + jsonString);
            jObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
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
    public String getJSONStringWithParam_POST(String serviceUrl, Map<String, String> params) throws IOException {
        String jsonString = null;
        HttpURLConnection conn = null;
        String line;

        URL url;
        try {
            url = new URL(serviceUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + serviceUrl);
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
        Log.w("getJSONStringWithParam", "param=>" + body);
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
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
    public byte[] getDownloadWithParam_POST(String serviceUrl, Map<String, String> params) throws IOException {
        HttpURLConnection conn;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        URL url = new URL(serviceUrl);
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
        Log.w("getJSONStringWithParam", "param=>" + body);
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

            InputStream stream = conn.getInputStream();
            int c;
            byte[] buffer = new byte[4096];
            while ((c = stream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, c);
            }
            stream.close();
            conn.disconnect();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }
}
