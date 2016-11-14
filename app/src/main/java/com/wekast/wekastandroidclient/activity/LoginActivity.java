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
import static com.wekast.wekastandroidclient.model.AccessServiceAPI.*;
import static com.wekast.wekastandroidclient.model.Utils.*;


/**
 * Created by RDL on 15.07.2016.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText txtLogin;
    private EditText txtPassword;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = LoginActivity.this;

        txtLogin = (EditText) findViewById(R.id.txt_username_login);
        txtPassword = (EditText) findViewById(R.id.txt_pwd_login);

        txtLogin.setText(getFieldSP(context, LOGIN));
        txtPassword.setText(getFieldSP(context, PASSWORD));

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
        new TaskLogin().execute();
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public class TaskLogin extends AsyncTask<String, Void, Integer> {
        private String JSONresponse;
        public String JSONList;
        String login;
        String password;
        ProgressDialog progressDialog;

        public TaskLogin() {
            this.login = txtLogin.getText().toString();
            this.password = txtPassword.getText().toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context,
                    "Please wait",
                    "Authentication processing...",
                    true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            //Create date to pass in param
            Map<String, String> param = new HashMap<>();
            param.put(LOGIN, login);
            param.put(PASSWORD, password);

            JSONObject jsonObject;

            try {
                JSONresponse = getJSONStringWithParam_POST(SERVICE_API_URL_LIST, param);
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
                toastShow(context, "Login success");
                setFieldSP(context, LOGIN, login);
                setFieldSP(context, PASSWORD, password);
                startWelcome();
            } else {
                toastShow(context, "Login fail: " + JSONresponse);
            }
        }

        private void startWelcome() {
            Intent i = new Intent(context, WelcomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }
}
