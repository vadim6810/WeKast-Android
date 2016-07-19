package com.wekast.wekastandroidclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        txtLogin = (EditText)findViewById(R.id.txt_login);
        txtEmail = (EditText)findViewById(R.id.txt_email);
        m_AccessServiceAPI = new AccessServiceAPI();
    }

    public void btnRegister_Click(View v){
        //validate input
        if ("".equals(txtLogin.getText().toString())){
            txtLogin.setError("Login is required!");
            return;
        }
        if ("".equals(txtEmail.getText().toString())){
            txtEmail.setError("Email is required!");
            return;
        }
        //exec task register
        new TaskRegister().execute(txtLogin.getText().toString(), txtEmail.getText().toString());

    }

    public class TaskRegister extends AsyncTask<String , Void, Integer>{
        private String JSONresponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgressDialog = ProgressDialog.show(RegisterActivity.this, "Please wait", "Registration processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String ,String> postParam = new HashMap<>();
            postParam.put("login", params[0]);
            postParam.put("email", params[1]);
            try{
                JSONresponse = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL_REGISTER, postParam);
//                JSONObject jsonObject = new JSONObject(JSONresponse);
                JSONObject jsonObject = m_AccessServiceAPI.convertJSONString2Obj(JSONresponse);
                return (jsonObject.getString("login")).length() > 0 ? Common.RESULT_SUCCESS : Common.RESULT_ERROR;


            } catch (Exception e) {
                e.printStackTrace();
                return Common.RESULT_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            m_ProgressDialog.dismiss();
            if (integer == Common.RESULT_SUCCESS) {
                Toast.makeText(RegisterActivity.this, "Register success", Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                i.putExtra("login", txtLogin.getText().toString());
                i.putExtra("email", txtEmail.getText().toString());
                setResult(1, i);
                finish();
            } else if (integer == Common.RESULT_USERS_EXISTS) {
                Toast.makeText(RegisterActivity.this, "Login is exists!", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(RegisterActivity.this, "Registration fail!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
