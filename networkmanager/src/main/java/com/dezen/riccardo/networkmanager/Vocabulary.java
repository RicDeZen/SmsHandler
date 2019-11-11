package com.dezen.riccardo.networkmanager;

import com.dezen.riccardo.smshandler.Peer;

/**
 * The Address of a Peer is used as it's key
 * @param <P> Peer type
 * The Name of a Resource is used as it's key
 * @param <R> Resource type
 */
public interface Vocabulary<P extends Peer, R extends Resource>{
    /**
     * Adds a new Peer
     * @param newPeer the new Peer, whose key does not already exist
     * @return true if the Peer was added, false otherwise
     */
    boolean addPeer(P newPeer);

    /**
     * Removes the Peer with the matching key, if it exists
     * @param peerToRemove Peer to remove
     * @return true if it was removed, false otherwise
     */
    boolean removePeer(P peerToRemove);

    /**
     * Updates the Peer with the matching key, if it exists
     * @param updatedPeer the new value for the Peer if one with a matching key exists
     * @return true if the Peer was updated, false if it didn't exist
     */
    boolean updatePeer(P updatedPeer);

    /**
     * Returns an array that contains all Peers.
     * @return an array containing all Peers
     */
    P[] getPeers();

    /**
     * Adds a new resource
     * @param newResource the new Resource, whose key does not already exist
     * @return true if the Resource was added, false otherwise
     */
    boolean addResource(R newResource);

    /**
     * Removes the Resource with the matching key, if it exists
     * @param resourceToRemove the Resource to remove
     * @return true if it was removed, false otherwise
     */
    boolean removeResource(R resourceToRemove);

    /**
     * Updates the value of a Resource, if it exists
     * @param updatedResource the new value for the Resource
     * @return true if the resource was updated, false otherwise.
     */
    boolean updateResource(R updatedResource);

    /**
     * Returns an array containing all Resources.
     * @return an array containing all Resources
     */
    R[] getResources();
}
