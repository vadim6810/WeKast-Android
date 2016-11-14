package com.wekast.wekastandroidclient.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

/**
 * Created by RDL on 15.10.2016.
 */
public class CustomPhoneStateListener extends PhoneStateListener  {
    Context context;
    public volatile static boolean blockingCall;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    PendingIntent sentPI;

    PendingIntent deliveredPI;

    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
        blockingCall = true;
        sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);
    }

    @Override
    public void onCallStateChanged(int state, String callingNumber)
    {
        super.onCallStateChanged(state, callingNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //handle out going call
                endCallIfBlocked(callingNumber);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //handle in coming call
                endCallIfBlocked(callingNumber);
                break;
            default:
                break;
        }
    }

    private void endCallIfBlocked(String callingNumber) {
        if(blockingCall) {
            try {

                // Java reflection to gain access to TelephonyManager's
                // ITelephony getter
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
                c = Class.forName(telephonyService.getClass().getName()); // Get its class
                m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
                m.setAccessible(true); // Make it accessible
                m.invoke(telephonyService); // invoke endCall()
               /* SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(callingNumber, null,
                        "I am presenting a cool presentation " +
                                "using the best presentation tool ever! WeKast RULES!!!! \n" +
                                "Sent By WeKast! \n" +
                                "For More Information: http://wekast.com ",
                        sentPI, deliveredPI);*/

            /*com.android.internal.telephony.ITelephony telephonyService = (ITelephony) m.invoke(tm);
            telephonyService = (ITelephony) m.invoke(tm);
            //
            telephonyService.silenceRinger();
            telephonyService.endCall();*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
