package com.dezen.riccardo.networkmanager;

import com.dezen.riccardo.smshandler.Message;

//CODE-REVIEW DE ZEN
/**
 * Listener Interface to use as callback to notify a class of events related to the Network.
 * @author Riccardo De Zen.
 * @param <R>
 */
public interface OnNetworkEventListener<M extends Message, R extends Resource>{
    /**
     * //TODO better onMessageReceived with generic types
     */
    void onMessageReceived(M message);
}
