package com.dezen.riccardo.networkmanager;

import android.content.Context;
import android.util.Log;

import com.dezen.riccardo.smshandler.CommunicationHandler;
import com.dezen.riccardo.smshandler.ReceivedMessageListener;
import com.dezen.riccardo.smshandler.SMSManager;
import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SMSPeer;

//CODE-REVIEW DE ZEN
/**
 * @author Niccolò Turcato. Invitation sending and receiving. Switch actions on Message received.
 * @author Riccardo De Zen. User management and synchronization. Enumeration to define actions.
 */
public class NetworkManager extends NetworkInterface<SMSMessage, SMSPeer,StringResource, NetworkDictionary> implements ReceivedMessageListener<SMSMessage> {
    /**
     * Actions the network can send and receive.
     * Temporary syntax for messages is as follows:
     * <#>[ACTION] [PARAMETER] [EXTRA]
     */
    private enum ACTIONS {
        //<#>INVITE [IGNORED] [IGNORED]
        INVITE(0),
        //<#>ACCEPT [IGNORED] [IGNORED]
        ACCEPT(1),
        //<#>ADD_USER [PEER] [IGNORED]
        ADD_USER(2),
        //<#>REMOVE_USER [PEER] [IGNORED]
        REMOVE_USER(3),
        //<#>GREET_USER [PEER] [IGNORED]
        GREET_USER(4),
        //<#>MSG [MESSAGE]
        MSG(5),
        //<#>ADD_RESOURCE [KEY] [VALUE]
        ADD_RESOURCE(6),
        //<#>REMOVE_RESOURCE [KEY] [IGNORED]
        REMOVE_RESOURCE(7);
        private int val;
        ACTIONS(int num){
            val = num;
        }
        public int value(){return val;}
    }
    private static final String[] actionMessages = {
            "INVITE",
            "ACCEPT",
            "ADD_USER",
            "REMOVE_USER",
            "GREET_USER",
            "MSG",
            "ADD_RESOURCE",
            "REMOVE_RESOURCE"
    };
    //TODO add action private class to allow easier code reading.
    //TODO add questioning whether invited user is already part of the network.
    //TODO add support for multiple networks.

    private boolean isPartOfNetwork;
    private SMSPeer myPeer;
    private OnNetworkEventListener<SMSMessage, StringResource> listener;
    private NetworkDictionary dictionary;
    private CommunicationHandler<SMSMessage> handler;
    private Context context;
    private final String MANAGER_TAG = "MANAGER_TAG";

    public NetworkManager(Context registerContext) {
         dictionary = new NetworkDictionary();
         handler = SMSManager.getInstance(registerContext);
         handler.addReceiveListener(this);
         context = registerContext;
         isPartOfNetwork = false;
    }

    /**
     * This version of the constructor should be used to insert the peer that builds the object
     * (if it is a peer for the sms Network)
     * @param firstPeer
     */
    public NetworkManager(Context registerContext, SMSPeer firstPeer) {
        this(registerContext);
        dictionary.addPeer(firstPeer);
        myPeer = firstPeer;
    }

    /**
     * Method to send an invitation to a new User (Peer)
     * @param newPeer the Peer to invite
     */
    @Override
    public void invite(SMSPeer newPeer) {
        handler.sendMessage(new SMSMessage(newPeer, actionMessages[ACTIONS.INVITE.value()]));
    }

    /**
     * Method to accept an invitation received
     * @param inviter the user that sent the invitation
     */
    @Override
    public void acceptInvite(SMSPeer inviter) {
        handler.sendMessage(new SMSMessage(inviter, actionMessages[ACTIONS.ACCEPT.value()]));
        dictionary.addPeer(inviter);
        isPartOfNetwork = true;
    }

    /**
     * Method for when a user has accepted the invitation
     * @param invited
     */
    private void onInviteAccepted(SMSPeer invited) {
        Log.d(MANAGER_TAG, invited.getAddress() +" has accepted");
        dictionary.addPeer(invited);
        broadcast(ACTIONS.GREET_USER,invited.getAddress(),"");
        //This Peer wasn't part of a network but now it is since someone accepted its invitation.
        if(!isPartOfNetwork) isPartOfNetwork = true;
    }

