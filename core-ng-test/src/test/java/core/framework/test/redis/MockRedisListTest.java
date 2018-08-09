package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(redis.list().pop("key1")).isEqualTo("val1");
        assertThat(redis.list().range("key1")).containsOnly("val2");
        assertThat(redis.list().pop("key1")).isEqualTo("val2");
        assertThat(redis.list().range("key1")).isEmpty();
    }

    @Test
    void range() {
        redis.list().push("key1", "val1", "val2", "val3");
        assertThat(redis.list().range("key1")).containsExactly("val1", "val2", "val3");
    }
}
