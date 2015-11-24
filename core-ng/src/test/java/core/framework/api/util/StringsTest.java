package core.framework.api.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class StringsTest {
    @Test
    public void equals() {
        assertEquals(true, Strings.equals(null, null));
        assertEquals(false, Strings.equals(null, ""));
        assertEquals(false, Strings.equals("", null));
        assertEquals(true, Strings.equals("", ""));
    }

    @Test
    public void compare() {
        assertEquals(-1, Strings.compare(null, ""));
        assertEquals(1, Strings.compare("b", "a"));
        assertEquals(0, Strings.compare(null, null));
    }

    @Test
    public void truncate() {
        assertNull(Strings.truncate(null, 10));
        assertEquals("value", Strings.truncate("value", 10));
        assertEquals("1234567890", Strings.truncate("123456789012345", 10));
    }

    @Test
    public void isEmpty() {
        assertTrue(Strings.isEmpty(""));
        assertTrue(Strings.isEmpty(" "));

        assertFalse(Strings.isEmpty("1"));
        assertFalse(Strings.isEmpty(" 1"));
    }

    @Test
    public void split() {
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