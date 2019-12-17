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

    T buildPingAnsw(P peer);

    T buildInvite(P peer);

    T buildInviteAnsw(P peer, boolean accept);

    T buildStore(P peer, R resource);

    T buildStoreAnsw(P peer);

    T buildFindNode(P peer, N node);

    T buildFindNodeAnsw(P peer, N[] nodes);

    T buildFindValue(P peer, N resourceNode);

    T buildFindValueAnsw(P peer, R resource);
}
