package core.framework.test.redis;

import core.framework.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class MockRedisSetTest {
    MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void isMember() {
        redis.set().add("key6", "value1");

        assertTrue(redis.set().isMember("key6", "value1"));
        assertFalse(redis.set().isMember("key6", "value2"));
    }

    @Test
    void members() {
        redis.set().add("key1", "value1");
        redis.set().add("key1", "value2");

        Set<String> values = redis.set().members("key1");
        assertEquals(Sets.newHashSet("value1", "value2"), values);
    }
}
