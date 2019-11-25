package com.dezen.riccardo.networkmanager;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.dezen.riccardo.networkmanager.database_dictionary.PeerDatabase;
import com.dezen.riccardo.networkmanager.database_dictionary.PeerEntity;
import com.dezen.riccardo.networkmanager.database_dictionary.ResourceDatabase;
import com.dezen.riccardo.networkmanager.database_dictionary.ResourceEntity;
import com.dezen.riccardo.smshandler.Peer;
import com.dezen.riccardo.smshandler.SMSPeer;

import java.util.HashMap;
import java.util.Map;

import static com.dezen.riccardo.smshandler.SMSHandler.UNREAD_SMS_DATABASE_NAME;
import static java.lang.Long.valueOf;

/**
 * Class implementing Dictionary. Conceived as a double dictionary on SMSPeer and StringResource,
 * also allowing Peer ownership of Resources.
 * Due to trouble with testing android class. Lists have been used.
 * @author Riccardo De Zen, Giorgia Bortoletti
 */
public class NetworkDictionary implements Dictionary<SMSPeer, StringResource> {
    private Map<String, String>  peers;
    private Map<String, String> resources;
    private NetworkDictionaryDatabase database;
    private ImportFromDatabasesTask importFromDatabasesTask;
    private ExportToDatabaseTask exportToDatabaseTask;

    /**
     * Constructor of NetworkDictionary
     * @param context application context
     */
    public NetworkDictionary(Context context){
        database = new NetworkDictionaryDatabase(context);
        peers = new HashMap<>();
        resources = new HashMap<>();
        importFromDatabase();
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
    public void exportToDatabase(){
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
     * Class to interact with Peer Database e Resource Database
     * @author Giorgia Bortoletti
     */
    private class NetworkDictionaryDatabase{


        private ResourceDatabase resourceDatabase;
        private PeerDatabase peerDatabase;

        /**
         * Constructor of NetworkDictionaryDatabase
         * @param context application context
         */
        private NetworkDictionaryDatabase(Context context) {
            this.resourceDatabase = Room.databaseBuilder(context, ResourceDatabase.class, UNREAD_SMS_DATABASE_NAME)
                    .enableMultiInstanceInvalidation()
                    .build();
            this.peerDatabase = Room.databaseBuilder(context, PeerDatabase.class, UNREAD_SMS_DATABASE_NAME)
                    .enableMultiInstanceInvalidation()
                    .build();
        }

        /**
         * Adds a new Peer. Null Peer will not be inserted.
         * @param newPeer the new Peer, whose key does not already exist
         * @return true if peer has been added, false otherwise.
         */
        private boolean addPeer(SMSPeer newPeer){
            peerDatabase.access().add(new PeerEntity(newPeer.getAddress()));
            return containsPeer(newPeer);
        }

        /**
         * Removes the Peer with the matching key, if it exists. Null Peer will not be removed.
         * @param peerToRemove Peer to remove
         * @return true if peer doesn't exist, false otherwise.
         */
        private boolean removePeer(SMSPeer peerToRemove) {
            peerDatabase.access().remove(new PeerEntity(peerToRemove.getAddress()));
            return !containsPeer(peerToRemove);
        }

        /**
         * Updates the value of a Peer. Null or invalid Resource won't be updated.
         * @param updatedPeer the new value for the Peer
         * @return true if peer has been updated, false otherwise.
         */
        private boolean updatePeer(SMSPeer updatedPeer) {
            peerDatabase.access().update(new PeerEntity(updatedPeer.getAddress()));
            return containsPeer(updatedPeer);
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
         * @return true if resource has been added, false otherwise.
         */
        private boolean addResource(StringResource newResource) {
            resourceDatabase.access().add(new ResourceEntity(newResource.getName(), newResource.getValue()));
            return containsResource(newResource);
        }

        /**
         * Removes the Resource with the matching key, if it exists.
         * Null or invalid Resource won't be searched for.
         * @param resourceToRemove the Resource to remove
         * @return true if resource doesn't exist, false otherwise.
         */
        private boolean removeResource(StringResource resourceToRemove) {
            resourceDatabase.access().remove(new ResourceEntity(resourceToRemove.getName(), resourceToRemove.getValue()));
            return !containsResource(resourceToRemove);
        }

        /**
         * Updates the value of a Resource. Null or invalid Resource won't be updated.
         * @param updatedResource the new value for the Resource
         * @return return !true if resource has been updated, false otherwise.
         */
        private boolean updateResource(StringResource updatedResource) {
            resourceDatabase.access().update(new ResourceEntity(updatedResource.getName(), updatedResource.getName()));
            return !containsResource(updatedResource);
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
                SMSPeer[] smsPeers = db.getPeers();
                StringResource[] stringResources = db.getResources();

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
