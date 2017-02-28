package com.wekast.wekastandroidclient.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import static com.wekast.wekastandroidclient.model.Utils.EMAIL;
import static com.wekast.wekastandroidclient.model.Utils.LOGIN;
import static com.wekast.wekastandroidclient.model.Utils.PASSWORD;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_ERROR;
import static com.wekast.wekastandroidclient.model.Utils.RESULT_SUCCESS;
import static com.wekast.wekastandroidclient.model.Utils.USER_REGISTER;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.setFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;


/**
 * Created by RDL on 15.07.2016.
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText txtLogin;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtLogin = (EditText) findViewById(R.id.txt_login);
        txtEmail = (EditText) findViewById(R.id.txt_email);

        txtLogin.setText(getFieldSP(RegisterActivity.this, LOGIN));
        txtEmail.setText(getFieldSP(RegisterActivity.this, EMAIL));

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

        //TODO this block-code only testers
        if (txtLogin.getText().toString().charAt(0) == '0') {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Attention!");
            builder.setMessage("The first digit '0' in the phone number, disables the confirmation of registration by e-mail and SMS!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Call async task to register
                    new TaskRegister().execute();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            //Call async task to register
            new TaskRegister().execute();
        }

    }

    public void btnLogin_Click(View v) {
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public class TaskRegister extends AsyncTask<Void, Void, Integer> {
        private String JSONresponse;
        private String password;
        private ProgressDialog progressDialog;
        private String login;
        private String email;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login = txtLogin.getText().toString();
            email = txtEmail.getText().toString();
            progressDialog = ProgressDialog.show(RegisterActivity.this,
                    "Please wait",
                    "Registration processing...",
                    true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put(LOGIN, login);
            postParam.put(EMAIL, email);

            JSONObject jsonObject;

            try {
                JSONresponse = getJSONStringWithParam_POST(USER_REGISTER, postParam);
                jsonObject = convertJSONString2Obj(JSONresponse);

                if (jsonObject.getInt("status") == 0) {
                    JSONresponse = jsonObject.getString("answer");
                    JSONObject jsonObject2 = new JSONObject(JSONresponse);
                    password = jsonObject2.getString(PASSWORD);
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
                toastShow(RegisterActivity.this, "Register success! Password: " + password);
                setFieldSP(RegisterActivity.this, LOGIN, login);
                setFieldSP(RegisterActivity.this, PASSWORD, password.toString());
                Intent i = new Intent(RegisterActivity.this, WelcomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            } else {
                toastShow(RegisterActivity.this, "Registration fail: " + JSONresponse);
            }
        }
    }
}
