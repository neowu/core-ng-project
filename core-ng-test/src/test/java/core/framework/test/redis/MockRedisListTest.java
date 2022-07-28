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
        redis.list().push("key1", "v1", "v2", "v3");
        redis.list().push("key1", "v4");

        assertThat(redis.list().pop("key1")).isEqualTo("v1");
        assertThat(redis.list().range("key1")).containsOnly("v2", "v3", "v4");
        assertThat(redis.list().pop("key1", 3)).containsOnly("v2", "v3", "v4");
        assertThat(redis.list().range("key1")).isEmpty();
    }

    @Test
    void pop() {
        redis.list().push("key1", "v1", "v2", "v3");

        assertThat(redis.list().pop("key1")).isEqualTo("v1");
        assertThat(redis.list().pop("key1", 3)).containsExactly("v2", "v3");
        assertThat(redis.list().pop("key1", 5)).isEmpty();
    }

    @Test
    void range() {
        redis.list().push("key2", "v1", "v2", "v3");

        assertThat(redis.list().range("key2")).containsExactly("v1", "v2", "v3");
        assertThat(redis.list().range("key2", 2, -1)).containsExactly("v3");
        assertThat(redis.list().range("key2", -1, -1)).containsExactly("v3");
        assertThat(redis.list().range("key2", 0, 0)).containsExactly("v1");
        assertThat(redis.list().range("key2", 1, 2)).containsExactly("v2", "v3");
        assertThat(redis.list().range("key2", -1, 5)).containsExactly("v3");
        assertThat(redis.list().range("key2", 9, 10)).isEmpty();
        assertThat(redis.list().range("key2", -2, -1)).containsExactly("v2", "v3");
        assertThat(redis.list().range("key2", -3, 2)).containsExactly("v1", "v2", "v3");
        assertThat(redis.list().range("key2", -100, 100)).containsExactly("v1", "v2", "v3");
        assertThat(redis.list().range("key2", -100, -100)).isEmpty();
        assertThat(redis.list().range("key2", -2, 0)).isEmpty();
    }

    @Test
    void trim() {
        redis.list().push("key3", "v1", "v2", "v3", "v4");
        redis.list().trim("key3", 3);
        assertThat(redis.list().range("key3")).containsExactly("v2", "v3", "v4");

        redis.list().trim("key3", 4);
        assertThat(redis.list().range("key3")).containsExactly("v2", "v3", "v4");

        redis.list().push("key3", "v5");
        redis.list().trim("key3", 2);
        assertThat(redis.list().range("key3")).containsExactly("v4", "v5");
    }
}
