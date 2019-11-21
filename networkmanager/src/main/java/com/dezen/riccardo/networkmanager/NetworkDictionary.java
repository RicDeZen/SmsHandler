package com.dezen.riccardo.networkmanager;

import android.content.Context;

import androidx.room.Room;

import com.dezen.riccardo.networkmanager.database_dictionary.PeerDatabase;
import com.dezen.riccardo.networkmanager.database_dictionary.PeerEntity;
import com.dezen.riccardo.networkmanager.database_dictionary.ResourceDatabase;
import com.dezen.riccardo.networkmanager.database_dictionary.ResourceEntity;
import com.dezen.riccardo.smshandler.SMSPeer;

import java.util.HashMap;
import java.util.Map;

import static com.dezen.riccardo.smshandler.SmsHandler.SMS_HANDLER_LOCAL_DATABASE;

/**
 * Class implementing Dictionary. Conceived as a double dictionary on SMSPeer and StringResource.
 * Due to trouble with testing android class. Maps have been used.
 *
 * @author Riccardo De Zen, Giorgia Bortoletti
 */
public class NetworkDictionary implements Dictionary<SMSPeer, StringResource> {
    private Map<String, String>  peers;
    private Map<String, String> resources;
    private NetworkDictionaryDatabase database;

    /**
     * Constructor of NetworkDictionary
     * @param context
     */
    public NetworkDictionary(Context context){
        database = new NetworkDictionaryDatabase(context);
        peers = new HashMap<>();
        resources = new HashMap<>();
    }

    //TODO? make this method asynchronous
    /**
     * Imports resources and peers from database to variables Map
     */
    private void importFromDatabase(){
        SMSPeer[] smsPeers = database.getPeers();
        StringResource[] stringResources = database.getResources();

        for(SMSPeer peer : smsPeers)
            peers.put(peer.getAddress(), "");

        for(StringResource resource : stringResources)
            resources.put(resource.getName(), resource.getName());

    }

    //TODO? make this method asynchronous
    /**
     * Exports resources and peer from maps to database
     */
    private void exportToDatabase(){

        for(Map.Entry<String, String> peerEntry : peers.entrySet())
        {
            SMSPeer smsPeer = new SMSPeer(peerEntry.getKey());
            if(!database.containsPeer(smsPeer))
            {
                database.addPeer(smsPeer);
            }
        }

        for(Map.Entry<String, String> resourceEntry : resources.entrySet())
        {
            StringResource stringResource = new StringResource(resourceEntry.getKey(), resourceEntry.getValue());
            if(!database.containsResource(stringResource)){
                database.addResource(stringResource);
            }else{
                database.updateResource(stringResource);
            }
        }
    }

    /**
     * Adds a new Peer. Null Peer will not be inserted.
     * @param newPeer the new Peer, whose key does not already exist
     * @return true if the newPeer is was added, false otherwise
     */
    @Override
    public boolean addPeer(SMSPeer newPeer){
        if(contains(newPeer)) return false;
        peers.put(newPeer.getAddress(), "");
        return true;
    }

    /**
     * Removes the Peer with the matching key, if it exists. Null Peer will not be removed.
     * @param peerToRemove Peer to remove
     * @return true if it was removed, false otherwise
     */
    @Override
    public boolean removePeer(SMSPeer peerToRemove) {
        if(!contains(peerToRemove)) return false;
        peers.remove(peerToRemove.getAddress());
        return true;
    }

    /**
     * Updates the Peer with the matching key, if it exists.
     * @param updatedPeer the new value for the Peer if one with a matching key exists
     * @return true if the Peer was found and updated, false otherwise
     */
    @Override
    public boolean updatePeer(SMSPeer updatedPeer) {
        if(!contains(updatedPeer)) return false;
        peers.remove(updatedPeer.getAddress());
        peers.put(updatedPeer.getAddress(),"");
        return true;
    }

    /**
     * Returns an array containing all Peers. Peers are not copied singularly.
     * @return a list containing all Peers
     */
    @Override
    public SMSPeer[] getPeers() {
        SMSPeer[] peerArray = new SMSPeer[peers.size()];
        int i = 0;
        for(Map.Entry<String, String> peerEntry : peers.entrySet())
        {
            peerArray[i++] = new SMSPeer(peerEntry.getKey());
        }

        return peerArray;
    }

    /**
     * Returns whether the given user exists in this Vocabulary
     * @param peer the Peer to find
     * @return true if Peer exists, false otherwise.
     */
    public boolean contains(SMSPeer peer){
        if(peer == null && !peer.isValid()) return false;
        return peers.containsKey(peer.getAddress());
    }

    /**
     * Adds a new resource. Null or invalid Resource won't be added.
     * @param newResource the new Resource, whose key does not already exist
     * @return true if it is added, false otherwise.
     */
    @Override
    public boolean addResource(StringResource newResource) {
        if(contains(newResource)) return false;
        resources.put(newResource.getName(), newResource.getValue());
        return true;
    }

    /**
     * Removes the Resource with the matching key, if it exists.
     * @param resourceToRemove the Resource to remove
     * @return true if it was removed, false otherwise
     */
    @Override
    public boolean removeResource(StringResource resourceToRemove) {
        if(!contains(resourceToRemove))
            return false;
        resources.remove(resourceToRemove.getName());
        return true;
    }

