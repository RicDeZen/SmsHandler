package com.gruppo1.distributednetworkmanager;

import com.dezen.riccardo.networkmanager.Resource;
import com.dezen.riccardo.smshandler.Peer;

/**
 *
 * @param <T> the real class that describes an Action of you custom network
 * @param <P> a real type of peer that can be contacted
 * @param <R> a resource type
 * @param <N> a Node of the network
 */
public interface ActionsBuilder<T, P extends Peer, R extends Resource, N extends Node> {

    T buildPing(P peer);


    /**
     * Hypothesis: this type of responses (ANSW) is built starting from the request (mainly to retrieve the action ID/number)
     *
     * QUESTION: is it good?
     */
    T buildPingAnsw(T request);


    T buildInvite(P peer);

    T buildInviteAnsw(T request, boolean accept);



    T buildStore(P peer, R resource);

    T buildStoreAnsw(T request);


    T buildFindNode(P peer, N node);

    T buildFindNodeAnsw(T request, N[] nodes);


    T buildFindValue(P peer, N resourceNode);

    T buildFindValueAnsw(T request, R resource);
}
