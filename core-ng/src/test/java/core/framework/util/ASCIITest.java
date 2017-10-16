package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ASCIITest {
    @Test
    void isDigit() {
        assertTrue(ASCII.isDigit('0'));
        assertTrue(ASCII.isDigit('9'));
        assertFalse(ASCII.isDigit('a'));
        assertFalse(ASCII.isDigit('z'));
        assertFalse(ASCII.isDigit('A'));
        assertFalse(ASCII.isDigit('Z'));
    }

    @Test
    void isLetter() {
        assertFalse(ASCII.isLetter('0'));
        assertFalse(ASCII.isLetter('9'));
        assertTrue(ASCII.isLetter('a'));
        assertTrue(ASCII.isLetter('z'));
        assertTrue(ASCII.isLetter('A'));
        assertTrue(ASCII.isLetter('Z'));
    }

    @Test
    void toUpperCase() {
        assertEquals("TEXT", ASCII.toUpperCase("text"));
        assertEquals("TEXT", ASCII.toUpperCase("tExt"));
        assertEquals("TEXT", ASCII.toUpperCase("TEXT"));
        assertEquals("01239_-", ASCII.toUpperCase("01239_-"));
    }

    @Test
    void toLowerCase() {
        assertEquals("text", ASCII.toLowerCase("text"));
        assertEquals("text", ASCII.toLowerCase("tExt"));
        assertEquals("text", ASCII.toLowerCase("TEXT"));
        assertEquals("01239_-", ASCII.toLowerCase("01239_-"));
    }

    @Test
    void toUpperCaseChar() {
        assertEquals('-', ASCII.toUpperCase('-'));
        assertEquals('A', ASCII.toUpperCase('a'));
        assertEquals('Z', ASCII.toUpperCase('z'));
        assertEquals('0', ASCII.toUpperCase('0'));
        assertEquals('9', ASCII.toUpperCase('9'));
        assertEquals('A', ASCII.toUpperCase('A'));
    }

    @Test
    void toLowerCaseChar() {
        assertEquals('-', ASCII.toLowerCase('-'));
        assertEquals('a', ASCII.toLowerCase('A'));
        assertEquals('z', ASCII.toLowerCase('Z'));
        assertEquals('0', ASCII.toLowerCase('0'));
        assertEquals('9', ASCII.toLowerCase('9'));
        assertEquals('a', ASCII.toLowerCase('a'));
    }
}
