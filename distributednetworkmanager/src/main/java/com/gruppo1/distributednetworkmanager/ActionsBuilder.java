package com.gruppo1.distributednetworkmanager;

import com.dezen.riccardo.networkmanager.Resource;
import com.dezen.riccardo.smshandler.Peer;

/**
 * @author Niccolo' Turcato
 *
 * @param <T> the real class that describes an Action of you custom network
 * @param <P> a real type of peer that can be contacted
 * @param <R> a resource type
 * @param <N> a Node of the network
 *
 * Each response is based on a request
 * Responses (ANSW) are built starting from the request (mainly to retrieve the action ID/number)
 */
public interface ActionsBuilder<T extends NodeActionStructure, P extends Peer, R extends Resource, N extends Node> {

    /**
     * @param peer the peer that will be pinged
     * @return an action built to execute the ping command to the peer
     */
    T buildPing(P peer);
    /**
     * @param request the ping request received from a peer (that will be the receiver for the response)
     * @return a response action built to respond to the received ping command from the peer
     */
    T buildPingAnsw(T request);

    /**
     * @param peer the receiving peer (the peer to invite)
     * @return an action built to execute the invite command to the peer
     */
    T buildInvite(P peer);
    /**
     * @param request the received invite request
     * @param accepted set to indicate if the answer accepts the invite or if it rejects it
     * @return a response action built to respond to the received invite
     */
    T buildInviteAnsw(T request, boolean accepted);


    /**
     * @param peer the receiving peer
     * @param node the node to be stored
     * @return an action built to execute the store (node) command to the peer
     */
    T buildStore(P peer, N node);
    /**
     * @param peer the receiving peer
     * @param resource the resource to be stored
     * @return an action built to execute the store (resource) command to the peer
     */
    T buildStore(P peer, R resource);
    /**
     * @param request the received store (node) request action
     * @param peers the peers of which forward addresses
     * @return an array of responses (one for each peer in peers) for the received request
     */
    T[] buildStoreAnsw(T request, P[] peers);
    /**
     * @param request the received store (resource) request action
     * @param confirmStore set to confirm that the store command has been executed
     * @return a response action built to respond to the received store (resource)
     */
    T buildStoreAnsw(T request, boolean confirmStore);


    /**
     * @param peer the peer that will receive the built action command
     * @param node the node of which find its address (on the used network)
     * @return an action built to execute the find node command to the peer
     */
    T buildFindNode(P peer, N node);

    /**
     * @param request the received find node request from a peer
     * @param peers the peers that are closest to the searched node (or the searched node's corresponding peer)
     * @return an array of responses (one for each peer in peers) for the received request
     */
    T[] buildFindNodeAnsw(T request, P[] peers);


    /**
     * @param peer the peer that will receive the built action command
     * @param resourceNode the node representing the searched resource
     * @return an action built to execute the find value (of a resource) command to the peer
     */
    T buildFindValue(P peer, N resourceNode);
    /**
     * @param request the received find value request from a peer
     * @param resource if found the searched resource (by the requesting peer), otherwise a resource containing a fixed token (that indicated that the resource hasn't been found)
     * @return a response action built to respond to the received find value command (of a resource)
     */
    T buildFindValueAnsw(T request, R resource);
    /**
     * @param request the received find value request from a peer
     * @param peers the peers corresponding to the nodes that are closest to the searched resource
     * @return an array of responses (one for each peer in peers) for the received request
     */
    T[] buildFindValueAnsw(T request, P[] peers);
}
