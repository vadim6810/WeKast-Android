package com.wekast.wekastandroidclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

/**
 * Created by RDL on 23.07.2016.
 */
public class InitActivity extends Activity{
    private VideoView startVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        startVideo = (VideoView) findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        startVideo.setVideoURI(videoUri);
        startVideo.start();

        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);

    }
}
