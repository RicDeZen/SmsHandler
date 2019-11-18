package com.dezen.riccardo.smshandler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

//CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen
 */
public class SmsUtilsTest {

    private String formattedNumber = "+393928402034";
    private String validNumber = "3928402034";
    private String invalidNumber = "12345";
    private String validCountryCode = "IT";
    private String invalidCountryCode = "it";

    @Test
    public void formatSMSNumber_nullNumber() {
        assertNull(SmsUtils.formatSMSNumber(null, null));
        assertNull(SmsUtils.formatSMSNumber(null,validCountryCode));
    }

    @Test
    public void formatSMSNumber_nullCountryCode() {
        assertEquals(invalidNumber,SmsUtils.formatSMSNumber(invalidNumber, null));
        assertEquals(validNumber,SmsUtils.formatSMSNumber(validNumber,null));
    }

    @Test
    public void formatSMSNumber_invalidCountryCode() {
        assertEquals(invalidNumber,SmsUtils.formatSMSNumber(invalidNumber, invalidCountryCode));
        assertEquals(validNumber, SmsUtils.formatSMSNumber(validNumber,invalidCountryCode));
    }

    @Test
    public void formatSMSNumber_validCountryCode() {
        assertEquals(invalidNumber,SmsUtils.formatSMSNumber(invalidNumber,validCountryCode));
        assertEquals(formattedNumber, SmsUtils.formatSMSNumber(validNumber, validCountryCode));
    }
}