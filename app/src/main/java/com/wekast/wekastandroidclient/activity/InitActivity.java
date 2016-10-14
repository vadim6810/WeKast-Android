package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.Timer;
import java.util.TimerTask;

import static com.wekast.wekastandroidclient.model.Utils.LOGIN;


/**
 * Created by RDL on 23.07.2016.
 */
public class InitActivity extends Activity {
    private VideoView startVideo;
    public Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_init);

        startVideo = (VideoView) findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        startVideo.setVideoURI(videoUri);
        startVideo.start();

        Utils.initWorkFolder();

        new Timer().schedule(new InitActivityTimer(), 3500);
    }

    private class InitActivityTimer extends TimerTask {
        @Override
        public void run() {
            Intent intent;
            if (Utils.getContainsSP(context, LOGIN)){
                intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), RegisterActivity.class);
            }
            startActivity(intent);
        }
    }
}


