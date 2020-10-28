package core.framework.test.redis;

import core.framework.redis.RedisSortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
public class MockRedisSortedSetTest {
    private MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void pushAlways() {
        assertThat(redis.sortedSet().push("test", 1000, "100", true)).isTrue();
        assertThat(redis.sortedSet().push("test", 2000, "100", false)).isTrue();
    }

    @Test
    void pushOnlyIfAbsent() {
        assertThat(redis.sortedSet().push("test", 1000, "100", true)).isTrue();
        assertThat(redis.sortedSet().push("test", 2000, "100", true)).isFalse();
    }

    @Test
    void popByScore() {
        RedisSortedSet redisSortedSet = redis.sortedSet();
        redisSortedSet.push("test", 1000, "100", true);
        redisSortedSet.push("test", 2000, "200", true);
        redisSortedSet.push("test", 3000, "200", true);

        assertThat(redisSortedSet.popByScoreCap("test", 999)).isNull();
        assertThat(redisSortedSet.popByScoreCap("test", 1000)).isEqualTo("100");
        assertThat(redisSortedSet.popByScoreCap("test", 3000)).isEqualTo("200");
    }
}
