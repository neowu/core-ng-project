package core.framework.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class StringsTest {
    @Test
    public void nullStringsEqual() {
        assertEquals(true, Strings.equals(null, null));
    }

    @Test
    public void nullStringNotEqualsToEmpty() {
        assertEquals(false, Strings.equals(null, ""));
        assertEquals(false, Strings.equals("", null));
    }

    @Test
    public void emptyStringsEqual() {
        assertEquals(true, Strings.equals("", ""));
    }

    @Test
    public void nullStringIsLessThanEmptyString() {
        assertEquals(-1, Strings.compare(null, ""));
    }

    @Test
    public void nullStringEqualsToNull() {
        assertEquals(0, Strings.compare(null, null));
    }

    @Test
    public void compareRegularStrings() {
        assertEquals(1, Strings.compare("b", "a"));
    }

    @Test
    public void truncateNull() {
        String value = Strings.truncate(null, 10);

        assertNull(value);
    }

    @Test
    public void truncateTextShorterThanMaxLength() {
        String value = Strings.truncate("value", 10);

        assertEquals("value", value);
    }

    @Test
    public void truncateTextLongerThanMaxLength() {
        String value = Strings.truncate("123456789012345", 10);

        assertEquals("1234567890", value);
    }

    @Test
    public void empty() {
        assertTrue(Strings.empty(""));
        assertTrue(Strings.empty(" "));
    }

    @Test
    public void notEmpty() {
        assertFalse(Strings.empty("1"));
        assertFalse(Strings.empty(" 1"));
    }
}