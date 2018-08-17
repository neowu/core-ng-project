package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MockRedisSetTest {
    private MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void isMember() {
        redis.set().add("key6", "value1");

        assertThat(redis.set().isMember("key6", "value1")).isTrue();
        assertThat(redis.set().isMember("key6", "value2")).isFalse();
    }

    @Test
    void members() {
        redis.set().add("key1", "value1");
        redis.set().add("key1", "value2");

        assertThat(redis.set().members("key1")).containsOnly("value1", "value2");
    }

    @Test
    void remove() {
        redis.set().add("key7", "value1", "value2");

        assertThat(redis.set().remove("key7", "value1")).isEqualTo(1);
        assertThat(redis.set().members("key7")).containsOnly("value2");
    }
}
