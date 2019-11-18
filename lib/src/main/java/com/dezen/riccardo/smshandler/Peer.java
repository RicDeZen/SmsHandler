package com.dezen.riccardo.smshandler;

//CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen. Based on suggestion from Dr. Li Dao Hong.
 * @param <T> The type of address that identifies the Peer.
 */
//TODO turn Peer into abstract class to implement equals for better generics handling in networkDictionary.
public interface Peer<T> {
    /**
     * @return the address of this Peer
     */
    T getAddress();
}
