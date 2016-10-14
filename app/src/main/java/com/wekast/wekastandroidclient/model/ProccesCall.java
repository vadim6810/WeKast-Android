package com.wekast.wekastandroidclient.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by RDL on 15.10.2016.
 */
public class ProccesCall extends BroadcastReceiver {
    CustomPhoneStateListener customPhoneListener;
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        //Create object of Telephony Manager class.
        TelephonyManager telephony = (TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE);
        //Assign a phone state listener.
        customPhoneListener = new CustomPhoneStateListener (context);
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public CustomPhoneStateListener getPhoneListener(){
        return this.customPhoneListener;
    }
    

}
