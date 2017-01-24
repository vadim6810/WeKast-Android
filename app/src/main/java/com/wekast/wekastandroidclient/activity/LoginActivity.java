package com.wekast.wekastandroidclient.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.wekast.wekastandroidclient.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.wekast.wekastandroidclient.model.AccessServiceAPI.convertJSONString2Obj;
import static com.wekast.wekastandroidclient.model.AccessServiceAPI.getJSONStringWithParam_POST;
import static com.wekast.wekastandroidclient.model.Utils.LOGIN;
import static com.wekast.wekastandroidclient.model.Utils.PASSWORD;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_CONFIRM;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_ERROR;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_SUCCESS;
import static com.wekast.wekastandroidclient.model.Utils.EZS_LIST;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.setFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;


/**
 * Created by RDL on 15.07.2016.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText txtLogin;
    private EditText txtPassword;
    private CheckBox checkPass;
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

        checkPass = (CheckBox) findViewById(R.id.checkPass);
        checkPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    txtPassword.setTransformationMethod(null);
                else txtPassword.setTransformationMethod(new PasswordTransformationMethod());
                txtPassword.setSelection(txtPassword.length());
            }
        });

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

    public void btnReg_Click(View v) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
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
                JSONresponse = getJSONStringWithParam_POST(EZS_LIST, param);
                jsonObject = convertJSONString2Obj(JSONresponse);

                if (jsonObject.getInt("status") == 0) {
                    JSONList = jsonObject.getString("answer");
                    return RESULT_SUCCESS;
                }
                if (jsonObject.getInt("status") == 13) {
                    return RESULT_CONFIRM;
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
            if (result == RESULT_SUCCESS || result == RESULT_CONFIRM) {
                toastShow(context, "Login success");
                setFieldSP(context, LOGIN, login);
                setFieldSP(context, PASSWORD, password);
                startActivity(result == RESULT_SUCCESS ?
                        WelcomeActivity.class : PhoneConfirmActivity.class);
            } else {
                toastShow(context, "Login fail: " + JSONresponse);
            }
        }

        private void startActivity(Class<?> clazz) {
            Intent i = new Intent(context, clazz)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }
}
