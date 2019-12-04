package com.gruppo1.distributednetworkmanager;

import com.dezen.riccardo.smshandler.Peer;
import com.dezen.riccardo.smshandler.SMSPeer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KBucketTest {
    int dim = 20;
    KBucket bucket;
    PeerNode node;
    SMSPeer peer = new SMSPeer("+390425678102");

    @Before
    public void Initialize()
    {
        bucket = new KBucket(dim);
        node = new PeerNode(128, peer);
        bucket.Add(node);
    }

    @Test
    public void KBucket_ContainsPositiveTest()
    {
        assertTrue(bucket.Contains(node));
        PeerNode otherNode = new PeerNode(128, new SMSPeer("+39348456789"));

        assertFalse(bucket.Contains(otherNode));
    }

    @Test
    public void KBucket_AddsPositiveTest(){
        PeerNode a = new PeerNode(128, new SMSPeer("+39348456789"));

        assertTrue(bucket.Add(a));
        assertTrue(bucket.Contains(a));
    }

    @Test
    public void KBucket_RemovesPositiveTest() {
        PeerNode a = new PeerNode(128, new SMSPeer("+39348456789"));

        assertTrue(bucket.Add(a));
        assertTrue(bucket.Contains(a));
        assertTrue(bucket.Remove(a));
        assertFalse(bucket.Contains(a));
    }

    @Test
    public void KBucket_GetsElementsPositiveTest(){
        PeerNode a = new PeerNode(128, new SMSPeer("+39348456789"));
        PeerNode b = new PeerNode(128, new SMSPeer("+39348676789"));
        PeerNode c = new PeerNode(128, new SMSPeer("+39568456789"));

        KBucket newBucket = new KBucket(3);

        newBucket.Add(a);
        newBucket.Add(b);
        newBucket.Add(c);

        PeerNode[] result = newBucket.getElements();

        assertTrue(result[0].equals(a));
        assertTrue(result[1].equals(b));
        assertTrue(result[2].equals(c));
    }

    @Test
    public void KBucket_RemovesKeepsSortedPositiveTest() {
        PeerNode a = new PeerNode(128, new SMSPeer("+39348456789"));
        PeerNode b = new PeerNode(128, new SMSPeer("+39348676789"));
        PeerNode c = new PeerNode(128, new SMSPeer("+39568456789"));

        KBucket newBucket = new KBucket(3);

        newBucket.Add(a);
        newBucket.Add(b);
        newBucket.Add(c);

        newBucket.Remove(b);

        PeerNode[] elements = newBucket.getElements();

        assertEquals(elements[0], a);
        assertEquals(elements[1], c);
    }

    @Test
    public void KBucket_getOldestPositiveTest(){
        PeerNode a = new PeerNode(128, new SMSPeer("+39348456789"));
        PeerNode b = new PeerNode(128, new SMSPeer("+39348676789"));
        PeerNode c = new PeerNode(128, new SMSPeer("+39568456789"));

        KBucket newBucket = new KBucket(3);

        newBucket.Add(a);
        newBucket.Add(b);
        newBucket.Add(c);

        assertEquals(a, newBucket.getOldest());
    }
}