package com.dezen.riccardo.smshandler;

import android.content.Context;

//CODE-REVIEW DE ZEN
/**
 * Class to implement CommunicationHandler through use of SMS messages.
 * @author Riccardo De Zen based on decisions of whole class. Model proposed by Marco Cognolato and Luca Crema.
 */
public class SMSManager extends CommunicationHandler<SMSMessage>{
    private static SMSManager instance;

    private static Context owner;

    private SmsHandler smsHandler;
    private ReceivedMessageListener<SMSMessage> rListener;
    private SentMessageListener<SMSMessage> sListener;
    private DeliveredMessageListener<SMSMessage> dListener;
    private SmsHandler.OnSmsEventListener fullListener;

    public static SMSManager getInstance(Context context){
        if(instance == null){
            instance = new SMSManager();
            owner = context;
        }
        return instance;
    }

    public static void drop(Context context){
        if(context.equals(owner)){
            instance.removeReceiveListener();
            owner = null;
        }
    }

    private SMSManager(){
        //prevent use of reflection to change constructor to public at runtime
        if (instance != null)
            throw new RuntimeException("This class uses the singleton building pattern. Use getInstance() to get a reference to the single instance of this class");
        smsHandler = new SmsHandler();
        fullListener = new SmsHandler.OnSmsEventListener() {
            @Override
            public void onReceive(SMSMessage message) {
                if(rListener != null) rListener.onMessageReceived(message);
            }

            @Override
            public void onSent(int resultCode, SMSMessage message) {
                if(sListener != null) sListener.onMessageSent(resultCode,message);
            }

            @Override
            public void onDelivered(int resultCode, SMSMessage message) {
                if(dListener != null) dListener.onMessageDelivered(resultCode,message);
            }
        };
    }

    @Override
    public boolean sendMessage(SMSMessage message) {
        if(message.isValid()) {
            smsHandler.sendSMS(owner, message.getPeer().getAddress(), message.getData());
            return true;
        }
        return false;
    }

    @Override
    public void addReceiveListener(ReceivedMessageListener<SMSMessage> listener) {
        smsHandler.registerReceiver(owner);
        smsHandler.setListener(fullListener);
        rListener = listener;
    }

    @Override
    public void removeReceiveListener() {
        smsHandler.unregisterReceiver(owner);
    }

    //TODO implementation of sending and delivery confirm (?)
}
