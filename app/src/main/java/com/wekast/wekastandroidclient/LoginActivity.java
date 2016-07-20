package com.wekast.wekastandroidclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RDL on 15.07.2016.
 */
public class LoginActivity extends Activity {
    private EditText txtLogin;
    private EditText txtPassword;
    private AccessServiceAPI m_AccessServiceAPI;
    private ProgressDialog m_ProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtLogin = (EditText) findViewById(R.id.txt_username_login);
        txtPassword = (EditText) findViewById(R.id.txt_pwd_login);
        m_AccessServiceAPI = new AccessServiceAPI();

    }

    public void btnLogin_Click(View v) {
        //validate input
        if ("".equals(txtLogin.getText().toString())) {
            txtLogin.setError("Login is required!");
            return;
        }
        if ("".equals(txtPassword.getText().toString())) {
            txtPassword.setError("Password is required!");
            return;
        }

        //Call async task to login
        new TaskLogin().execute(txtLogin.getText().toString(), txtPassword.getText().toString());
    }

    public void btnRegister_Click(View v) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            txtLogin.setText(data.getStringExtra("login"));
            txtPassword.setText(data.getStringExtra("password"));
        }
    }

    public class TaskLogin extends AsyncTask<String, Void, Integer>{
        private String JSONresponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Open progress dialog during login
            m_ProgressDialog = ProgressDialog.show(LoginActivity.this, "Please wait...", "Processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            //Create date to pass in param
            Map<String, String > param = new HashMap<>();
            param.put("login", params[0]);
            param.put("password", params[1]);

            JSONObject jsonObject;

            try {

                JSONresponse = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL_LIST, param);
                jsonObject = m_AccessServiceAPI.convertJSONString2Obj(JSONresponse);

                if(jsonObject.getInt("status") == 0)
                    return Common.RESULT_SUCCESS;
                else {
                    JSONresponse = jsonObject.getString("error");
                    return Common.RESULT_ERROR;
                }

//                Object json = new JSONTokener(JSONresponse).nextValue();
//                if (json instanceof JSONObject) {
////                    JSONObject jsonObject = new JSONObject(JSONresponse);
//                    jObjResult = m_AccessServiceAPI.convertJSONString2Obj(JSONresponse);
//                    JSONresponse = jObjResult.getString("err");
//                    return Common.RESULT_ERROR;
//                }
//                else {
//                  //  if (json instanceof JSONArray)
//                    return Common.RESULT_SUCCESS;
//                }

            } catch (Exception e) {
                return Common.RESULT_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            m_ProgressDialog.dismiss();
            if (result == Common.RESULT_SUCCESS) {
                toastShow("Login success");
                Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                i.putExtra("login", txtLogin.getText().toString());
                startActivity(i);
            } else {
                toastShow("Login fail ==> " + JSONresponse);
            }
        }
        private void toastShow(String s) {
            Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
