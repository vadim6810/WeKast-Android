package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;


/**
 * Created by RDL on 15.07.2016.
 */
public class LoginActivity extends Activity {
    private EditText txtLogin;
    private EditText txtPassword;
    private AccessServiceAPI m_AccessServiceAPI;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtLogin = (EditText) findViewById(R.id.txt_username_login);
        txtPassword = (EditText) findViewById(R.id.txt_pwd_login);
        m_AccessServiceAPI = new AccessServiceAPI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtLogin.setText(Utils.getFieldSP(context, "login"));
        txtPassword.setText(Utils.getFieldSP(context, "password"));
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
        Utils.setFieldSP(context, "login", txtLogin.getText().toString());
        Utils.setFieldSP(context, "password", txtPassword.getText().toString());

        //Call async task to login
        m_AccessServiceAPI.taskLogin(txtLogin.getText().toString(), txtPassword.getText().toString(), context);
    }

    public void btnRegister_Click(View v) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }
}
