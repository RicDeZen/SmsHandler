package com.dezen.riccardo.smshandler;

//CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen based on decisions of whole class.
 * @param <D> The type of data this message contains.
 * @param <P> The type of Peer this message allows to communicate.
 */
public interface Message<D, P extends Peer>{
    /**
     * @return the data in this message
     */
    D getData();

    /**
     * @return the peer for this message
     */
    P getPeer();
}
