package com.dezen.riccardo.smshandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.dezen.riccardo.smshandler.database.SmsDatabase;
import com.dezen.riccardo.smshandler.database.SmsEntity;

import java.util.ArrayList;
import java.util.List;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)){
            List<SmsMessage> messages = filter(Telephony.Sms.Intents.getMessagesFromIntent(intent));
            if(messages.size() > 0){
                if(SmsHandler.shouldHandleIncomingSms()){
                    //broadcast local intent to wake the local receiver if the app is running
                    Log.d("SmsReceiver", "Forwarding intent...");
                    Intent local_intent = new Intent();
                    local_intent.replaceExtras(intent);
                    local_intent.setAction(context.getString(R.string.sms_handler_received_broadcast));
                    local_intent.setPackage(context.getApplicationContext().getPackageName());
                    context.sendBroadcast(local_intent);
                }
                else{
                    //write new sms to local database asynchronously
                    Log.d("SmsReceiver", "Writing to database...");
                    SmsDatabase db = Room.databaseBuilder(context, SmsDatabase.class, context.getString(R.string.sms_local_database))
                            .enableMultiInstanceInvalidation()
                            .build();
                    new WriteToDbTask(messages,db).execute();
                }
            }
        }
    }

    private static class WriteToDbTask extends AsyncTask<String,Integer,Void>{
        private List<SmsMessage> smsMessages;
        private SmsDatabase db;

        WriteToDbTask(List<SmsMessage> smsMessages, SmsDatabase db) {
            this.smsMessages = smsMessages;
            this.db = db;
        }

        @Override
        protected Void doInBackground(String... strings) {
            for(SmsMessage sms : smsMessages){
                SmsEntity s = new SmsEntity(db.access().getCount(),
                        sms.getOriginatingAddress(),
                        sms.getDisplayMessageBody());
                db.access().insert(s);
            }
            return null;
        }
    }

    /**
     * Method filtering messages containing SmsHandler.APP_KEY
     * @param messages array of messages
     * @return list of messages containing SmsHandler.APP_KEY
     */
    private List<SmsMessage> filter(SmsMessage[] messages){
        List<SmsMessage> list = new ArrayList<>();
        if(messages != null){
            for(SmsMessage sms : messages){
                if(sms.getMessageBody().contains(SmsHandler.APP_KEY)) list.add(sms);
            }
        }
        return list;
    }
}
