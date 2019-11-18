package com.dezen.riccardo.smshandler;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.dezen.riccardo.smshandler.database.SmsDatabase;
import com.dezen.riccardo.smshandler.database.SmsEntity;

import java.util.ArrayList;
import java.util.List;

//CODE-REVIEW DE ZEN
/**
 * Utility class with experimental methods to help testing of some features.
 * @author Riccardo De Zen
 */
public class SmsUtils {
    //projection to use for SMS Inbox query
    private static String[] inboxProjection = {
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.SUBJECT,
            Telephony.Sms.BODY
    };

    /**
     * Method to get SMS Inbox representation. Used for testing and debugging.
     * @param context the calling context used to get the ContentResolver cursor.
     * @return a list containing the SMSMessages
     */
    public static List<SMSMessage> getInbox(@NonNull Context context){
        List<SMSMessage> list = new ArrayList<>();
        String[] selectionArgs = null;
        String selectionClause = null;
        Cursor mCursor = context.getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                inboxProjection, selectionClause, selectionArgs,
                Telephony.Sms._ID);
        if(mCursor != null && mCursor.getCount() >= 1){
            mCursor.moveToFirst();
            while(!mCursor.isLast()){
                SMSMessage sms = new SMSMessage(new SMSPeer(mCursor.getString(1)),mCursor.getString(3));
                Log.d("SMSUtils", sms.toString());
                mCursor.moveToNext();
            }
            mCursor.close();
        }
        return list;
    }

    /**
     * Method to log the unread messages in the database.
     * @param context The calling context, used to instantiate the database.
     */
    public static void logUnreadMessages(@NonNull Context context){
        SmsDatabase db = Room.databaseBuilder(context, SmsDatabase.class, SmsHandler.SMS_HANDLER_LOCAL_DATABASE)
                .enableMultiInstanceInvalidation()
                .build();
        new LogTask(db).execute();
    }

    //AsyncTask to access database since it cannot be accessed from main Thread
    private static class LogTask extends AsyncTask<String, Integer, Void>{
        private SmsDatabase db;
        public LogTask(SmsDatabase db){
            this.db = db;
        }
        @Override
        protected Void doInBackground(String... strings) {
            SmsEntity[] messages = db.access().loadAllSms();
            for(SmsEntity sms : messages){
                db.access().deleteSms(sms);
                Log.e("Unread Message", sms.address+" "+sms.body);
            }
            return null;
        }
    }

    /**
     * Method to get the sim's country code to be used when formatting addresses
     * @param context the calling context to get the TelephonyManager service.
     * @return a String containing the country code or null if not available.
     */
    public static String getCountryCode(@NonNull Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm != null){
            return tm.getSimCountryIso();
        }
        return null;
    }

    /**
     * PhoneNumberUtils.formatNumber(...) requires capital letters country code
     * @param context the valid calling context to get the TelephonyManager service.
     * @return a String containing the upper case country code or null if not available.
     */
    public static String getCapitalCountryCode(@NonNull Context context){
        String countryCode = getCountryCode(context);
        if(countryCode != null) return countryCode.toUpperCase();
        return null;
    }

    /**
     * Adds the country phone prefix corresponding to the given country Code if not already present
     * @param number The valid phone number to be formatted
     * @param countryCode the countryCode in capital letters
     * @return The number with the added country code or the original number if the country code
     * cannot be applied (invalid number or country code)
     */
    public static String formatSMSNumber(String number, String countryCode){
        String formattedNumber = PhoneNumberUtils.formatNumberToE164(number,countryCode);
        return (formattedNumber != null) ? formattedNumber : number;
    }
}
