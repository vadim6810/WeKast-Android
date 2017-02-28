package com.wekast.wekastandroidclient.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.wekast.wekastandroidclient.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.wekast.wekastandroidclient.model.AccessServiceAPI.convertJSONString2Obj;
import static com.wekast.wekastandroidclient.model.AccessServiceAPI.getJSONStringWithParam_POST;
import static com.wekast.wekastandroidclient.model.Utils.EZS_LIST;
import static com.wekast.wekastandroidclient.model.Utils.LOGIN;
import static com.wekast.wekastandroidclient.model.Utils.PASSWORD;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_CONFIRM;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_ERROR;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_SUCCESS;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.setFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;


/**
 * Created by RDL on 15.07.2016.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText txtLogin;
    private EditText txtPassword;
    private CheckBox checkPass;
    private Button btnCancel, btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnCancel = (Button) findViewById(R.id.btn_CANCEL);
        btnLogin = (Button) findViewById(R.id.btn_LOGIN);
        btnRegister = (Button) findViewById(R.id.btn_REGISTER);
        txtLogin = (EditText) findViewById(R.id.txt_username_login);
        txtPassword = (EditText) findViewById(R.id.txt_pwd_login);

        txtLogin.setText(getFieldSP(LoginActivity.this, LOGIN));
        txtPassword.setText(getFieldSP(LoginActivity.this, PASSWORD));
        btnCancel.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_LOGIN:
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
                break;
            case R.id.btn_REGISTER:
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.btn_CANCEL:
                finish();
                break;
        }
    }

    public class TaskLogin extends AsyncTask<Void, Void, Integer> {
        private String JSONresponse;
        private String JSONList;
        private String login;
        private String password;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login = String.valueOf(txtLogin.getText());
            password = String.valueOf(txtPassword.getText());
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Please wait",
                    "Authentication processing...",
                    true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
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
                toastShow(LoginActivity.this, "Login success");
                setFieldSP(LoginActivity.this, LOGIN, login);
                setFieldSP(LoginActivity.this, PASSWORD, password);
                runActivity(result == RESULT_SUCCESS ?
                        WelcomeActivity.class : PhoneConfirmActivity.class);
            } else {
                toastShow(LoginActivity.this, "Login fail: " + JSONresponse);
            }
        }

        private void runActivity(Class<?> clazz) {
            Intent i = new Intent(LoginActivity.this, clazz)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        }
    }
}
