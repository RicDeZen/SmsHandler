package com.dezen.riccardo.smshandler;

import android.telephony.PhoneNumberUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen. Based on decisions of whole class.
 */
public class SMSPeer implements Peer<String> {
    private String address;

    /**
     * @param address the address for the peer
     */
    public SMSPeer(String address){
        this.address = address;
    }

    /**
     * @return the address of the peer
     */
    @Override
    public String getAddress() {
        return address;
    }

    /**
     * @return true if this peer is valid
     */
    public boolean isValid(){ return !isEmpty() && !isBlank() && isAddressValid(); }

    /**
     * @return true if address is empty string
     */
    private boolean isEmpty(){
        return address.isEmpty();
    }

    /**
     * @return true if address is a String of blank spaces
     */
    private boolean isBlank(){ return address.replace(" ","").isEmpty(); }

    /**
     * @return true if address fulfills international phone address standards
     */
    private boolean isAddressValid(){
        return (PhoneNumberUtils.isGlobalPhoneNumber(address));
    }

    /**
     * @return String type representation of the Object
     */
    @NonNull
    public String toString(){
        return address;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof SMSPeer) return ((SMSPeer) obj).address.equals(this.address);
        else return false;
    }
}
