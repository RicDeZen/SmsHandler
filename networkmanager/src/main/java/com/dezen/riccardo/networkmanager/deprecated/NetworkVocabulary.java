package com.dezen.riccardo.networkmanager.deprecated;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dezen.riccardo.networkmanager.Dictionary;
import com.dezen.riccardo.networkmanager.StringResource;
import com.dezen.riccardo.smshandler.SMSPeer;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing Dictionary. Conceived as a double dictionary on SMSPeer and StringResource.
 * Due to trouble with testing android class. Lists have been used.
 * @author Riccardo De Zen.
 */
class NetworkVocabulary implements Dictionary<SMSPeer, StringResource> {
    private List<PeerItem> peers;
    private List<StringResource> resources;

    public NetworkVocabulary(){
        peers = new ArrayList<>();
        resources = new ArrayList<>();
    }

    //Class is deprecated
    /**
     * @return the number of contained peers
     */
    public int countPeers(){
        return 0;
    }

    /**
     * @return the number of contained resources
     */
    public int countResources(){
        return 0;
    }

    /**
     * Class used to contain references to Peers and their associated resources
     */
    private class PeerItem{
        private SMSPeer peer;
        private List<StringResource> ownedResources;
        PeerItem(@NonNull SMSPeer peer){
            this.peer = peer;
            ownedResources = new ArrayList<>();
        }

        /**
         * @return the Peer for this item
         */
        public SMSPeer getPeer(){
            return peer;
        }

        /**
         * Method to update the value of this PeerItems's Peer.
         * Peer key is considered unique, so it must be equal for field "peer" and parameter "newPeer".
         * @param newPeer the new valid value for the Peer
         */
        public void setPeer(SMSPeer newPeer){
            this.peer = newPeer;
        }

        /**
         * Method to add a Resource to the list of Resources owned by the Peer if not owned already
         * @param resource the Resource to add
         * @return true if resource was added, false otherwise
         */
        public boolean addResource(StringResource resource){
            if(!ownsResource(resource)){
                ownedResources.add(resource);
                return true;
            }
            return false;
        }

        /**
         * Method to remove a Resource from the list of owned Resources
         * @param resource the Resource to remove
         * @return true if resource was removed, false otherwise
         */
        public boolean removeResource(StringResource resource){
            if(!ownsResource(resource)) return false;
            ownedResources.remove(resource);
            return true;
        }

        /**
         * Method to check ownership of valid Resource
         * @param resource the Resource to check
         * @return true if "peer" owns the resource, false if not
         */
        public boolean ownsResource(StringResource resource){
            return ownedResources.contains(resource);
        }

        /**
         * Getter for ownedResources
         * @return the array of resources "peer" owns
         */
        public StringResource[] getOwnedResources() {
            StringResource[] ownedResourcesArray = new StringResource[ownedResources.size()];
            ownedResources.toArray(ownedResourcesArray);
            return ownedResourcesArray;
        }

        /**
         * Two PeerItems are equal if their "peer" field is.
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof PeerItem){
                return this.peer.equals(((PeerItem) obj).peer);
            }
            else return false;
        }
    }

    /**
     * Adds a new Peer. Null Peer will not be inserted.
     * @param newPeer the new Peer, whose key does not already exist
     */
    @Override
    public boolean addPeer(SMSPeer newPeer){
        if(newPeer == null || contains(newPeer)) return false;
        peers.add(new PeerItem(newPeer));
        return true;
    }

    /**
     * Removes the Peer with the matching key, if it exists. Null Peer will not be removed.
     * @param peerToRemove Peer to remove
     * @return true if it was removed, false otherwise
     */
    @Override
    public boolean removePeer(SMSPeer peerToRemove) {
        if(peerToRemove == null || !contains(peerToRemove)) return false;
        peers.remove(new PeerItem(peerToRemove));
        return true;
    }

    /**
     * Updates the Peer with the matching key, if it exists.
     * Null Peer will not be updated. Since a matching key is required to update, and being "peerAddress"
     * the key for Peers, this method only updates any extra fields the Peer might have besides
     * "peerAddress" itself.
     * @param updatedPeer the new value for the Peer if one with a matching key exists
     * @return true if the Peer was found and updated, false otherwise
     */
    @Override
    public boolean updatePeer(SMSPeer updatedPeer) {
        if(updatedPeer == null || !contains(updatedPeer)) return false;
        //We find the PeerItem whose key matches "updatedPeer" to change it's "peer" field but not
        //its resources.
        getItemFor(updatedPeer).setPeer(updatedPeer);
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
        for(PeerItem peerItem : peers) peerArray[i++] = peerItem.getPeer();
        return peerArray;
    }

