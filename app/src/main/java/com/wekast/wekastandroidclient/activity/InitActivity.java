package com.wekast.wekastandroidclient.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.VideoView;


import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.wekast.wekastandroidclient.model.Utils.*;


/**
 * Created by RDL on 23.07.2016.
 */
public class InitActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 403;
    private static final String TAG = "InitActivity";
    private VideoView startVideo;
    public Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        chekPermission();
    }

    private void chekPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (!permissions.isEmpty()) {
                Log.d(TAG, "chekPermission: requestPermissions");
                requestPermissions(permissions.toArray(new String[permissions.size()]),
                        PERMISSION_REQUEST_CODE);
            } else initApp();
        } else initApp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean resPerm = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    resPerm = false;
                    break;
                }
            }
            if (resPerm) {
                initApp();
            } else {
                toastShow(context, "WeKast can't work without \"PERMISSIONS!\"");
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initApp() {
        initWorkFolder();
        clearWorkDirectory(PREVIEW_ABSOLUTE_PATH);
        startVideo = (VideoView) findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        startVideo.setVideoURI(videoUri);
        startVideo.start();

        new Timer().schedule(new InitActivityTimer(), 3500);
    }

    private class InitActivityTimer extends TimerTask {
        @Override
        public void run() {
            Intent intent;
            if (Utils.getContainsSP(context, LOGIN)) {
                intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), RegisterActivity.class);
            }
            startActivity(intent);
        }
    }
}


