package com.dezen.riccardo.networkmanager;

import com.dezen.riccardo.smshandler.Message;
import com.dezen.riccardo.smshandler.Peer;

//CODE-REVIEW DE ZEN
/**
 * Possible interface for Network Dictionary sharing. By default this interface defines only read
 * behaviour.
 * @author Riccardo De Zen.
 * @param <P>
 * @param <R>
 * @param <D>
 */
public abstract class NetworkInterface<M extends Message, P extends Peer, R extends Resource, D extends Dictionary<P,R>>{

    /**
     * Method to send an invitation to a new User (Peer)
     * @param newPeer the Peer to invite
     */
    public abstract void invite(P newPeer);

    /**
     * Method to accept an invitation received
     * @param inviter the user that sent the invitation
     */
    public abstract void acceptInvite(P inviter);

    /**
     * Method to request a Resource from the network.
     * @param key the key of Resource to request.
     */
    public abstract R getResource(String key);

    /**
     * Method to get an array of the Peers.
     * @return array containing all Available Peers.
     */
    public abstract P[] getAvailablePeers();

    /**
     * Method to get an array of the Resources.
     * @return array containing all Available Resources.
     */
    public abstract R[] getAvailableResources();

    /**
     * Setter for a listener that should listen for Resources being obtained.
     * @param listener the class listening for Resource events.
     */
    public abstract void setListener(OnNetworkEventListener<M, R> listener);
}
