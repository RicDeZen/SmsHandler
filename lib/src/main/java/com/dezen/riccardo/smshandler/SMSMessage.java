package com.dezen.riccardo.smshandler;

import android.telephony.SmsMessage;

import androidx.annotation.NonNull;


/**
 * Class implementing Message to represent an SMS-type message.
 * @author Riccardo De Zen based on decisions of whole class.
 */
public class SMSMessage extends Message<String, SMSPeer>{
    private String data;
    private SMSPeer peer;

    /**
     * @param peer the Peer associated with this Message
     * @param data the data to be contained in the message
     */
    public SMSMessage(SMSPeer peer, String data){
        this.peer = peer;
        this.data = data;
    }

    /**
     * Constructor from a valid SmsMessage
     */
    public SMSMessage(SmsMessage message){
        this.peer = new SMSPeer(message.getOriginatingAddress());
        this.data = message.getMessageBody();
    }

    /**
     * @return the data of this message
     */
    @Override
    public String getData() {
        return data;
    }

    /**
     * @return the peer of this message
     */
    @Override
    public SMSPeer getPeer() {
        return peer;
    }

    /**
     * @return true if this message is not empty and has a valid peer
     */
    public boolean isValid(){
        return peer.isValid() && !data.isEmpty();
    }

    /**
     * @return the String type representation of this object
     */
    @NonNull
    public String toString(){
        return "Peer: "+peer.toString()+"\nData: "+data;
    }
}
