package com.dezen.riccardo.networkmanager;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.dezen.riccardo.networkmanager.database_dictionary.DictionaryDatabase;
import com.dezen.riccardo.networkmanager.database_dictionary.PeerEntity;
import com.dezen.riccardo.networkmanager.database_dictionary.ResourceEntity;
import com.dezen.riccardo.smshandler.SMSPeer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.valueOf;

/**
 * Class implementing Dictionary. Conceived as a double dictionary on SMSPeer and StringResource,
 * also allowing Peer ownership of Resources.
 * Due to trouble with testing android class. Lists have been used.
 * @author Riccardo De Zen, Giorgia Bortoletti
 */
public class NetworkDictionary implements Dictionary<SMSPeer, StringResource> {

    public static final String NETWORK_DICTIONARY_DATABASE_NAME = "NETWORK_DICTIONARY_DATABASE";
    public static final String NETWORK_DICTIONARY_PEER_TABLE_NAME = "peerentity";
    public static final String NETWORK_DICTIONARY_RESOURCE_TABLE_NAME = "resourceentity";
    public static final String PEER_TABLE_ADDRESS_COLUMN_NAME = "address";
    public static final String RESOURCE_TABLE_KEY_COLUMN_NAME = "key";
    public static final String RESOURCE_TABLE_VALUE_COLUMN_NAME = "value";

    private Map<String, String> peers;
    private Map<String, String> resources;
    private ArrayList<SMSPeer> deletedPeers;
    private ArrayList<StringResource> deletedResources;
    private NetworkDictionaryDatabase database;
    private ImportFromDatabasesTask importFromDatabasesTask;
    private ExportToDatabaseTask exportToDatabaseTask;

    public static StringResource INVALID_RESOURCE = new StringResource("I'm invalid","empty");
    public static SMSPeer INVALID_PEER = null; //TODO: find a better solution

    /**
     * Constructor of NetworkDictionary
     * @param context application context
     */
    public NetworkDictionary(Context context){
        database = new NetworkDictionaryDatabase(context);
        peers = new HashMap<>();
        resources = new HashMap<>();
        importFromDatabase();
        deletedResources = new ArrayList<>();
        deletedPeers = new ArrayList<>();
    }

    /**
     * Imports resources and peers from database to variables Map
     */
    private void importFromDatabase(){
        importFromDatabasesTask = new ImportFromDatabasesTask();
        importFromDatabasesTask.execute(database);
    }

    /**
     * @author Niccolo' Turcato
     * @param mayInterruptIfRunning true if the thread executing this task should be interrupted; otherwise, in-progress tasks are allowed to complete
     */
    private void cancelImportFromDatabase(boolean mayInterruptIfRunning)
    {
        importFromDatabasesTask.cancel(mayInterruptIfRunning);
    }

    /**
     * @author Niccolo' Turcato
     * @return Returns true if this task was cancelled before it completed normally.
     */
    private boolean importFromDatabaseIsCanceled()
    {
        return importFromDatabasesTask.isCancelled();
    }


    /**
     * Exports resources and peer from maps to database
     */
    private void exportToDatabase(){
        exportToDatabaseTask = new ExportToDatabaseTask();
        exportToDatabaseTask.execute(database);
    }

    /**
     * @author Niccolo' Turcato
     * @param mayInterruptIfRunning true if the thread executing this task should be interrupted; otherwise, in-progress tasks are allowed to complete
     */
    private void cancelexportToDatabase(boolean mayInterruptIfRunning)
    {
        exportToDatabaseTask.cancel(mayInterruptIfRunning);
    }

    /**
     * @author Niccolo' Turcato
     * @return Returns true if this task was cancelled before it completed normally.
     */
    private boolean exportToDatabaseIsCanceled()
    {
        return exportToDatabaseTask.isCancelled();
    }

