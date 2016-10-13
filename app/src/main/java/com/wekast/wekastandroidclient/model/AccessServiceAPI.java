package com.wekast.wekastandroidclient.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

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
            Log.w("convertJSONString2Obj ", e.getMessage());
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
    public static String getJSONStringWithParam_POST(String serviceUrl, Map<String, String> params) throws IOException {
        String jsonString = null;
        HttpURLConnection conn = null;
        String line;
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
            byte[] buffer = new byte[100*1024];
//            byte[] buffer = new byte[stream.available()];
            while ((c = stream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, c);
            }
            stream.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void taskLogin(String login, String password, Context context, int whoCalled) {
        new TaskLogin(whoCalled, context).execute(login, password);
    }

    public void taskRegister (String login, String email, Context context) {
        new TaskRegister(context).execute(login, email);
    }

    public static class TaskLogin extends AsyncTask<String, Void, Integer> {
            private String JSONresponse;
            public String JSONList;
            String login;
            String password;
            int whoCalled;
            Context context;

        public TaskLogin(int whoCalled, Context context) {
            this.whoCalled = whoCalled;
            this.context = context;
        }

         @Override
            protected Integer doInBackground(String... params) {
                login = params[0];
                password = params[1];
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
                if (result == Utils.RESULT_SUCCESS) {
                    Utils.toastShow(context, "Login success");
                    if (whoCalled == 1) {
                        startWelcome();
                    }
                    if (whoCalled == 2){
                        Utils.setFieldSP(context,"login", login);
                        Utils.setFieldSP(context,"password", password);
                        startWelcome();
                    }
                } else {
                    Utils.toastShow(context, "Login fail ==> " + JSONresponse);
                }
            }

        private void startWelcome() {
            Intent i = new Intent(context, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }

    public class TaskRegister extends AsyncTask<String, Void, Integer> {
         private Context context;
        private String JSONresponse;
        private String password;
        ProgressDialog m_ProgressDialog;
        String login;

        public TaskRegister(Context context) {
            this.context = context;
        }


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
                //Call async task to login
                taskLogin(login, password.toString(), context, 1);
            } else {
                Utils.toastShow(context, "Registration fail ==> " + JSONresponse);
            }
        }
    }

}
