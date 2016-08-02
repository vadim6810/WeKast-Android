package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by RDL on 23.07.2016.
 */
public class InitActivity extends Activity {
    private VideoView startVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_init);

        startVideo = (VideoView) findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        startVideo.setVideoURI(videoUri);
        startVideo.start();

        Utils.initWorkFolder();

        new Timer().schedule(new LoginActivityTimer(), 4000);

    }


    private class LoginActivityTimer extends TimerTask {
        @Override
        public void run() {
            Intent i;
            if (getLogin()){
                i = new Intent(getApplicationContext(), LoginActivity.class);
            } else {
                i = new Intent(getApplicationContext(), ListActivity.class);
            }
            startActivity(i);
        }

        private boolean getLogin() {
            SharedPreferences settingsActivity = getSharedPreferences(Utils.SHAREDPREFERNCE, MODE_PRIVATE);
            String login = settingsActivity.getString("login", "");
            return (login == "") ? true : false ;
        }
    }
    }


