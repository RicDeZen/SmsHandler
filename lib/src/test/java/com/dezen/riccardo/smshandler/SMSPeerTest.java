package com.dezen.riccardo.smshandler;

import android.os.Build;

import com.dezen.riccardo.smshandler.exceptions.InvalidAddressException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Giorgia Bortoletti
 * @author Riccardo De Zen
 */
@Config(sdk = Build.VERSION_CODES.P)
@RunWith(RobolectricTestRunner.class)
public class SMSPeerTest {

    private SMSPeer peer;
    private static final String EX_VALID_ADDRESS = "+39892424";

    @Test
    public void isAddressValidNull(){
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_NOT_PHONE_NUMBER,
                SMSPeer.isAddressValid(null)
        );
    }

    @Test
    public void isAddressValidEmptyOrBlank(){
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_NOT_PHONE_NUMBER,
                SMSPeer.isAddressValid("")
        );
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_NOT_PHONE_NUMBER,
                SMSPeer.isAddressValid("     ")
        );
    }

    @Test
    public void isAddressValidNaN(){
        String notANumber = "Wassup";
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_NOT_PHONE_NUMBER,
                SMSPeer.isAddressValid(notANumber)
        );
    }

    @Test
    public void isAddressValidNoCountryCode(){
        String noCC = "911";
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_NO_COUNTRY_CODE,
                SMSPeer.isAddressValid(noCC)
        );
    }

    @Test
    public void isAddressValidTooLong(){
        String longAddress = "+11111111111111111111111111";
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_TOO_LONG,
                SMSPeer.isAddressValid(longAddress)
        );
    }

    @Test
    public void isAddressValidTooShort(){
        String shortAddress = "+10";
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_TOO_SHORT,
                SMSPeer.isAddressValid(shortAddress)
        );
    }

    @Test
    public void isAddressValidIsValid(){
        assertEquals(
                SMSPeer.PhoneNumberValidity.ADDRESS_VALID,
                SMSPeer.isAddressValid(EX_VALID_ADDRESS)
        );
    }

    @Test(expected = InvalidAddressException.class)
    public void constructorFails(){
        String invalidAddress = "+10p";
        peer = new SMSPeer(invalidAddress);
    }

    @Test
    public void constructorPasses(){
        peer = new SMSPeer(EX_VALID_ADDRESS);
        assertNotNull(peer);
    }

    @Test
    public void getAddress(){
        peer = new SMSPeer(EX_VALID_ADDRESS);
        assertEquals(EX_VALID_ADDRESS, peer.getAddress());
    }

    @Test
    public void equalsSame(){
        peer = new SMSPeer(EX_VALID_ADDRESS);
        SMSPeer other = new SMSPeer(EX_VALID_ADDRESS);
        assertEquals(peer, peer);
        assertEquals(peer, other);
    }

    @Test
    public void equalsDifferent(){
        peer = new SMSPeer(EX_VALID_ADDRESS);
        SMSPeer other = new SMSPeer(EX_VALID_ADDRESS+"0");
        assertNotEquals(peer, other);
    }

    @Test
    public void invalidSMSPeerIsValidNegative(){
        assertFalse(SMSPeer.INVALID_SMS_PEER.isValid());
    }

}
