package com.wekast.wekastandroidclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by RDL on 23.07.2016.
 */
public class InitActivity extends Activity{
    private VideoView startVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_init);

        startVideo = (VideoView) findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        startVideo.setVideoURI(videoUri);
        startVideo.start();

        initWorkFolder();

        new Timer().schedule(new LoginActivityTimer(),4000);

    }

    private void initWorkFolder() {
        File file = new File(Common.DEFAULT_PATH_DIRECTORY + Common.WORK_DIRECTORY);
        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

    private class LoginActivityTimer extends TimerTask {
        @Override
        public void run() {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }

    }
}
