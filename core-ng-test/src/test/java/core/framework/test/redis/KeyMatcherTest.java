package core.framework.test.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class KeyMatcherTest {
    @Test
    void matches() {
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
