package com.gruppo1.distributednetworkmanager;
import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SMSPeer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Pardeep
 * This tests cover for 98% the class kadAction.
 */
public class KadActionTest {

    // Also test the getter methods.
    @Test
    public void testConstructorWithAllParameters(){
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=11;
        int expectedpart=12;
        int expectedmaxpart=23;
        int inviteCode=2;
        int peerAddressCode=2;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertEquals(expectedId,kadAction.getOperationId());
        assertEquals(expectedpart,kadAction.getCurrentPart());
        assertEquals(expectedmaxpart,kadAction.getTotalParts());
        assertEquals(expectedpayload,kadAction.getPayload());
        assertEquals(KadAction.ActionType.getTypeFromVal(inviteCode),kadAction.getActionType());
        assertEquals(KadAction.PayloadType.getTypeFromCode(peerAddressCode),kadAction.getPayloadType());
        assertEquals(smsPeer,kadAction.getPeer());

    }

    // Also test the getter methods.
    @Test
    public void testConstructorWithSMSMessage(){
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int length=4;
        int expectedId=2;
        int expectedpart=1;
        int expectedmaxpart=22;
        int inviteCode=2;
        int peerAddressCode=2;
        String expectedpayload="+393456298741";
        KadAction kadAction2=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        String data=inviteCode+KadAction.addPadding(expectedId,length)+KadAction.addPadding(expectedpart,length)
                +KadAction.addPadding(expectedmaxpart,length)+peerAddressCode+expectedpayload;
        assertEquals(data,kadAction2.toMessage().getData());
        SMSMessage smsMessage=new SMSMessage(smsPeer,data);
        KadAction kadAction=new KadAction(smsMessage);
        assertEquals(data,kadAction.toMessage().getData());
        assertEquals(expectedId,kadAction.getOperationId());
        assertEquals(expectedpart,kadAction.getCurrentPart());
        assertEquals(expectedmaxpart,kadAction.getTotalParts());
        assertEquals(expectedpayload,kadAction.getPayload());
        assertEquals(KadAction.ActionType.getTypeFromVal(inviteCode),kadAction.getActionType());
        assertEquals(KadAction.PayloadType.getTypeFromCode(peerAddressCode),kadAction.getPayloadType());
        assertEquals(smsPeer,kadAction.getPeer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorErrorAction(){
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int length=4;
        int expectedId=2;
        int expectedpart=1;
        int expectedmaxpart=22;
        int peerAddressCode=2;
        String actionCode="A";
        String expectedpayload="+393456298741";
        String data=actionCode+KadAction.addPadding(expectedId,length)+KadAction.addPadding(expectedpart,length)
                +KadAction.addPadding(expectedmaxpart,length)+peerAddressCode+expectedpayload;
        SMSMessage smsMessage=new SMSMessage(smsPeer,data);
        KadAction kadAction=new KadAction(smsMessage);
        assertEquals(data,kadAction.toMessage().getData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorErrorPayload(){
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int length=4;
        int expectedId=2;
        int expectedpart=1;
        int expectedmaxpart=22;
        String peerAddres="A";
        int actionCode=1;
        String expectedpayload="+393456298741";
        String data=actionCode+KadAction.addPadding(expectedId,length)+KadAction.addPadding(expectedpart,length)
                +KadAction.addPadding(expectedmaxpart,length)+peerAddres+expectedpayload;
        SMSMessage smsMessage=new SMSMessage(smsPeer,data);
        KadAction kadAction=new KadAction(smsMessage);
        assertEquals(data,kadAction.toMessage().getData());
    }

    @Test
    public void testPayloadMachIgnored(){
        String payload="";
        assertTrue(KadAction.payloadMatchesType(KadAction.PayloadType.IGNORED,payload));

    }
    @Test
    public void testPayloadMachBoolean(){
        String payload="true";
        assertTrue(KadAction.payloadMatchesType(KadAction.PayloadType.BOOLEAN,payload));

    }
    @Test
    public void testPayloadMachPeerAddress(){
        String payload="+393888542978";
        assertTrue(KadAction.payloadMatchesType(KadAction.PayloadType.PEER_ADDRESS,payload));

    }
    @Test
    public void testPayloadMachNodeId(){
        String payload="99999999999999998765432123456789";
        assertTrue(KadAction.payloadMatchesType(KadAction.PayloadType.NODE_ID,payload));
    }
    @Test
    public void testPayloadMachDefault(){
        String payload="payloadValue";
        assertFalse(KadAction.payloadMatchesType(KadAction.PayloadType.INVALID,payload));
    }

    @Test
    public void testIsRequestResponse(){
        int invalidCode=241;
        assertFalse(KadAction.ActionType.INVALID.isRequest());
        assertTrue(KadAction.ActionType.PING.isRequest());
        assertTrue(KadAction.ActionType.PING_ANSWER.isResponse());
        assertTrue(KadAction.ActionType.INVITE.isRequest());
        assertTrue(KadAction.ActionType.INVITE_ANSWER.isResponse());
        assertTrue(KadAction.ActionType.STORE.isRequest());
        assertTrue(KadAction.ActionType.STORE_ANSWER.isResponse());
        assertTrue(KadAction.ActionType.FIND_NODE.isRequest());
        assertTrue(KadAction.ActionType.FIND_NODE_ANSWER.isResponse());
        assertTrue(KadAction.ActionType.FIND_VALUE.isRequest());
        assertTrue(KadAction.ActionType.FIND_VALUE_ANSWER.isResponse());

        assertFalse(KadAction.ActionType.INVALID.isResponse());
        assertFalse(KadAction.ActionType.PING.isResponse());
        assertFalse(KadAction.ActionType.PING_ANSWER.isRequest());
        assertFalse(KadAction.ActionType.INVITE.isResponse());
        assertFalse(KadAction.ActionType.INVITE_ANSWER.isRequest());
        assertFalse(KadAction.ActionType.STORE.isResponse());
        assertFalse(KadAction.ActionType.STORE_ANSWER.isRequest());
        assertFalse(KadAction.ActionType.FIND_NODE.isResponse());
        assertFalse(KadAction.ActionType.FIND_NODE_ANSWER.isRequest());
        assertFalse(KadAction.ActionType.FIND_VALUE.isResponse());
        assertFalse(KadAction.ActionType.FIND_VALUE_ANSWER.isRequest());

        assertEquals(KadAction.PayloadType.INVALID,KadAction.PayloadType.getTypeFromCode(invalidCode));
        assertEquals(KadAction.ActionType.INVALID, KadAction.ActionType.getTypeFromVal(invalidCode));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidActionTypeIsInvalid()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1;
        int expectedpart=1;
        int expectedmaxpart=2;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVALID,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        kadAction.isValid();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidPayloadTypeIsInvalid()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1;
        int expectedpart=1;
        int expectedmaxpart=2;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.INVALID,expectedpayload);
        kadAction.isValid();
    }

    @Test(expected = IllegalArgumentException.class)
    public void  testPayloadMatchTypeResourceError() {
        String expectedaddress = "+393888145659";
        SMSPeer smsPeer = new SMSPeer(expectedaddress);
        int expectedId = 1;
        int expectedpart = 1;
        int expectedmaxpart = 1;
        String expectedpayload = "+non\nso\ncosa\nscrivere";
        KadAction kadAction = new KadAction(smsPeer, KadAction.ActionType.INVITE, expectedId, expectedpart, expectedmaxpart, KadAction.PayloadType.RESOURCE, expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidCurrentPartMinPart()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1;
        int expectedCurrentPart=0;
        int expectedmaxpart=1;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedCurrentPart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidCurrentPartTotalParts()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1;
        int expectedCurrentpart=2;
        int expectedmaxpart=1;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedCurrentpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidTotalPartsMaxPart()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1;
        int expectedpart=5;
        int expectedmaxpart=1000;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidOperationIdMinId()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedOperationId=0;
        int expectedpart=5;
        int expectedmaxpart=2;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedOperationId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidOperationIdMaxId()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1000;
        int expectedpart=1;
        int expectedmaxpart=1;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidExpectedPartExpectedMaxPart()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int expectedId=1;
        int expectedpart=1;
        int expectedmaxpart=0;
        String expectedpayload="+393456298741";
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.FIND_NODE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        assertFalse(kadAction.isValid());
    }

    @Test
    public void  testToMessage()
    {
        String expectedaddress="+393888145659";
        SMSPeer smsPeer=new SMSPeer(expectedaddress);
        int wantedLength=4;
        int expectedId=1;
        int expectedpart=1;
        int expectedmaxpart=2;
        String expectedpayload="+393456298741";
        String expectedMassage= KadAction.ActionType.INVITE.getCode()+KadAction.addPadding(expectedId,wantedLength)+KadAction.addPadding(expectedpart,wantedLength)+
                KadAction.addPadding(expectedmaxpart,wantedLength)+ KadAction.PayloadType.PEER_ADDRESS.getCode() +expectedpayload;
        KadAction kadAction=new KadAction(smsPeer, KadAction.ActionType.INVITE,expectedId,expectedpart,expectedmaxpart, KadAction.PayloadType.PEER_ADDRESS,expectedpayload);
        SMSMessage smsMessage=kadAction.toMessage();
        assertEquals(expectedMassage,smsMessage.getData());
    }

    @Test
    public void  testAddPadding()
    {
        int wantedLength=4;
        int id=5;
        String expectedId="0005";
        assertEquals(expectedId,KadAction.addPadding(id,wantedLength));
    }

    @Test
    public void  testRemovePadding()
    {
        String strinToRemovePadding="0007";
        String string1="00004250";
        int expected1=4250;
        int expectedString=7;
        assertEquals(expectedString,KadAction.removePadding(strinToRemovePadding));
        assertEquals(expected1,KadAction.removePadding(string1));
    }
    @Test(expected = IllegalArgumentException.class)
    public void  testRemovePaddingError()
    {
        String strinToRemovePadding="0a007";
        String string1="00004250";
        int expected1=4250;
        int expectedString=7;
        assertEquals(expectedString,KadAction.removePadding(strinToRemovePadding));
        assertEquals(expected1,KadAction.removePadding(string1));
    }

    @Test
    public void testInvalidKadAction(){
        assertFalse(KadAction.INVALID_KAD_ACTION.isValid());
    }

}