    /**
     * Adds a new Peer. Null Peer will not be inserted.
     * @param newPeer the new Peer, whose key does not already exist
     * @return true if the newPeer has been added, false otherwise
     */
    @Override
    public boolean addPeer(SMSPeer newPeer){
        if(newPeer == null || contains(newPeer)) return false;
        peers.put(newPeer.getAddress(), "");
        return contains(newPeer);
    }

    /**
     * Removes the Peer with the matching key, if it exists. Null Peer will not be removed.
     * @param peerToRemove Peer to remove
     * @return true if peerToRemove has been removed, false otherwise
     */
    @Override
    public boolean removePeer(SMSPeer peerToRemove) {
        if(peerToRemove == null || !contains(peerToRemove)) return false;
        peers.remove(peerToRemove.getAddress());
        if(!contains(peerToRemove))
            deletedPeers.add(peerToRemove);
        //Tracking removed peers to then update backup db
        return !contains(peerToRemove);
    }

    /**
     * Updates the Peer with the matching key, if it exists.
     * @param updatedPeer the new value for the Peer if one with a matching key exists
     * @return true if the Peer was found and updated, false otherwise
     */
    @Override
    public boolean updatePeer(SMSPeer updatedPeer) {
        if(updatedPeer == null || !contains(updatedPeer)) return false;
        peers.remove(updatedPeer.getAddress());
        peers.put(updatedPeer.getAddress(),"");
        return contains(updatedPeer);
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
        if(peer == null || !peer.isValid()) return false;
        return peers.containsKey(peer.getAddress());
    }

    /**
     * Method to tell whether the given Resource exists in this Vocabulary
     * @param resource the Resource to find
     * @return true if resource exists, false otherwise.
     */
    public boolean contains(StringResource resource){
        if(resource == null || !resource.isValid()) return false;
        return resources.containsKey(resource.getName());
    }

    /**
     * Adds a new resource. Null or invalid Resource won't be added.
     * @param newResource the new Resource, whose key does not already exist
     * @return true if it is added, false otherwise.
     */
    @Override
    public boolean addResource(StringResource newResource) {
        if(newResource == null || contains(newResource)) return false;
        resources.put(newResource.getName(), newResource.getValue());
        return contains(newResource);
    }

    /**
     * Removes the Resource with the matching key, if it exists.
     * @param resourceToRemove the Resource to remove
     * @return true if it was removed, false otherwise
     */
    @Override
    public boolean removeResource(StringResource resourceToRemove) {
        if(resourceToRemove == null || !contains(resourceToRemove)) return false;
        resources.remove(resourceToRemove.getName());
        if(!contains(resourceToRemove))
            deletedResources.add(resourceToRemove);
        //Tracking removed resources to then update backup db
        return !contains(resourceToRemove);
    }

