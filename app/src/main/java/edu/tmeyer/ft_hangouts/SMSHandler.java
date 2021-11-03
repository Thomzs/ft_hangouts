package edu.tmeyer.ft_hangouts;

import android.content.Context;
import android.telephony.SmsManager;

public class SMSHandler {

    /**
     * Need to check for SEND_SMS permission before calling this method
     */
    public static boolean sendSMS(Context context, String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
