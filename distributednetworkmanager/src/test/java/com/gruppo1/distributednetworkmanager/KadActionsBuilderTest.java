package com.gruppo1.distributednetworkmanager;

import com.dezen.riccardo.networkmanager.StringResource;
import com.dezen.riccardo.smshandler.SMSPeer;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class KadActionsBuilderTest {
    private SMSPeer testPeer = new SMSPeer("+390425667000");
    private StringResource testResource = new StringResource("It's me, resource!", "Henlo");
    private KadActionsBuilder testingBuilder;
    private KadAction action;
    private final int maxTestingID = 99;
    private static Random random = new Random();

    @Before
    public void init() {
        testingBuilder = new KadActionsBuilder(maxTestingID);
    }

    @Test
    public void KadActionsBuilder_DefConstructorTest(){
        KadActionsBuilder testBuild = new KadActionsBuilder();
        assertEquals(testBuild.buildPing(KadAction.MAX_ID+1, testPeer), KadAction.INVALID_KAD_ACTION);
        assertEquals(testBuild.buildPing(KadAction.MIN_ID-1, testPeer), KadAction.INVALID_KAD_ACTION);
    }


    private void testValidity(int expectedPart, int expectedmaxPart){
        assertTrue(action.isValid());
        assertEquals(expectedPart, action.getCurrentPart());
        assertEquals(expectedmaxPart, action.getTotalParts());
        assertTrue(action.getOperationId() > 0 && action.getOperationId() <= maxTestingID);
    }

    private void testPeerCoherent(SMSPeer peer){
        assertEquals(peer, action.getPeer());
    }

    @Test
    public void KadActionsBuilder_buildPingTest() {
        action = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1,testPeer);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.PING, action.getActionType());
        assertEquals(KadAction.PayloadType.IGNORED, action.getPayloadType());
    }

    @Test
    public void KadActionsBuilder_buildPingAnswTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildPingAnsw(ping);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.PING_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.IGNORED, action.getPayloadType());
    }

    @Test
    public void KadActionsBuilder_buildPingAnswINCOMPATIBLEREQTest(){
        KadAction invite = testingBuilder.buildInvite(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildPingAnsw(invite);
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }

    @Test
    public void KadActionsBuilder_buildInviteTest() {
        action = testingBuilder.buildInvite(random.nextInt(maxTestingID-1)+1, testPeer);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.INVITE, action.getActionType());
        assertEquals(KadAction.PayloadType.IGNORED, action.getPayloadType());
    }

    @Test
    public void KadActionsBuilder_buildInviteAnswTest(){
        KadAction invite = testingBuilder.buildInvite(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildInviteAnsw(invite, true);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.INVITE_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.BOOLEAN, action.getPayloadType());
        assertEquals(Boolean.toString(true), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildInviteAnswINCOMPATIBLEREQTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildInviteAnsw(ping, false);
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }

    @Test
    public void KadActionsBuilder_buildStoreNodeTest() {
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        action = testingBuilder.buildStore(random.nextInt(maxTestingID-1)+1, testPeer, new PeerNode(key));
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.STORE, action.getActionType());
        assertEquals(KadAction.PayloadType.NODE_ID, action.getPayloadType());
        assertEquals(key.toHex(), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildStoreNodeAnswTest(){
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        KadAction storeNode = testingBuilder.buildStore(random.nextInt(maxTestingID-1)+1, testPeer, new PeerNode(key));
        action = testingBuilder.buildStoreAnsw(storeNode, new SMSPeer[]{testPeer})[0]; //1 smspeer -> 1 action
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.STORE_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.PEER_ADDRESS, action.getPayloadType());
        assertEquals(testPeer.getAddress(), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildStoreNodeAnswINCOMPATIBLEREQTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildStoreAnsw(ping, new SMSPeer[]{testPeer})[0];
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }

    @Test
    public void KadActionsBuilder_buildStoreResourceTest() {
        action = testingBuilder.buildStore(random.nextInt(maxTestingID-1)+1, testPeer, testResource);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.STORE, action.getActionType());
        assertEquals(KadAction.PayloadType.RESOURCE, action.getPayloadType());
        assertEquals(testResource.getName()+KadActionsBuilder.RESOURCE_SEPARATOR+testResource.getValue(), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildStoreResAnswTest(){
        KadAction storeResource = testingBuilder.buildStore(random.nextInt(maxTestingID-1)+1, testPeer, testResource);
        action = testingBuilder.buildStoreAnsw(storeResource, false);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.STORE_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.BOOLEAN, action.getPayloadType());
        assertNotEquals(Boolean.toString(true), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildStoreResAnswINCOMPATIBLEREQTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildStoreAnsw(ping,true);
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }

    @Test
    public void KadActionsBuilder_buildFindNodeTest() {
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        action = testingBuilder.buildFindNode(random.nextInt(maxTestingID-1)+1, testPeer, new PeerNode(key));
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.FIND_NODE, action.getActionType());
        assertEquals(KadAction.PayloadType.NODE_ID, action.getPayloadType());
        assertEquals(key.toHex(), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildFindNodeAnswTest(){
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        KadAction findNode = testingBuilder.buildFindNode(random.nextInt(maxTestingID-1)+1, testPeer, new PeerNode(key));
        action = testingBuilder.buildFindNodeAnsw(findNode, new SMSPeer[]{testPeer})[0];
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.FIND_NODE_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.PEER_ADDRESS, action.getPayloadType());
        assertEquals(testPeer.getAddress(), action.getPayload());
    }


    @Test
    public void KadActionsBuilder_buildFindNodeAnswINCOMPATIBLEREQTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildFindNodeAnsw(ping,new SMSPeer[]{testPeer, testPeer})[0];
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }

    @Test
    public void KadActionsBuilder_buildFindValueTest() {
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        action = testingBuilder.buildFindValue(random.nextInt(maxTestingID-1)+1, testPeer, new ResourceNode(key, "stuff"));
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.FIND_VALUE, action.getActionType());
        assertEquals(KadAction.PayloadType.NODE_ID, action.getPayloadType());
        assertEquals(key.toHex(), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildFindValueResFOUNDAnswTest(){
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        KadAction findResource = testingBuilder.buildFindValue(random.nextInt(maxTestingID-1)+1, testPeer, new ResourceNode(key, "stuff"));
        action = testingBuilder.buildFindValueAnsw(findResource, testResource);
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.FIND_VALUE_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.RESOURCE, action.getPayloadType());
    }

    @Test
    public void KadActionsBuilder_buildFindValueResFOUNDAnswINCOMPATIBLEREQTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildFindValueAnsw(ping, testResource);
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }

    @Test
    public void KadActionsBuilder_buildFindValueResNOTFOUNDAnswTest() {
        BinarySet key = new BinarySet("AAFFFFFFFF0000");
        KadAction findResource = testingBuilder.buildFindValue(random.nextInt(maxTestingID - 1) + 1, testPeer, new ResourceNode(key, "stuff"));
        action = testingBuilder.buildFindValueAnsw(findResource, new SMSPeer[]{testPeer})[0];
        testValidity(1, 1);
        testPeerCoherent(testPeer);
        assertEquals(KadAction.ActionType.FIND_VALUE_ANSWER, action.getActionType());
        assertEquals(KadAction.PayloadType.PEER_ADDRESS, action.getPayloadType());
        assertEquals(testPeer.getAddress(), action.getPayload());
    }

    @Test
    public void KadActionsBuilder_buildFindValueResNOTFOUNDAnswINCOMPATIBLEREQTest(){
        KadAction ping = testingBuilder.buildPing(random.nextInt(maxTestingID-1)+1, testPeer);
        action = testingBuilder.buildFindValueAnsw(ping, new SMSPeer[]{testPeer})[0];
        assertEquals(action, KadActionsBuilder.INVALID_ACTION);
    }
}