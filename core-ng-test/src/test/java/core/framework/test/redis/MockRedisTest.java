package core.framework.test.redis;

import core.framework.api.util.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

        Map<String, String> values = redis.mget("key1", "key3", "key2");
        assertEquals("value2", values.get("key2"));
        assertEquals("value3", values.get("key3"));
        assertNull(values.get("key1"));
    }

    @Test
    public void hget() {
        redis.hmset("key4", Maps.newHashMap("field1", "value1"));
        Map<String, String> hash = redis.hgetAll("key4");
        assertEquals(1, hash.size());
        assertEquals("value1", hash.get("field1"));

        redis.hmset("key4", Maps.newHashMap("field2", "value2"));
        hash = redis.hgetAll("key4");
        assertEquals(2, hash.size());
        assertEquals("value1", hash.get("field1"));
        assertEquals("value2", hash.get("field2"));
    }
}