    /**
     * Adds a new resource. Null or invalid Resource won't be added.
     * @param newResource the new Resource, whose key does not already exist
     */
    @Override
    public boolean addResource(StringResource newResource) {
        if(newResource == null || !newResource.isValid() ||contains(newResource)) return false;
        resources.add(newResource);
        return true;
    }

    /**
     * Removes the Resource with the matching key, if it exists.
     * Null or invalid Resource won't be searched for.
     * @param resourceToRemove the Resource to remove
     * @return true if it was removed, false otherwise
     */
    @Override
    public boolean removeResource(StringResource resourceToRemove) {
        if(resourceToRemove == null || !resourceToRemove.isValid() || !contains(resourceToRemove))
            return false;
        resources.remove(resourceToRemove);
        return true;
    }

    /**
     * Updates the value of a Resource. Null or invalid Resource won't be updated.
     * Since a matching key is required to update, and being "name" the key for Resources, this
     * method only updates any extra fields the Resource might have besides "peerAddress" itself.
     * @param updatedResource the new value for the Resource
     * @return true if the resource was found and updated, false otherwise.
     */
    @Override
    public boolean updateResource(StringResource updatedResource) {
        if(updatedResource == null || !updatedResource.isValid() || !contains(updatedResource))
            return false;
        resources.set(resources.indexOf(updatedResource), updatedResource);
        return true;
    }

    /**
     * Returns a copy of the list of all Resources. Resources are not copied singularly.
     * @return a list containing all Resources
     */
    @Override
    public StringResource[] getResources() {
        StringResource[] resourceArray = new StringResource[resources.size()];
        int i = 0;
        for(StringResource resource : resources) resourceArray[i++] = resource;
        return resourceArray;
    }

    /**
     * Returns whether the given user exists in this Vocabulary
     * @param peer the Peer to find
     * @return true if it exists (an instance of PeerItem exists whose "peer" value equals the "peer"
     * passed as parameter), false otherwise.
     */
    public boolean contains(SMSPeer peer){
        if(peer == null) return false;
        PeerItem tempItem = new PeerItem(peer);
        return peers.contains(tempItem);
    }

    /**
     * Method to tell whether the given Resource exists in this Vocabulary
     * @param resource the Resource to find
     * @return true if resource exists, false otherwise.
     */
    public boolean contains(StringResource resource){
        return resources.contains(resource);
    }

    /**
     * Method to get all Resources owned by a specific Peer.
     * @param peer the Peer
     * @return an array containing the resources owned by parameter "peer"
     */
    public StringResource[] getResourcesForPeer(SMSPeer peer){
        StringResource[] peerResources = new StringResource[0];
        if(peer == null || !contains(peer)) return peerResources;
        for(PeerItem peerItem : peers){
            if(peerItem.getPeer().equals(peer)){
                return peerItem.getOwnedResources();
            }
        }
        return peerResources;
    }

    /**
     * Method to get all Peers which own a specific Resource.
     * @param resource the Resource
     * @return an array containing the Peers who own the parameter "resource"
     */
    public SMSPeer[] getResourceOwners(StringResource resource){
        List<SMSPeer> owners = new ArrayList<>();
        if(resource == null || !resource.isValid() ||!contains(resource)) return new SMSPeer[0];
        for(PeerItem peerItem : peers){
            if(peerItem.ownsResource(resource)){
                owners.add(peerItem.getPeer());
            }
        }
        return owners.toArray(new SMSPeer[0]);
    }

    /**
     * Method to grant ownership of a non already owned valid Resource.
     * @param owner the Peer gaining the ownership
     * @param resource the Resource being given
     * @return true if the Resource's ownership was granted, false otherwise
     */
    public boolean addOwnerForResource(SMSPeer owner, StringResource resource){
        if(!contains(owner) || !contains(resource))
            return false;
        return getItemFor(owner).addResource(resource);
    }

    /**
     * Method to revoke ownership of an already owned valid Resource.
     * @param owner the Peer losing the ownership
     * @param resource the Resource whose ownership is being revoked
     * @return true if the Resource's ownership was revoked and false otherwise.
     */
    public boolean removeOwnerForResource(SMSPeer owner, StringResource resource){
        if(owner == null || !contains(owner) || resource == null || !resource.isValid() || !contains(resource))
            return false;
        return getItemFor(owner).removeResource(resource);
    }

    /**
     * @return the PeerItem corresponding to an existing Peer
     */
    private PeerItem getItemFor(SMSPeer peer){
        return peers.get(peers.indexOf(new PeerItem(peer)));
    }
}
