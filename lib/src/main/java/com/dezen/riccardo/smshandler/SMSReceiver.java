package com.dezen.riccardo.smshandler;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.room.Room;

import com.dezen.riccardo.smshandler.database.SmsDatabase;
import com.dezen.riccardo.smshandler.database.SmsEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Class meant to intercept SMSMessages coming from the Android system.
 * The filtering work on messages is voluntarily repeated between this Receiver and
 * NotificationCatcherService in order to avoid attaching the Receiver to the Service and keep it
 * lighter.
 * The class checks whether pertinent messages have been received. Then proceeds to check whether a
 * suitable listener is available for immediate response. If not then proceeds to either fire a
 * broadcast meant to wake some other process or writes the messages to a database for later use.
 * @author Riccardo De Zen
 */
public class SMSReceiver extends BroadcastReceiver {
    private boolean shouldWake = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)){
            List<SmsMessage> messages = filter(Telephony.Sms.Intents.getMessagesFromIntent(intent));
            if(messages.size() < 1) return;
            if(SMSHandler.shouldHandleIncomingSms()){
                /*
                 * SMSHandler.shouldHandleIncomingSms() returns true if a suitable listener for
                 * immediate response is available. A broadcast event is therefore fired to
                 * notify said listener through the receiver it is attached to.
                 */
                Intent local_intent = new Intent();
                local_intent.replaceExtras(intent);
                local_intent.setAction(SMSHandler.RECEIVED_BROADCAST);
                local_intent.setPackage(context.getApplicationContext().getPackageName());
                context.sendBroadcast(local_intent);
            }
            else if(shouldWake){
                Intent wake_intent = new Intent();
                wake_intent.replaceExtras(intent);
                wake_intent.setAction(SMSHandler.WAKE_BROADCAST);
                sendImplicitBroadcast(context, wake_intent);
            }
            else{
                //write new sms to local database asynchronously
                SmsDatabase db = Room.databaseBuilder(context, SmsDatabase.class, SMSHandler.UNREAD_SMS_DATABASE_NAME)
                        .enableMultiInstanceInvalidation()
                        .build();
                new WriteToDbTask(messages,db).execute();
            }
        }
    }

    /**
     * Class defining a task that writes the smsMessages to the local database.
     */
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
     * Method to filter messages containing SMSHandler.APP_KEY.
     * Messages are meant to be the ones coming directly from the received Intent.
     * @param messages array of SmsMessage.
     * @return list of messages containing SMSHandler.APP_KEY
     */
    private List<SmsMessage> filter(SmsMessage[] messages){
        List<SmsMessage> list = new ArrayList<>();
        if(messages != null)
            for(SmsMessage sms : messages){
                if(sms.getMessageBody().contains(SMSHandler.APP_KEY)) list.add(sms);
                if(sms.getMessageBody().contains(SMSHandler.WAKE_KEY)) shouldWake = true;
            }
        return list;
    }

    /**
     * Method to turn an implicit Broadcast into explicit ones. This method is needed in order to
     * send broadcasts to manifest declared receivers on API 26 and above, since the ability to send
     * implicit broadcasts to manifest receivers in the same app has been removed.
     * //TODO test this method on APIs below 26.
     */
    public void sendImplicitBroadcast(Context context, Intent intent){
        int flags = 0;
        PackageManager packageManager = context.getPackageManager();
        //get all broadcastReceivers for this context.
        List<ResolveInfo> matchingReceivers = packageManager.queryBroadcastReceivers(intent,flags);
        for (ResolveInfo resolveInfo : matchingReceivers) {
            //create and broadcast intent for every matching receiver
            //this is a specific broadcast because it targets the receivers specifically
            Intent explicit=new Intent(intent);
            ComponentName componentName = new ComponentName(
                    resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name
            );
            explicit.setComponent(componentName);
            context.sendBroadcast(explicit);
        }
    }
}