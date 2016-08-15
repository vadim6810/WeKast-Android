package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.R;

/**
 * Created by RDL on 15.07.2016.
 */
public class RegisterActivity extends Activity {
    private EditText txtLogin;
    private EditText txtEmail;
    private AccessServiceAPI m_AccessServiceAPI;
    Context context = this;


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
            txtLogin.setError("Phone number is required!");
            return;
        }
        if ("".equals(txtEmail.getText().toString())) {
            txtEmail.setError("Email is required!");
            return;
        }
        //exec task register
        m_AccessServiceAPI.taskRegister(txtLogin.getText().toString(), txtEmail.getText().toString(), context);
    }

    public void btnLogin_Click(View v) {
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }
}
