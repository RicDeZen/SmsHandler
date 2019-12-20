package com.dezen.riccardo.networkmanager;

import com.dezen.riccardo.smshandler.Peer;
//TODO add sizeP() and sizeR() methods. Update tests accordingly.
/**
 * The Dictionary interface, meant to contain two tables/maps/sets, a Peer set and a Resource set.
 * The Address of a Peer is used as it's key for the sake of equals()
 * @param <P> Peer type
 * The Name of a Resource is used as it's key for the sake of equals()
 * @param <R> Resource type
 * @author Riccardo De Zen.
 */
public interface Dictionary<P extends Peer, R extends Resource>{
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

    /**
     * Returns whether the given user exists in this Vocabulary
     * @param peer the Peer to find
     * @return true if it exists (an instance of PeerItem exists whose "peer" value equals the "peer"
     * passed as parameter), false otherwise.
     */
    boolean contains(P peer);

    /**
     * Method to tell whether the given Resource exists in this Vocabulary
     * @param resource the Resource to find
     * @return true if resource exists, false otherwise.
     */
    boolean contains(R resource);

    /**
     * @return the number of contained peers
     */
    int countPeers();

    /**
     *
     * @return the number of contained resources
     */
    int countResources();
}
