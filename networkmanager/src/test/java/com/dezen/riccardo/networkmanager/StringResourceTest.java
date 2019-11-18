package com.dezen.riccardo.networkmanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

////CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen
 */
public class StringResourceTest {

    @Test
    public void getName() {
        String name = "Harry";
        String value = "Potter";
        StringResource stringResource = new StringResource(name, value);
        assertEquals(name, stringResource.getName());
    }

    @Test
    public void getValue() {
        String name = "Severus";
        String value = "Snape";
        StringResource stringResource = new StringResource(name, value);
        assertEquals(value, stringResource.getValue());
    }

    @Test
    public void setName() {
        String originalName = "OriginalName";
        String originalValue = "OriginalValue";
        StringResource stringResource = new StringResource(originalName, originalValue);
        String newName = "newName";
        stringResource.setName(newName);
        assertEquals(newName, stringResource.getName());
    }

    @Test
    public void setValue() {
        String originalName = "OriginalName";
        String originalValue = "OriginalValue";
        StringResource stringResource = new StringResource(originalName, originalValue);
        String newValue = "newValue";
        stringResource.setValue(newValue);
        assertEquals(newValue, stringResource.getValue());
    }

    @Test
    public void isValid_nullName(){
        String name = null;
        String value = "exampleValue";
        StringResource stringResource = new StringResource(name, value);
        assertFalse(stringResource.isValid());
    }

    @Test
    public void isValid_emptyName(){
        String name = "";
        String value = "exampleValue";
        StringResource stringResource = new StringResource(name, value);
        assertFalse(stringResource.isValid());
    }

    @Test
    public void isValid(){
        String name = "exampleName";
        String value = "exampleValue";
        StringResource stringResource = new StringResource(name, value);
        assertTrue(stringResource.isValid());
    }
}