    /**
     * Updates the value of a Resource. Null or invalid Resource won't be updated.
     * @param updatedResource the new value for the Resource
     * @return true if the resource was found and updated, false otherwise.
     */
    @Override
    public boolean updateResource(StringResource updatedResource) {
        if(!contains(updatedResource))
            return false;
        resources.remove(updatedResource.getName());
        resources.put(updatedResource.getName(), updatedResource.getName());
        return true;
    }

    /**
     * Returns a copy of the list of all Resources. Resources are not copied singularly.
     * @return a list containing all Resources
     */
    @Override
    public StringResource[] getResources() {
        StringResource[] resourcesArray = new StringResource[resources.size()];
        int i = 0;
        for(Map.Entry<String, String> resourceEntry : resources.entrySet())
        {
            resourcesArray[i++] = new StringResource(resourceEntry.getKey(), resourceEntry.getValue());
        }

        return resourcesArray;
    }

    /**
     * Method to tell whether the given Resource exists in this Vocabulary
     * @param resource the Resource to find
     * @return true if resource exists, false otherwise.
     */
    public boolean contains(StringResource resource){
        if(resource == null && !resource.isValid()) return false;
        return resources.containsKey(resource.getName());
    }

    /**
     * Class to interact with Peer Database e Resource Database
     * @author Giorgia Bortoletti
     */
    private class NetworkDictionaryDatabase{

        //TODO? add return boolean in the methods
        private ResourceDatabase resourceDatabase;
        private PeerDatabase peerDatabase;

        /**
         * Constructor of NetworkDictionaryDatabase
         * @param context
         */
        private NetworkDictionaryDatabase(Context context) {
            this.resourceDatabase = Room.databaseBuilder(context, ResourceDatabase.class, SMS_HANDLER_LOCAL_DATABASE)
                    .enableMultiInstanceInvalidation()
                    .build();
            this.peerDatabase = Room.databaseBuilder(context, PeerDatabase.class, SMS_HANDLER_LOCAL_DATABASE)
                    .enableMultiInstanceInvalidation()
                    .build();
        }

        /**
         * Adds a new Peer. Null Peer will not be inserted.
         * @param newPeer the new Peer, whose key does not already exist
         */
        private void addPeer(SMSPeer newPeer){
            peerDatabase.access().add(new PeerEntity(newPeer.getAddress()));
        }

        /**
         * Removes the Peer with the matching key, if it exists. Null Peer will not be removed.
         * @param peerToRemove Peer to remove
         */
        private void removePeer(SMSPeer peerToRemove) {
            peerDatabase.access().remove(new PeerEntity(peerToRemove.getAddress()));
        }

        /**
         * Updates the value of a Peer. Null or invalid Resource won't be updated.
         * @param updatedPeer the new value for the Peer
         */
        private void updatePeer(SMSPeer updatedPeer) {
            peerDatabase.access().update(new PeerEntity(updatedPeer.getAddress()));
        }

        /**
         * Verify if a Peer is in the database
         * @param searchPeer the new value for the Peer
         * @return true if the peer was found
         */
        private boolean containsPeer(SMSPeer searchPeer) {
            return peerDatabase.access().contains(searchPeer.getAddress());
        }

        /**
         * Returns an array containing all Peers. Peers are not copied singularly.
         * @return a list containing all Peers
         */
        private SMSPeer[] getPeers() {
            int numbersPeer = peerDatabase.access().getAll().length;
            SMSPeer[] peerArray = new SMSPeer[numbersPeer];
            PeerEntity[] peerEntities = peerDatabase.access().getAll();
            for(int i=0; i<numbersPeer; i++)
                peerArray[i] = new SMSPeer(peerEntities[i].address);
            return peerArray;
        }

        /**
         * Adds a new resource. Null or invalid Resource won't be added.
         * @param newResource the new Resource, whose key does not already exist
         */
        private void addResource(StringResource newResource) {
            resourceDatabase.access().add(new ResourceEntity(newResource.getName(), newResource.getValue()));
        }

        /**
         * Removes the Resource with the matching key, if it exists.
         * Null or invalid Resource won't be searched for.
         * @param resourceToRemove the Resource to remove
         */
        private void removeResource(StringResource resourceToRemove) {
            resourceDatabase.access().remove(new ResourceEntity(resourceToRemove.getName(), resourceToRemove.getValue()));
        }

        /**
         * Updates the value of a Resource. Null or invalid Resource won't be updated.
         * @param updatedResource the new value for the Resource
         */
        private void updateResource(StringResource updatedResource) {
            resourceDatabase.access().update(new ResourceEntity(updatedResource.getName(), updatedResource.getName()));
        }

        /**
         * Returns an array containing all Resources. Resources are not copied singularly.
         * @return a list containing all Resources
         */
        private StringResource[] getResources() {
            int numbersResource = resourceDatabase.access().getAll().length;
            StringResource[] resourceArray = new StringResource[numbersResource];
            ResourceEntity[] resourceEntities = resourceDatabase.access().getAll();
            for(int i=0; i<numbersResource; i++)
                resourceArray[i] = new StringResource(resourceEntities[i].keyName, resourceEntities[i].value);
            return resourceArray;
        }

        /**
         * Verify if a Resource is in the database
         * @param searchResource the new value for the Peer
         * @return true if the Resource was found
         */
        private boolean containsResource(StringResource searchResource) {
            return peerDatabase.access().contains(searchResource.getName());
        }


    }


}
