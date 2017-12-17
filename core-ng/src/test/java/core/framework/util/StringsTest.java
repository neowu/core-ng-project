package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class StringsTest {
    @Test
    void stringEquals() {
        assertTrue(Strings.equals(null, null));
        assertFalse(Strings.equals(null, ""));
        assertFalse(Strings.equals("", null));
        assertTrue(Strings.equals("", ""));
    }

    @Test
    void compare() {
        assertEquals(-1, Strings.compare(null, ""));
        assertEquals(1, Strings.compare("b", "a"));
        assertEquals(0, Strings.compare(null, null));
    }

    @Test
    void truncate() {
        assertNull(Strings.truncate(null, 10));
        assertEquals("value", Strings.truncate("value", 10));
        assertEquals("1234567890", Strings.truncate("123456789012345", 10));
    }

    @Test
    void isEmpty() {
        assertTrue(Strings.isEmpty(""));
        assertTrue(Strings.isEmpty(" "));

        assertFalse(Strings.isEmpty("1"));
        assertFalse(Strings.isEmpty(" 1"));
    }

    @Test
    void split() {
        assertArrayEquals(new String[]{""}, Strings.split("", '/'));
        assertArrayEquals(new String[]{"", ""}, Strings.split("/", '/'));
        assertArrayEquals(new String[]{"", "", ""}, Strings.split("//", '/'));
        assertArrayEquals(new String[]{"", "1"}, Strings.split("/1", '/'));
        assertArrayEquals(new String[]{"", "1", ""}, Strings.split("/1/", '/'));
        assertArrayEquals(new String[]{"1", ""}, Strings.split("1/", '/'));
        assertArrayEquals(new String[]{"1", "2"}, Strings.split("1/2", '/'));
        assertArrayEquals(new String[]{"1", "2", "", "3"}, Strings.split("1/2//3", '/'));
        assertArrayEquals(new String[]{"1", "2", "3"}, Strings.split("1/2/3", '/'));
    }
}