    /**
     * Updates the value of a Resource. Null or invalid Resource won't be updated.
     * @param updatedResource the new value for the Resource
     * @return true if the resource was found and updated, false otherwise.
     */
    @Override
    public boolean updateResource(StringResource updatedResource) {
        if(updatedResource == null || !contains(updatedResource)) return false;
        resources.remove(updatedResource.getName());
        resources.put(updatedResource.getName(), updatedResource.getName());
        return contains(updatedResource);
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
     *
     * @param resourceName the wanted resource's key
     * @return if found the wanted resource, if not returns the defined INVALID_RESOURCE
     */
    public StringResource getResourceByName(String resourceName)
    {
        StringResource wantedResource = new StringResource(resourceName, "");
        StringResource[] resourcesArray = getResources();
        for (StringResource res: resourcesArray) {
            if (res.equals(wantedResource))
                return res;
        }
        return INVALID_RESOURCE;
    }

    /**
     *
     * @param peerAddress the wanted peer's address
     * @return the wanted SMSPeer if present, if not returns the INVALID_PEER
     */
    public SMSPeer getPeerByAddress(String peerAddress)
    {
        SMSPeer wantedPeer = new SMSPeer(peerAddress);
        if(wantedPeer.isValid()) {
            SMSPeer[] peersArray = getPeers();
            for (SMSPeer searchingPeer:peersArray) {
                if(searchingPeer.equals(wantedPeer))
                    return searchingPeer;
            }
        }
        return INVALID_PEER;
    }


    /**
     * @return the number of contained peers
     */
    public int countPeers(){
        return peers.size();
    }

    /**
     * @return the number of contained resources
     */
    public int countResources(){
        return resources.size();
    }

    /**
     * Class to interact with Peer Database e Resource Database
     * @author Giorgia Bortoletti
     */
    private class NetworkDictionaryDatabase{

        private DictionaryDatabase dictionaryDatabase;

        /**
         * Constructor of NetworkDictionaryDatabase
         * @param context application context
         */
        private NetworkDictionaryDatabase(Context context) {
            this.dictionaryDatabase = Room.databaseBuilder(context, DictionaryDatabase.class, NETWORK_DICTIONARY_DATABASE_NAME)
                    .enableMultiInstanceInvalidation()
                    .build();
        }

        /**
         * Adds a new Peer. Null Peer will not be inserted.
         * @param newPeer the new Peer, whose key does not already exist
         * @return true if peer has been added, false otherwise.
         */
        private boolean addPeer(SMSPeer newPeer){
            dictionaryDatabase.access().addPeer(new PeerEntity(newPeer.getAddress()));
            return containsPeer(newPeer);
        }

        /**
         * Removes the Peer with the matching key, if it exists. Null Peer will not be removed.
         * @param peerToRemove Peer to remove
         * @return true if peer doesn't exist, false otherwise.
         */
        private boolean removePeer(SMSPeer peerToRemove) {
            dictionaryDatabase.access().removePeer(new PeerEntity(peerToRemove.getAddress()));
            return !containsPeer(peerToRemove);
        }

        /**
         * Updates the value of a Peer. Null or invalid Resource won't be updated.
         * @param updatedPeer the new value for the Peer
         * @return true if peer has been updated, false otherwise.
         */
        private boolean updatePeer(SMSPeer updatedPeer) {
            dictionaryDatabase.access().updatePeer(new PeerEntity(updatedPeer.getAddress()));
            return containsPeer(updatedPeer);
        }

        /**
         * Verify if a Peer is in the database
         * @param searchPeer the new value for the Peer
         * @return true if the peer was found
         */
        private boolean containsPeer(SMSPeer searchPeer) {
            return dictionaryDatabase.access().containsPeer(searchPeer.getAddress());
        }

        /**
         * Returns an array containing all Peers. Peers are not copied singularly.
         * @return a list containing all Peers
         */
        private List<SMSPeer> getPeers() {
            List<SMSPeer> peerList = new ArrayList<SMSPeer>();
            List<PeerEntity> peerEntities = dictionaryDatabase.access().getAllPeers();
            for(PeerEntity peer : peerEntities)
                peerList.add(new SMSPeer(peer.address));
            return peerList;
        }

        /**
         * Adds a new resource. Null or invalid Resource won't be added.
         * @param newResource the new Resource, whose key does not already exist
         * @return true if resource has been added, false otherwise.
         */
        private boolean addResource(StringResource newResource) {
            dictionaryDatabase.access().addResource(new ResourceEntity(newResource.getName(), newResource.getValue()));
            return containsResource(newResource);
        }

        /**
         * Removes the Resource with the matching key, if it exists.
         * Null or invalid Resource won't be searched for.
         * @param resourceToRemove the Resource to remove
         * @return true if resource doesn't exist, false otherwise.
         */
        private boolean removeResource(StringResource resourceToRemove) {
            dictionaryDatabase.access().removeResource(new ResourceEntity(resourceToRemove.getName(), resourceToRemove.getValue()));
            return !containsResource(resourceToRemove);
        }

        /**
         * Updates the value of a Resource. Null or invalid Resource won't be updated.
         * @param updatedResource the new value for the Resource
         * @return return !true if resource has been updated, false otherwise.
         */
        private boolean updateResource(StringResource updatedResource) {
            dictionaryDatabase.access().updateResource(new ResourceEntity(updatedResource.getName(), updatedResource.getName()));
            return !containsResource(updatedResource);
        }

        /**
         * Returns an array containing all Resources. Resources are not copied singularly.
         * @return a list containing all Resources
         */
        private List<StringResource> getResources() {
            List<StringResource> resourceList = new ArrayList<>();
            List<ResourceEntity> resourceEntities = dictionaryDatabase.access().getAllResources();
            for(ResourceEntity resource : resourceEntities)
                resourceList.add(new StringResource(resource.key, resource.value));
            return resourceList;
        }

        /**
         * Verify if a Resource is in the database
         * @param searchResource the new value for the Peer
         * @return true if the Resource was found
         */
        private boolean containsResource(StringResource searchResource) {
            return dictionaryDatabase.access().containsResource(searchResource.getName());
        }


    }

    /**
     * @author Niccolo' Turcato
     *
     * Imports resources and peers from database to variables Map in async Task
     */
    private class ImportFromDatabasesTask extends AsyncTask<NetworkDictionaryDatabase, Integer, Long>
    {
        /**
         * @param database array of databases from which extract peers and resources
         * @return 1 if task has been canceled, 0 if task has been executed
         */
        protected Long doInBackground(NetworkDictionaryDatabase ... database)
        {
            for (NetworkDictionaryDatabase db: database) {
                List<SMSPeer> smsPeers = db.getPeers();
                List<StringResource> stringResources = db.getResources();

                //Could this be done better?
                // Maybe return the arrays so that maps are handled in main thread?
                for(SMSPeer peer : smsPeers)
                    peers.put(peer.getAddress(), "");

                for(StringResource resource : stringResources)
                    resources.put(resource.getName(), resource.getName());

                if (isCancelled()) return valueOf(1);
            }
            return  valueOf(0);
        }

        protected void onProgressUpdate(Integer ... progress)
        {

        }

        protected void onPostExecute(Long result)
        {

        }
    }

    /**
     * @author Niccolo' Turcato
     *
     * Exports resources and peer from maps to database in async Task
     */
    private class ExportToDatabaseTask extends AsyncTask<NetworkDictionaryDatabase, Integer, Long>
    {
        /**
         * @param database array of databases to which export peers and resources
         * @return 1 if task has been canceled, 0 if task has been executed
         */
        protected Long doInBackground(NetworkDictionaryDatabase ... database)
        {
            for (NetworkDictionaryDatabase db: database) {

                for (SMSPeer peer : deletedPeers)
                    if(db.containsPeer(peer))
                        db.removePeer(peer);

                for (StringResource resource: deletedResources)
                    if(db.containsResource(resource))
                        db.removeResource(resource);

                for(Map.Entry<String, String> peerEntry : peers.entrySet())
                {
                    SMSPeer smsPeer = new SMSPeer(peerEntry.getKey());
                    if(!db.containsPeer(smsPeer))
                    {
                        db.addPeer(smsPeer);
                    }
                }

                for(Map.Entry<String, String> resourceEntry : resources.entrySet())
                {
                    StringResource stringResource = new StringResource(resourceEntry.getKey(), resourceEntry.getValue());
                    if(!db.containsResource(stringResource)){
                        db.addResource(stringResource);
                    }else{
                        db.updateResource(stringResource);
                    }
                }

                if (isCancelled()) return valueOf(1);
            }
            return  valueOf(0);
        }

        protected void onProgressUpdate(Integer ... progress)
        {

        }

        protected void onPostExecute(Long result)
        {

        }
    }
}
