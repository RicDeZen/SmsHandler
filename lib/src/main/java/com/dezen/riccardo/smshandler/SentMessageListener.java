package com.dezen.riccardo.smshandler;

//CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen
 * @param <M> The type of Message sent.
 */
public interface SentMessageListener<M extends Message> {
    /**
     * Called when a message is sent
     * @param resultCode result code of the sending operation (success or failure)
     * @param message the message the operation tried to send
     */
    void onMessageSent(int resultCode, M message);
}
