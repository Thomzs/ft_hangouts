package edu.tmeyer.ft_hangouts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;

public class SMSHandler extends BroadcastReceiver {

    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;

    /**
     * Need to check for SEND_SMS permission before calling this method
     */
    public static boolean sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects) {

                        currentSMS = getIncomingMessage(aObject, bundle);

                        String senderNo = currentSMS.getDisplayOriginatingAddress();

                        message = currentSMS.getDisplayMessageBody();
                        String name = getSender(context, senderNo);
                        String messageFrom = context.getResources().getString(R.string.incoming_message) + name;
                        MaterialAlertDialogBuilder messageDialog = new MaterialAlertDialogBuilder(((BackgroundObserver) context.getApplicationContext()).getCurrentActivity());
                        messageDialog
                                .setTitle(messageFrom)
                                .setCancelable(true)
                                .setMessage(message)
                                .setPositiveButton(R.string.reply, new SMSDialog(((BackgroundObserver) context.getApplicationContext()).getCurrentActivity(), senderNo))
                                .show();
                    }
                    this.abortBroadcast();
                }
            }
        }
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        String format = bundle.getString("format");
        currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);

        return currentSMS;
    }

    private String getSender(Context context, String sender) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        List<String> numbers = databaseHelper.getAllNumbers();

        for (String number : numbers) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (PhoneNumberUtils.areSamePhoneNumber(sender, number, getUserCountry(context))) {
                    Contact contact = databaseHelper.getContact(number);
                    return contact.toString();
                }
            }
        }
        databaseHelper.addContact(new Contact(sender));
        return sender;
    }

    private String getUserCountry(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(context.getResources().getConfiguration().locale);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
