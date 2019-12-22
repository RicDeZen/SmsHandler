package com.gruppo1.distributednetworkmanager;

import com.dezen.riccardo.networkmanager.StringResource;
import com.dezen.riccardo.smshandler.SMSPeer;

import java.util.Random;

/**
 * @author Niccolo' Turcato
 * Builds KadAction objs with randomized ids (1-999)
 * <p>
 * TODO: add detailed description (summary from De Zen's Paper)
 */
public class KadActionsBuilder implements ActionsBuilder<KadAction, SMSPeer, StringResource, Node<BinarySet>> {


    private static int MAX_ACTION_ID;
    private static int DEFAULT_PARTS = KadAction.MIN_PARTS;
    private static final String PAYLOAD_IGNORED = "0w0";
    private static final String ILLEGAL_MAX_ID_MSG = "Can't initialize Builder, the defined max ID isn't compatible with class KadActions";
    public static final String RESOURCE_SEPARATOR = KadAction.RESOURCE_SEPARATOR;

    public static final KadAction INVALID_ACTION = KadAction.INVALID_KAD_ACTION;


    /**
     * Constructor for this builder, defines a new max limit for IDs, min limit remains the one defined in KadAction
     *
     * @param maxActionID max value for action IDs that this builder will accept, must be a valid KadAction ID
     */
    public KadActionsBuilder(int maxActionID) {
        if (maxActionID >= KadAction.MIN_ID && maxActionID <= KadAction.MAX_ID)
            MAX_ACTION_ID = maxActionID;
        else
            throw new IllegalArgumentException(ILLEGAL_MAX_ID_MSG);

    }

    /**
     * Default constructor, builder will accept all valid IDs, (as indicated in KadAction)
     */
    public KadActionsBuilder() {
        MAX_ACTION_ID = KadAction.MAX_ID;
    }

    private boolean isValidID(int id) {
        return id >= KadAction.MIN_ID && id <= MAX_ACTION_ID;
    }

    /**
     * @param peer     the peer that will be pinged
     * @param actionID the unique ID of the action
     * @return if the given ID is valid: an action built to execute the ping command to the peer, otherwise returns an INVALID_ACTION
     */
    public KadAction buildPing(int actionID, SMSPeer peer) {
        if (isValidID(actionID))
            return new KadAction(
                    peer,
                    KadAction.ActionType.PING,
                    actionID,
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.IGNORED,
                    PAYLOAD_IGNORED
            );
        else return INVALID_ACTION;
    }

    /**
     * @param request the ping request received from a peer (that will be the receiver for the response)
     * @return a response action built to respond to the received ping command from the peer, or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction buildPingAnsw(KadAction request) {
        if (request.getActionType() == KadAction.ActionType.PING)
            return new KadAction(
                    request.getPeer(),
                    KadAction.ActionType.PING_ANSWER,
                    request.getOperationId(),
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.IGNORED,
                    PAYLOAD_IGNORED
            );
        else
            return INVALID_ACTION;
    }

    /**
     * @param peer     the receiving peer (the peer to invite)
     * @param actionID the unique ID of the action
     * @return if the given ID is valid: an action built to execute the invite command to the peer, otherwise returns an INVALID_ACTION
     */
    public KadAction buildInvite(int actionID, SMSPeer peer) {
        if (isValidID(actionID))
            return new KadAction(
                    peer,
                    KadAction.ActionType.INVITE,
                    actionID,
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.IGNORED,
                    PAYLOAD_IGNORED
            );
        else
            return INVALID_ACTION;
    }

    /**
     * @param request  the received invite request
     * @param accepted set to indicate if the answer accepts the invite or if it rejects it
     * @return a response action built to respond to the received invite, or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction buildInviteAnsw(KadAction request, boolean accepted) {
        if (request.getActionType() == KadAction.ActionType.INVITE)
            return new KadAction(
                    request.getPeer(),
                    KadAction.ActionType.INVITE_ANSWER,
                    request.getOperationId(),
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.BOOLEAN,
                    Boolean.toString(accepted)
            );
        else
            return INVALID_ACTION;
    }


    /**
     * @param actionID the unique ID of the action
     * @param peer     the receiving peer
     * @param node     the node to be stored
     * @return if the given ID is valid: an action built to execute the store (node) command to the peer, otherwise returns an INVALID_ACTION
     */
    public KadAction buildStore(int actionID, SMSPeer peer, Node<BinarySet> node) {
        if (isValidID(actionID))
            return new KadAction(
                    peer,
                    KadAction.ActionType.STORE,
                    actionID,
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.NODE_ID,
                    node.getKey().toHex()
            );
        else
            return INVALID_ACTION;
    }

    /**
     * @param actionID the unique ID of the action
     * @param peer     the receiving peer
     * @param resource the resource to be stored
     * @return if the given ID is valid: an action built to execute the store (resource) command to the peer, otherwise returns an INVALID_ACTION
     */
    public KadAction buildStore(int actionID, SMSPeer peer, StringResource resource) {
        if (isValidID(actionID))
            return new KadAction(
                    peer,
                    KadAction.ActionType.STORE,
                    actionID,
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.RESOURCE,
                    resource.getName() + RESOURCE_SEPARATOR + resource.getValue()
            );
        else
            return INVALID_ACTION;
    }

