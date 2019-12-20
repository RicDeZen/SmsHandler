package com.dezen.riccardo.networkmanager;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dezen.riccardo.smshandler.SMSPeer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class NetworkDictionaryTest {

    //Reusable fields for Peer management tests

    private String address = "+390425111000";
    private SMSPeer peer = new SMSPeer(address);

    //Reusable fields for Resource management tests

    private String name = "example";
    private String value = "example";
    private StringResource resource = new StringResource(name, value);

    private NetworkDictionary networkVocabulary;
    private NetworkDictionary sampleDictionary;

    @Before
    public void createContext() {
        Context context = ApplicationProvider.getApplicationContext();
        networkVocabulary = new NetworkDictionary(context);
        sampleDictionary = new NetworkDictionary(context);
    }

    @Test
    public void addPeerAddsValidPeer() {
        SMSPeer newPeer = new SMSPeer("+390425000000");
        int previousLenght = networkVocabulary.getPeers().length;
        assertTrue(networkVocabulary.addPeer(newPeer));
        assertEquals(++previousLenght, networkVocabulary.getPeers().length);
    }

    @Test
    public void addPeerIgnoresNullPeer() {
        SMSPeer nullPeer = null;
        assertFalse(networkVocabulary.addPeer(nullPeer));
        assertEquals(0, networkVocabulary.getPeers().length);
    }

    @Test
    public void addPeerIgnoresExistingPeer() {
        networkVocabulary.addPeer(peer);
        assertFalse(networkVocabulary.addPeer(peer));
        assertEquals(1, networkVocabulary.getPeers().length);
    }

    @Test
    public void removePeerIgnoresNullPeer() {
        SMSPeer nullPeer = null;
        assertFalse(networkVocabulary.removePeer(nullPeer));
    }

    @Test
    public void removePeerRemovesExistingPeer() {
        SMSPeer newPeer = new SMSPeer("+390425000001");
        int previousLenght = networkVocabulary.getPeers().length;
        assertTrue(networkVocabulary.addPeer(newPeer));
        assertTrue(networkVocabulary.removePeer(newPeer));
        assertEquals(previousLenght, networkVocabulary.getPeers().length);
    }

    @Test
    public void removePeerIgnoresNonExistingPeer() {
        String existingAddress = "+390425000011";
        SMSPeer existingPeer = new SMSPeer(existingAddress);
        networkVocabulary.addPeer(existingPeer);
        int previousLenght = networkVocabulary.getPeers().length;
        String nonExistingAddress = "+390425000111";
        SMSPeer nonExistingPeer = new SMSPeer(nonExistingAddress);
        assertFalse(networkVocabulary.removePeer(nonExistingPeer));
        assertEquals(previousLenght, networkVocabulary.getPeers().length);
    }

    @Test
    public void updatePeerIgnoresNullPeer() {
        SMSPeer peer = null;
        assertFalse(networkVocabulary.updatePeer(peer));
    }

    @Test
    public void updatePeerUpdatesExistingPeer() {
        String address = "+390425000003";
        SMSPeer examplePeer = new SMSPeer(address);
        networkVocabulary.addPeer(examplePeer);
        assertTrue(networkVocabulary.updatePeer(examplePeer));
        //assertEquals(peer,networkVocabulary.getPeers()[0]);
    }

    @Test
    public void updatePeerIgnoresNonExistingPeer() {
        String existingAddress = "+390425000004"; //This one is added
        SMSPeer existingPeer = new SMSPeer(existingAddress);
        networkVocabulary.addPeer(existingPeer);
        String nonExistingAddress = "+390425000005"; //This one is not added
        SMSPeer nonExistingPeer = new SMSPeer(nonExistingAddress);
        assertFalse(networkVocabulary.updatePeer(nonExistingPeer));
        assertTrue(networkVocabulary.updatePeer(existingPeer));
    }

    @Test
    public void getPeersReturnsCopy() {
        String address = "+390425000006";
        SMSPeer peer = new SMSPeer(address);
        networkVocabulary.addPeer(peer);
        Assert.assertNotEquals(0, networkVocabulary.getPeers().length);
    }

    @Test
    public void addResourceAddsValidResource() {
        assertTrue(networkVocabulary.addResource(resource));
        assertEquals(1, networkVocabulary.getResources().length);
    }

    @Test
    public void addResourceIgnoresNullResource() {
        StringResource nullResource = null;
        assertFalse(networkVocabulary.addResource(nullResource));
        assertEquals(0, networkVocabulary.getResources().length);
    }

    @Test
    public void addResourceIgnoresExistingResource() {
        networkVocabulary.addResource(resource);
        assertFalse(networkVocabulary.addResource(resource));
        assertEquals(1, networkVocabulary.getResources().length);
    }

    @Test
    public void removeResourceIgnoreNullResource() {
        StringResource nullResource = null;
        assertFalse(networkVocabulary.removeResource(nullResource));
    }

    @Test
    public void removeResourceRemovesExistingResource() {
        assertTrue(networkVocabulary.addResource(resource));
        assertTrue(networkVocabulary.removeResource(resource));
        assertEquals(0, networkVocabulary.getResources().length);
    }

    @Test
    public void removeResourceIgnoresNonExistingResource() {
        String existingName = "I exist";
        String existingValue = "I'm the existing value";
        StringResource existing_resource = new StringResource(existingName, existingValue);
        networkVocabulary.addResource(existing_resource);
        String nonExistingName = "I don't";
        String nonExistingValue = "I'm the non-existing value";
        StringResource nonExistingResource = new StringResource(nonExistingName, nonExistingValue);
        assertFalse(networkVocabulary.removeResource(nonExistingResource));
        assertEquals(1, networkVocabulary.getResources().length);
    }

    @Test
    public void updateResourceIgnoresNullResource() {
        StringResource nullResource = null;
        assertFalse(networkVocabulary.updateResource(nullResource));
    }

    @Test
    public void updateResourceUpdatesExistingResource() {
        networkVocabulary.addResource(resource);
        assertTrue(networkVocabulary.updateResource(resource));
        assertEquals(resource, networkVocabulary.getResources()[0]);
    }

    @Test
    public void updateResourceIgnoresNonExistingResource() {
        String existingName = "I exist";
        String existingValue = "I'm the existing value";
        StringResource existingResource = new StringResource(existingName, existingValue);
        networkVocabulary.addResource(existingResource);
        String nonExistingName = "I don't";
        String nonExistingValue = "I'm the non-existing value";
        StringResource nonExistingResource = new StringResource(nonExistingName, nonExistingValue);
        assertFalse(networkVocabulary.updateResource(nonExistingResource));
        assertTrue(networkVocabulary.updateResource(existingResource));
    }

    @Test
    public void getResourcesReturnsCopy() {
        networkVocabulary.addResource(resource);
        StringResource[] resources = networkVocabulary.getResources();
        Assert.assertNotEquals(0, networkVocabulary.getResources().length);
    }

    @Test
    public void containsPeerPositive() {
        networkVocabulary.addPeer(peer);
        assertTrue(networkVocabulary.contains(peer));
    }

    @Test
    public void containsPeerNegative() {
        networkVocabulary.addPeer(peer);
        String nonExistingAddress = "+390426000023";
        SMSPeer nonExistingPeer = new SMSPeer(nonExistingAddress);
        assertFalse(networkVocabulary.contains(nonExistingPeer));
    }

    @Test
    public void containsPeerIgnoresNull() {
        networkVocabulary.addPeer(peer);
        SMSPeer nullPeer = null;
        assertFalse(networkVocabulary.contains(nullPeer));
    }

    @Test
    public void containsResourcePositive() {
        networkVocabulary.addResource(resource);
        assertTrue(networkVocabulary.contains(resource));
    }

    @Test
    public void containsResourceNegative() {
        networkVocabulary.addResource(resource);
        String nonExistingName = "I don't exist";
        String nonExistingValue = "I'm the non-existing value";
        StringResource nonExistingResource = new StringResource(nonExistingName, nonExistingValue);
        assertFalse(networkVocabulary.contains(nonExistingResource));
    }

    @Test
    public void containsResourceIgnoresNull() {
        networkVocabulary.addResource(resource);
        StringResource nullResource = null;
        assertFalse(networkVocabulary.contains(nullResource));
    }

    @Test
    public void containsResourceIgnoresInvalid() {
        networkVocabulary.addResource(resource);
        StringResource invalidResource = new StringResource("", "");
        assertFalse(invalidResource.isValid());
        assertFalse(networkVocabulary.contains(invalidResource));
    }

    @Test
    public void getResourceByNamePositive() {
        StringResource validResource = new StringResource("aValidKey", "resValue");
        networkVocabulary.addResource(validResource);
        StringResource foundResource = networkVocabulary.getResourceByName(validResource.getName());
        assertEquals(foundResource, validResource);
    }

    @Test
    public void getResourceByNameNegative() {
        StringResource validResource = new StringResource("anotherValidKey", "resValue");
        networkVocabulary.addResource(validResource);
        StringResource foundResource = networkVocabulary.getResourceByName("totallyRandom");
        assertEquals(foundResource, NetworkDictionary.INVALID_RESOURCE);
    }

    @Test
    public void getPeerByAddressPositive() {
        SMSPeer validPeer = new SMSPeer("+390425000010");
        networkVocabulary.addPeer(validPeer);
        SMSPeer foundPeer = networkVocabulary.getPeerByAddress(validPeer.getAddress());
        assertEquals(validPeer, foundPeer);
    }

    @Test
    public void getPeerByAddressNegative() {
        SMSPeer validPeer = new SMSPeer("+390425000020");
        networkVocabulary.addPeer(validPeer);
        SMSPeer foundPeer = networkVocabulary.getPeerByAddress("+390425666666");
        assertEquals(foundPeer, NetworkDictionary.INVALID_PEER);
    }

    @Test
    public void importFromDatabaseCanCancel() {
        networkVocabulary.addResource(resource);
        //TODO: mocking
        /*
        networkVocabulary.importFromDatabase();
        networkVocabulary.cancelImportFromDatabase();
        assertTrue(networkVocabulary.importFromDatabaseIsCanceled());
        */
    }

    @Test
    public void sizePeerPositiveTest() {
        assertEquals(sampleDictionary.countPeers(), 0);
        int count = 100;
        for (int i = 0; i < count; i++)
            sampleDictionary.addPeer(new SMSPeer("+390425" + i));
        assertEquals(sampleDictionary.countPeers(), count);
    }


    @Test
    public void sizeResourcePositiveTest() {
        assertEquals(sampleDictionary.countResources(), 0);
        int count = 100;
        for (int i = 0; i < count; i++)
            sampleDictionary.addResource(new StringResource("Resource n:" + i, "value: " + i));
        assertEquals(sampleDictionary.countResources(), count);
    }
}
