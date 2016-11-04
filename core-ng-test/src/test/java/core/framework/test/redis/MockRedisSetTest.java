package core.framework.test.redis;

import core.framework.api.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class MockRedisSetTest {
    MockRedis redis;

    @Before
    public void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    public void isMember() {
        redis.set().add("key6", "value1");

        assertTrue(redis.set().isMember("key6", "value1"));
        assertFalse(redis.set().isMember("key6", "value2"));
    }

    @Test
    public void members() {
        redis.set().add("key1", "value1");
        redis.set().add("key1", "value2");

        Set<String> values = redis.set().members("key1");
        assertEquals(Sets.newHashSet("value1", "value2"), values);
    }
}