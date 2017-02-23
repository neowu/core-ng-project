package core.framework.test.redis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class KeyMatcherTest {
    @Test
    public void matches() {
        KeyMatcher matcher = new KeyMatcher("*");
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("aa"));
        assertTrue(matcher.matches("abc"));

        matcher = new KeyMatcher("");
        assertTrue(matcher.matches(""));

        matcher = new KeyMatcher("a*");
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("aa"));
        assertTrue(matcher.matches("abc"));

        matcher = new KeyMatcher("a*c");
        assertFalse(matcher.matches("c"));
        assertTrue(matcher.matches("abc"));
        assertTrue(matcher.matches("abbc"));
        assertTrue(matcher.matches("aacc"));

        matcher = new KeyMatcher("a?c");
        assertFalse(matcher.matches("ac"));
        assertTrue(matcher.matches("abc"));
        assertTrue(matcher.matches("acc"));
        assertFalse(matcher.matches("abcc"));
    }
}