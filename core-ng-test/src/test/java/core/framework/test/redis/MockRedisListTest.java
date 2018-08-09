package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rexthk
 */
class MockRedisListTest {
    private MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void push() {
        redis.list().push("key1", "val1");
        redis.list().push("key1", "val2");

        assertEquals("val1", redis.list().pop("key1"));
        assertThat(redis.list().getAll("key1")).containsOnly("val2");
    }

    @Test
    void getAll() {
        redis.list().push("key1", "val1");
        redis.list().push("key1", "val2");
        redis.list().push("key1", "val3");
        assertThat(redis.list().getAll("key1")).containsOnly("val1", "val2", "val3");
    }
}
