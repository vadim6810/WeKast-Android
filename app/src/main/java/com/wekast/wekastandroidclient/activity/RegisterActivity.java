package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RDL on 15.07.2016.
 */
public class RegisterActivity extends Activity {
    private EditText txtLogin;
    private EditText txtEmail;
    private ProgressDialog m_ProgressDialog;
    private AccessServiceAPI m_AccessServiceAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtLogin = (EditText) findViewById(R.id.txt_login);
        txtEmail = (EditText) findViewById(R.id.txt_email);
        m_AccessServiceAPI = new AccessServiceAPI();
    }

    public void btnRegister_Click(View v) {
        //validate input
        if ("".equals(txtLogin.getText().toString())) {
            txtLogin.setError("Login is required!");
            return;
        }
        if ("".equals(txtEmail.getText().toString())) {
            txtEmail.setError("Email is required!");
            return;
        }
        //exec task register
        new TaskRegister().execute(txtLogin.getText().toString(), txtEmail.getText().toString());

    }

    public class TaskRegister extends AsyncTask<String, Void, Integer> {
        private String JSONresponse;
        private String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgressDialog = ProgressDialog.show(RegisterActivity.this, "Please wait", "Registration processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("login", params[0]);
            postParam.put("email", params[1]);

            JSONObject jsonObject;

            try {
                JSONresponse = m_AccessServiceAPI.getJSONStringWithParam_POST(Utils.SERVICE_API_URL_REGISTER, postParam);
                jsonObject = m_AccessServiceAPI.convertJSONString2Obj(JSONresponse);

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
                toastShow("Register success! Password: " + password);
                Intent i = new Intent();
                i.putExtra("login", txtLogin.getText().toString());
                i.putExtra("password", password.toString());
                setResult(1, i);
                finish();
            } else {
                toastShow("Registration fail ==> " + JSONresponse);
            }
        }

        private void toastShow(String s) {
            Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
