package com.wekast.wekastandroidclient.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.wekast.wekastandroidclient.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.wekast.wekastandroidclient.model.AccessServiceAPI.convertJSONString2Obj;
import static com.wekast.wekastandroidclient.model.AccessServiceAPI.getJSONStringWithParam_POST;
import static com.wekast.wekastandroidclient.model.Utils.CODESMS;
import static com.wekast.wekastandroidclient.model.Utils.LOGIN;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_ERROR;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_SUCCESS;
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_CONFIRM;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.setFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;

/**
 * Created by RDL on 19.01.2017.
 */

public class PhoneConfirmActivity extends AppCompatActivity {
    private EditText txtLogin;
    private EditText txtCodeSms;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneconfirm);
        context = PhoneConfirmActivity.this;

        txtLogin = (EditText) findViewById(R.id.txt_username_login);
        txtCodeSms = (EditText) findViewById(R.id.txt_code_sms);

        txtLogin.setText(getFieldSP(context, LOGIN));
    }

    public void btnConfirm_Click(View v) {
        //validate input
        if ("".equals(txtLogin.getText().toString())) {
            txtLogin.setError("Login is required!");
            return;
        }
        if ("".equals(txtCodeSms.getText().toString())) {
            txtCodeSms.setError("Code is required!");
            return;
        }

        //Call async task to login
        new TaskConfirm().execute();
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public class TaskConfirm extends AsyncTask<String, Void, Integer> {
        private String JSONresponse;
        public String JSONList;
        String login;
        String codeSMS;
        ProgressDialog progressDialog;

        public TaskConfirm() {
            this.login = txtLogin.getText().toString();
            this.codeSMS = txtCodeSms.getText().toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context,
                    "Please wait",
                    "Confirmation processing...",
                    true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> param = new HashMap<>();
            param.put(LOGIN, login);
            param.put(CODESMS, codeSMS);

            JSONObject jsonObject;

            try {
                JSONresponse = getJSONStringWithParam_POST(SERVICE_API_URL_CONFIRM, param);
                jsonObject = convertJSONString2Obj(JSONresponse);

                if (jsonObject.getInt("status") == 0) {
                    JSONList = jsonObject.getString("answer");
                    return RESULT_SUCCESS;
                } else {
                    JSONresponse = jsonObject.getString("error");
                    return RESULT_ERROR;
                }
            } catch (Exception e) {
                return RESULT_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result == RESULT_SUCCESS) {
                toastShow(context, "Confirm success");
                startActivity(WelcomeActivity.class);
            } else {
                toastShow(context, "Confirm fail: " + JSONresponse);
            }
        }

        private void startActivity(Class<?> clazz) {
            Intent i = new Intent(context, clazz)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }
}
