package com.wekast.wekastandroidclient.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.wekast.wekastandroidclient.activity.LoginActivity;
import com.wekast.wekastandroidclient.activity.WelcomeActivity;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by RDL on 15.07.2016.
 */
public class AccessServiceAPI {
    private Context context;
    private HashMap<String, String> mapList;

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

    public void taskLogin (String login, String password, Context context) {
        this.context = context;
        new TaskLogin().execute(login, password);
    }

    public void taskRegister (String login, String email, Context context) {
        this.context = context;
        new TaskRegister().execute(login, email);
    }

    public void taskDownload(String login, String password, HashMap<String, String> mapList, Context context) {
        this.context = context;
        this.mapList = mapList;
        new TaskDownload().execute(login, password);
    }

    public class TaskLogin extends AsyncTask<String, Void, Integer> {
            private String JSONresponse;
            private String JSONList;
            ProgressDialog m_ProgressDialog;
            String login;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //Open progress dialog during login
                m_ProgressDialog = ProgressDialog.show(context, "Please wait...", "Processing...", true);
            }

            @Override
            protected Integer doInBackground(String... params) {
                login = params[0];
                //Create date to pass in param
                Map<String, String> param = new HashMap<>();
                param.put("login", params[0]);
                param.put("password", params[1]);

                JSONObject jsonObject;

                try {

                    JSONresponse = getJSONStringWithParam_POST(Utils.SERVICE_API_URL_LIST, param);
                    jsonObject = convertJSONString2Obj(JSONresponse);

                    if (jsonObject.getInt("status") == 0) {
                        JSONList = jsonObject.getString("answer");
                        return Utils.RESULT_SUCCESS;
                    } else {
                        JSONresponse = jsonObject.getString("error");
                        return Utils.RESULT_ERROR;
                    }
                } catch (Exception e) {
                    return Utils.RESULT_ERROR;
                }
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                m_ProgressDialog.dismiss();
                if (result == Utils.RESULT_SUCCESS) {
                    Utils.toastShow(context, "Login success");
                    Intent i = new Intent(context, WelcomeActivity.class);
                    i.putExtra("login",  login);
                    i.putExtra("answer", JSONList);
                    context.startActivity(i);
                } else {
                    Utils.toastShow(context, "Login fail ==> " + JSONresponse);
                }
            }
    }

    public class TaskRegister extends AsyncTask<String, Void, Integer> {
        private String JSONresponse;
        private String password;
        ProgressDialog m_ProgressDialog;
        String login;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgressDialog = ProgressDialog.show(context, "Please wait", "Registration processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            login = params[0];
            Map<String, String> postParam = new HashMap<>();
            postParam.put("login", params[0]);
            postParam.put("email", params[1]);

            JSONObject jsonObject;

            try {
                JSONresponse = getJSONStringWithParam_POST(Utils.SERVICE_API_URL_REGISTER, postParam);
                jsonObject = convertJSONString2Obj(JSONresponse);

                if (jsonObject.getInt("status") == 0) {
                    JSONresponse = jsonObject.getString("answer");
                    JSONObject jsonObject2 = new JSONObject(JSONresponse);
                    password = jsonObject2.getString("password");
                    return Utils.RESULT_SUCCESS;
                } else {
                    JSONresponse = jsonObject.getString("error");
                    return Utils.RESULT_ERROR;
                }

            } catch (Exception e) {
                return Utils.RESULT_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            m_ProgressDialog.dismiss();
            if (result == Utils.RESULT_SUCCESS) {
                Utils.toastShow(context, "Register success! Password: " + password);
                Utils.setFieldSP(context,"login", login);
                Utils.setFieldSP(context,"password",  password.toString());
                Intent i = new Intent(context, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(i);
            } else {
                Utils.toastShow(context, "Registration fail ==> " + JSONresponse);
            }
        }
    }

    public class TaskDownload extends AsyncTask<String, Void, Integer> {
        ProgressDialog m_ProgressDialog;
        String LOG_TAG = "WelcomeActivity = ";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Open progress dialog during downloading
            m_ProgressDialog = ProgressDialog.show(context, "Please wait...", "Downloading...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            HashMap<String, String> param = new HashMap<>();
            param.put("login", params[0]);
            param.put("password", params[1]);
            for (Map.Entry<String, String> item : mapList.entrySet()) {
                try {
                    byte[] content = getDownloadWithParam_POST(Utils.SERVICE_API_URL_DOWNLOAD + item.getKey(), param);
                    Utils.writeFile(content, item.getValue(), LOG_TAG);
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
                Utils.toastShow(context, "Download completed.");
            } else {
                Utils.toastShow(context, "Download fail!!!");
            }
        }
    }
}
