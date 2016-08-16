package core.framework.test.redis;

import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class MockRedisTest {
    MockRedis redis;

    @Before
    public void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    public void get() {
        redis.set("key1", "value");

        String value = redis.get("key1");
        assertEquals("value", value);
    }

    @Test
    public void mget() {
        redis.set("key2", "value2");
        redis.set("key3", "value3");

        Map<String, String> values = redis.multiGet("key1", "key3", "key2");
        assertEquals("value2", values.get("key2"));
        assertEquals("value3", values.get("key3"));
        assertNull(values.get("key1"));
    }

    @Test
    public void hash() {
        redis.hash().set("key4", "field1", "value1");
        assertEquals("value1", redis.hash().get("key4", "field1"));

        redis.hash().set("key4", "field2", "value2");
        assertEquals("value1", redis.hash().get("key4", "field1"));
        assertEquals("value2", redis.hash().get("key4", "field2"));
    }

    @Test
    public void hashMulti() {
        redis.hash().multiSet("key5", Maps.newHashMap("field1", "value1"));
        Map<String, String> hash = redis.hash().getAll("key5");
        assertEquals(1, hash.size());
        assertEquals("value1", hash.get("field1"));

        redis.hash().multiSet("key5", Maps.newHashMap("field2", "value2"));
        hash = redis.hash().getAll("key5");
        assertEquals(2, hash.size());
        assertEquals("value1", hash.get("field1"));
        assertEquals("value2", hash.get("field2"));
    }

    @Test
    public void set() {
        redis.set().add("key6", "value1");
        assertTrue(redis.set().isMember("key6", "value1"));
        assertFalse(redis.set().isMember("key6", "value2"));

        Set<String> values = redis.set().members("key6");
        assertEquals(Sets.newHashSet("value1"), values);
    }
}