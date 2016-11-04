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
        redis.set("key1", "value");

        String value = redis.get("key1");
        assertEquals("value", value);
    }

    @Test
    public void multiGet() {
        redis.set("key2", "value2");
        redis.set("key3", "value3");
        Map<String, String> values = redis.multiGet("key1", "key3", "key2");

        assertEquals("value2", values.get("key2"));
        assertEquals("value3", values.get("key3"));
        assertNull(values.get("key1"));
    }
}