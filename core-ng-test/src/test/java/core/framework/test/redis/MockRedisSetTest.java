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

        assertThat(redis.set().remove("not-existed-key", "value")).isEqualTo(0);
    }

    @Test
    void pop() {
        redis.set().add("key8", "value1", "value2");
        assertThat(redis.set().pop("key8", 1)).hasSize(1).containsAnyOf("value1", "value2");
        assertThat(redis.set().pop("key8", 1)).hasSize(1).containsAnyOf("value1", "value2");
        assertThat(redis.set().pop("key8", 1)).isEmpty();

        assertThat(redis.set().pop("key9", 0)).isEmpty();
        assertThat(redis.set().pop("key9", 2)).isEmpty();
    }

    @Test
    void size() {
        assertThat(redis.set().size("key10")).isEqualTo(0);

        redis.set().add("key10", "value1", "value2");
        assertThat(redis.set().size("key10")).isEqualTo(2);

        redis.set().pop("key10", 1);
        assertThat(redis.set().size("key10")).isEqualTo(1);
    }
}
