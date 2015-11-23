package core.framework.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class ASCIITest {
    @Test
    public void isDigit() {
        assertTrue(ASCII.isDigit('0'));
        assertTrue(ASCII.isDigit('9'));
        assertFalse(ASCII.isDigit('a'));
        assertFalse(ASCII.isDigit('z'));
        assertFalse(ASCII.isDigit('A'));
        assertFalse(ASCII.isDigit('Z'));
    }

    @Test
    public void isLetter() {
        assertFalse(ASCII.isLetter('0'));
        assertFalse(ASCII.isLetter('9'));
        assertTrue(ASCII.isLetter('a'));
        assertTrue(ASCII.isLetter('z'));
        assertTrue(ASCII.isLetter('A'));
        assertTrue(ASCII.isLetter('Z'));
    }

    @Test
    public void upperCase() {
        assertEquals("TEXT", ASCII.toUpperCase("text"));
        assertEquals("TEXT", ASCII.toUpperCase("tExt"));
        assertEquals("TEXT", ASCII.toUpperCase("TEXT"));
        assertEquals("01239_-", ASCII.toUpperCase("01239_-"));
    }

    @Test
    public void lowerCase() {
        assertEquals("text", ASCII.toLowerCase("text"));
        assertEquals("text", ASCII.toLowerCase("tExt"));
        assertEquals("text", ASCII.toLowerCase("TEXT"));
        assertEquals("01239_-", ASCII.toLowerCase("01239_-"));
    }
}