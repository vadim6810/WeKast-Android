package com.wekast.wekastandroidclient.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import static com.wekast.wekastandroidclient.model.Utils.SERVICE_API_URL_REGISTER;
import static com.wekast.wekastandroidclient.model.Utils.getFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.setFieldSP;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;


/**
 * Created by RDL on 15.07.2016.
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText txtLogin;
    private EditText txtEmail;
    Context context = this;
    private static long back_pressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtLogin = (EditText) findViewById(R.id.txt_login);
        txtEmail = (EditText) findViewById(R.id.txt_email);

        txtLogin.setText(getFieldSP(context, LOGIN));
        txtEmail.setText(getFieldSP(context, EMAIL));

    }

//    @Override
//    public void onBackPressed() {
//        if (back_pressed + 2000 > System.currentTimeMillis())
//            finishAffinity();
//        else
//            toastShow(context, "Press once again to exit!");
//        back_pressed = System.currentTimeMillis();
//    }

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
            builder.setMessage("Первая цифра '0' в номере телефона отключает подтверждение регистрации по почте и смс!");
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
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    public void btnBack_Click(View v) {
        finish();
    }

    public class TaskRegister extends AsyncTask<String, Void, Integer> {
        private String JSONresponse;
        private String password;
        ProgressDialog progressDialog;
        String login;
        String email;

        public TaskRegister() {
            this.login = txtLogin.getText().toString();
            this.email = txtEmail.getText().toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context,
                    "Please wait",
                    "Registration processing...",
                    true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put(LOGIN, login);
            postParam.put(EMAIL, email);

            JSONObject jsonObject;

            try {
                JSONresponse = getJSONStringWithParam_POST(SERVICE_API_URL_REGISTER, postParam);
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
                toastShow(context, "Register success! Password: " + password);
                setFieldSP(context, LOGIN, login);
                setFieldSP(context, PASSWORD, password.toString());
                startWelcome();
            } else {
                toastShow(context, "Registration fail: " + JSONresponse);
            }
        }

        private void startWelcome() {
            Intent i = new Intent(context, WelcomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }
}
