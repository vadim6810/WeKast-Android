package com.wekast.wekastandroidclient.activity;

import android.app.ProgressDialog;
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
import static com.wekast.wekastandroidclient.model.Utils.SMS_CONFIRM;
import static com.wekast.wekastandroidclient.model.Utils.SMS_REMIND;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;

/**
 * Created by RDL on 19.01.2017.
 */

public class PhoneConfirmActivity extends AppCompatActivity {
    private EditText txtLogin;
    private EditText txtCodeSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneconfirm);

        txtLogin = (EditText) findViewById(R.id.txt_username_login);
        txtCodeSms = (EditText) findViewById(R.id.txt_code_sms);

        txtLogin.setText(getFieldSP(PhoneConfirmActivity.this, LOGIN));
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

        //Call async task to SMS_CONFIRM
        new TaskConfirm(SMS_CONFIRM).execute();
    }

    public void btnRemind_Click(View v) {
        //validate input
        if ("".equals(txtLogin.getText().toString())) {
            txtLogin.setError("Login is required!");
            return;
        }

        //Call async task to SMS_REMIND
        new TaskConfirm(SMS_REMIND).execute();
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public class TaskConfirm extends AsyncTask<String, Void, Integer> {
        private String JSONresponse;
        private String JSONList;
        private String login;
        private String codeSMS;
        private ProgressDialog progressDialog;
        private String smsRequest;

        public TaskConfirm(String smsRequest) {
            this.smsRequest = smsRequest;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login = String.valueOf(txtLogin.getText());
            codeSMS = String.valueOf(txtCodeSms.getText());
            progressDialog = ProgressDialog.show(PhoneConfirmActivity.this,
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
                JSONresponse = getJSONStringWithParam_POST(smsRequest, param);
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
                if (smsRequest == SMS_CONFIRM) {
                    toastShow(PhoneConfirmActivity.this, "Confirm success");
                    Intent i = new Intent(PhoneConfirmActivity.this, WelcomeActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();
                } else toastShow(PhoneConfirmActivity.this, "SMS was sent");
            } else {
                toastShow(PhoneConfirmActivity.this, "Confirm fail: " + JSONresponse);
            }
        }
    }
}