    /**
     * @param request the received store (node) request action
     * @param peers   the peers of which forward addresses
     * @return an array of responses (one for each peer in peers) for the received request, or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction[] buildStoreAnsw(KadAction request, SMSPeer[] peers) {
        if (request.getActionType() == KadAction.ActionType.STORE) {
            KadAction[] responses = new KadAction[peers.length];
            for (int i = 0; i < peers.length; i++) {
                responses[i] = new KadAction(
                        request.getPeer(),
                        KadAction.ActionType.STORE_ANSWER,
                        request.getOperationId(),
                        i + 1, peers.length,
                        KadAction.PayloadType.PEER_ADDRESS,
                        peers[i].getAddress()
                );
            }
            return responses;
        } else
            return new KadAction[]{INVALID_ACTION};
    }

    /**
     * @param request      the received store (resource) request action
     * @param confirmStore set to confirm that the store command has been executed
     * @return a response action built to respond to the received store (resource), or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction buildStoreAnsw(KadAction request, boolean confirmStore) {
        if (request.getActionType() == KadAction.ActionType.STORE)
            return new KadAction(
                    request.getPeer(),
                    KadAction.ActionType.STORE_ANSWER,
                    request.getOperationId(),
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.BOOLEAN,
                    Boolean.toString(confirmStore)
            );
        else
            return INVALID_ACTION;
    }


    /**
     * @param actionID the unique ID of the action
     * @param peer     the peer that will receive the built action command
     * @param node     the node of which find its address (on the used network)
     * @return if the given ID is valid: an action built to execute the find node command to the peer, otherwise returns an INVALID_ACTION
     */
    public KadAction buildFindNode(int actionID, SMSPeer peer, Node<BinarySet> node) {
        return new KadAction(
                peer,
                KadAction.ActionType.FIND_NODE,
                actionID,
                DEFAULT_PARTS, DEFAULT_PARTS,
                KadAction.PayloadType.NODE_ID,
                node.getKey().toHex()
        );
    }

    /**
     * @param request the received find node request from a peer
     * @param peers   the peers that are closest to the searched node (or the searched node's corresponding peer)
     * @return an array of responses (one for each peer in peers) for the received request, or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction[] buildFindNodeAnsw(KadAction request, SMSPeer[] peers) {
        if (request.getActionType() == KadAction.ActionType.FIND_NODE) {
            KadAction[] responses = new KadAction[peers.length];
            for (int i = 0; i < peers.length; i++) {
                responses[i] = new KadAction(
                        request.getPeer(),
                        KadAction.ActionType.FIND_NODE_ANSWER,
                        request.getOperationId(),
                        i + 1, peers.length,
                        KadAction.PayloadType.PEER_ADDRESS,
                        peers[i].getAddress()
                );
            }
            return responses;
        }
        else
            return new KadAction[]{INVALID_ACTION};
    }


    /**
     * @param actionID     the unique ID of the action
     * @param peer         the peer that will receive the built action command
     * @param resourceNode the node representing the searched resource
     * @return if the given ID is valid: an action built to execute the find value command to the peer, otherwise returns an INVALID_ACTION
     */
    public KadAction buildFindValue(int actionID, SMSPeer peer, Node<BinarySet> resourceNode) {
        if (isValidID(actionID))
            return new KadAction(
                    peer,
                    KadAction.ActionType.FIND_VALUE,
                    actionID,
                    DEFAULT_PARTS, DEFAULT_PARTS,
                    KadAction.PayloadType.NODE_ID,
                    resourceNode.getKey().toHex()
            );
        else
            return INVALID_ACTION;
    }

    /**
     * @param request  the received find value request from a peer
     * @param resource if found the searched resource (by the requesting peer), otherwise a resource containing a fixed token (that indicated that the resource hasn't been found)
     * @return a response action built to respond to the received find value command (of a resource), or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction buildFindValueAnsw(KadAction request, StringResource resource) {
        if(request.getActionType() == KadAction.ActionType.FIND_VALUE)
        return new KadAction(
                request.getPeer(),
                KadAction.ActionType.FIND_VALUE_ANSWER,
                request.getOperationId(),
                DEFAULT_PARTS, DEFAULT_PARTS,
                KadAction.PayloadType.RESOURCE,
                resource.getName() + RESOURCE_SEPARATOR + resource.getValue()
        );
        else
            return INVALID_ACTION;
    }

    /**
     * @param request the received find value request from a peer
     * @param peers   the peers corresponding to the nodes that are closest to the searched resource
     * @return an array of responses (one for each peer in peers) for the received request, or an invalid KAD action if the given action wasn't coherent with this building method (actionType)
     */
    public KadAction[] buildFindValueAnsw(KadAction request, SMSPeer[] peers) {
        if(request.getActionType() == KadAction.ActionType.FIND_VALUE) {
            KadAction[] responses = new KadAction[peers.length];
            for (int i = 0; i < peers.length; i++) {
                responses[i] = new KadAction(
                        request.getPeer(),
                        KadAction.ActionType.FIND_VALUE_ANSWER,
                        request.getOperationId(),
                        i + 1, peers.length,
                        KadAction.PayloadType.PEER_ADDRESS,
                        peers[i].getAddress()
                );
            }
            return responses;
        }
        else
            return new KadAction[]{ INVALID_ACTION};
    }
}
