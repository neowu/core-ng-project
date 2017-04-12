package core.framework.impl.web.rate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class LRUMapTest {
    @Test
    public void removeEldestEntry() {
        LRUMap<String, String> map = new LRUMap<>(3);
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        assertEquals(3, map.size());

        map.put("4", "4");
        assertEquals(3, map.size());
        assertFalse(map.containsKey("1"));
        assertTrue(map.containsKey("2"));
        assertTrue(map.containsKey("4"));

        map.put("5", "5");
        assertEquals(3, map.size());
        assertFalse(map.containsKey("2"));
        assertTrue(map.containsKey("4"));
        assertTrue(map.containsKey("5"));
    }
}