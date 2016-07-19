package com.wekast.wekastandroidclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by RDL on 15.07.2016.
 */
public class WelcomeActivity extends Activity{
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvWelcome = (TextView)findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome: " + getIntent().getStringExtra("login"));

    }

    public void btnBack_Click(View v){
        finish();
    }
}