    /**
     * Method to create a new valid Resource, and notify the network
     * @param newResource the new Resource
     */
    public void createResource(StringResource newResource) {
        dictionary.addResource(newResource);
        broadcast(ACTIONS.ADD_RESOURCE,newResource.getName(),newResource.getValue());
    }

    /**
     * Method to request a Resource from the network.
     * @param key the Resource to request.
     */
    @Override
    public StringResource getResource(String key) {
        for(StringResource res : dictionary.getResources()){
            if(res.getName().equals(key)) return res;
        }
        return StringResource.getDefaultInvalid();
    }

    /**
     * Method to get an array of the Peers.
     * @return array containing all Available Peers.
     */
    @Override
    public SMSPeer[] getAvailablePeers() {
        return dictionary.getPeers();
    }

    /**
     * Method to get an array of the Resources.
     * @return array containing all Available Resources.
     */
    @Override
    public StringResource[] getAvailableResources() {
        return dictionary.getResources();
    }

    /**
     * Setter for a listener that should listen for Resources being obtained.
     * @param listener the class listening for Resource events.
     */
    @Override
    public void setListener(OnNetworkEventListener<SMSMessage, StringResource> listener) {
        this.listener = listener;
    }

    /**
     * Method to send some kind of broadcast to the whole network
     * @param action the action to broadcast
     */
    public void broadcast(ACTIONS action, String param, String extra) {
        for(SMSPeer peer : getAvailablePeers()){
            if(!peer.equals(myPeer))
                handler.sendMessage(
                    new SMSMessage(peer, actionMessages[action.value()]+" "+param+" "+extra)
                );
        }
    }

    /**
     * Method to test a generic broadcast.
     */
    public void smile(){
        broadcast(ACTIONS.MSG,"SMILE","");
    }

    @Override
    public void onMessageReceived(SMSMessage message) {
        String receivedMessageString = message.getData().toString();
        if(receivedMessageString.contains(actionMessages[ACTIONS.INVITE.value()])){
            //Message contains invitation
            SMSPeer inviter = message.getPeer();
            acceptInvite(inviter);
        }

        if(receivedMessageString.contains(actionMessages[ACTIONS.ACCEPT.value()])){
            //Message contains invitation acceptance
            SMSPeer acceptingPeer = message.getPeer();
            onInviteAccepted(acceptingPeer);
        }

        if(receivedMessageString.contains(actionMessages[ACTIONS.ADD_USER.value()])){
            //Add the received parameter user
            dictionary.addPeer(new SMSPeer(receivedMessageString.split(" ")[1]));
        }

        if(receivedMessageString.contains(actionMessages[ACTIONS.GREET_USER.value()])){
            //Notifies the received parameter user that it should add this user
            SMSPeer newPeer = new SMSPeer(receivedMessageString.split(" ")[1]);
            dictionary.addPeer(newPeer);
            handler.sendMessage(new SMSMessage(newPeer,
                    actionMessages[ACTIONS.ADD_USER.value()]+" "+myPeer));
        }

        if(receivedMessageString.contains(actionMessages[ACTIONS.MSG.value()])){
            if(listener != null) listener.onMessageReceived(message);
        }

        if(receivedMessageString.contains(actionMessages[ACTIONS.ADD_RESOURCE.value()])){
            String[] words = receivedMessageString.split(" ");
            String name = words[1];
            StringBuilder value = new StringBuilder();
            for(int i = 2; i < words.length; i++) value.append(words[i]);
            dictionary.addResource(new StringResource(name,value.toString()));
        }

        if(receivedMessageString.contains(actionMessages[ACTIONS.REMOVE_RESOURCE.value()])){
            String name = receivedMessageString.split(" ")[1];
            dictionary.removeResource(new StringResource(name,""));
        }
    }
}
