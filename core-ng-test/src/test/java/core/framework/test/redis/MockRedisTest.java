package core.framework.test.redis;

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
        redis.set("key", "value");

        String value = redis.get("key");
        assertEquals("value", value);
    }

    @Test
    public void mget() {
        redis.set("key1", "value1");
        redis.set("key2", "value2");

        Map<String, String> values = redis.mget("key1", "key3", "key2");
        assertEquals("value1", values.get("key1"));
        assertEquals("value2", values.get("key2"));
        assertNull(values.get("key3"));
    }
}