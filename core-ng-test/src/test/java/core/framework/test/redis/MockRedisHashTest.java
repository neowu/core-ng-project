package core.framework.test.redis;

import core.framework.util.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class MockRedisHashTest {
    MockRedis redis;

    @Before
    public void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    public void set() {
        redis.hash().set("key4", "field1", "value1");
        assertEquals("value1", redis.hash().get("key4", "field1"));

        redis.hash().set("key4", "field2", "value2");
        assertEquals("value1", redis.hash().get("key4", "field1"));
        assertEquals("value2", redis.hash().get("key4", "field2"));
    }

    @Test
    public void multiSet() {
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
    public void del() {
        redis.hash().set("key1", "field1", "value1");
        redis.hash().set("key1", "field2", "value2");
        redis.hash().del("key1", "field1");

        Map<String, String> hash = redis.hash().getAll("key1");
        assertEquals(1, hash.size());
        assertEquals("value2", hash.get("field2"));
    }
}
