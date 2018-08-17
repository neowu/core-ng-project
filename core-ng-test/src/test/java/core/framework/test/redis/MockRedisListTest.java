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
        redis.list().push("key1", "v1");
        redis.list().push("key1", "v2");

        assertThat(redis.list().pop("key1")).isEqualTo("v1");
        assertThat(redis.list().range("key1")).containsOnly("v2");
        assertThat(redis.list().pop("key1")).isEqualTo("v2");
        assertThat(redis.list().range("key1")).isEmpty();
    }

    @Test
    void range() {
        redis.list().push("key2", "v1", "v2", "v3");

        assertThat(redis.list().range("key2")).containsExactly("v1", "v2", "v3");
        assertThat(redis.list().range("key2", 2, -1)).containsExactly("v3");
        assertThat(redis.list().range("key2", 1, 2)).containsExactly("v2", "v3");
        assertThat(redis.list().range("key2", -1, 5)).containsExactly("v1", "v2", "v3");
        assertThat(redis.list().range("key2", 9, 10)).isEmpty();
    }
}
