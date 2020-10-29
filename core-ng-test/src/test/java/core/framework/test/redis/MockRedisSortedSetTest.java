package core.framework.test.redis;

import core.framework.redis.RedisSortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
    void add() {
        assertThat(redis.sortedSet().zadd("test", "1000", 1000, true)).isTrue();
        assertThat(redis.sortedSet().zadd("test", "1000", 2000, false)).isTrue();
    }

    @Test
    void addOnlyIfAbsent() {
        assertThat(redis.sortedSet().zadd("test", "1000", 1000, true)).isTrue();
        assertThat(redis.sortedSet().zadd("test", "1000", 2000, true)).isFalse();
    }

    @Test
    void popByScore() {
        RedisSortedSet redisSortedSet = redis.sortedSet();
        redisSortedSet.zadd("test", "1000", 1000, true);
        redisSortedSet.zadd("test", "2000", 2000, true);
        redisSortedSet.zadd("test", "3000", 3000, true);
        redisSortedSet.zadd("test", "4000", 4000, true);

        assertThat(redisSortedSet.zpopByScore("test", 0, 999, 1)).isEmpty();
        assertThat(redisSortedSet.zpopByScore("test", 0, 1000, 1)).containsExactly(Map.entry("1000", 1000L));
        assertThat(redisSortedSet.zpopByScore("test", 0, 4000, 2)).containsExactly(Map.entry("2000", 2000L), Map.entry("3000", 3000L));
    }

    @Test
    void zrange() {
        RedisSortedSet redisSortedSet = redis.sortedSet();
        redisSortedSet.zadd("test", "1000", 1000, true);
        redisSortedSet.zadd("test", "2000", 2000, true);
        redisSortedSet.zadd("test", "3000", 3000, true);

        assertThat(redisSortedSet.zrange("test", 0, -1)).containsExactly(Map.entry("1000", 1000L), Map.entry("2000", 2000L), Map.entry("3000", 3000L));
        assertThat(redisSortedSet.zrange("test", 2, -1)).containsExactly(Map.entry("3000", 3000L));
        assertThat(redisSortedSet.zrange("test", 1, 2)).containsExactly(Map.entry("2000", 2000L), Map.entry("3000", 3000L));
        assertThat(redisSortedSet.zrange("test", -1, 5)).containsExactly(Map.entry("1000", 1000L), Map.entry("2000", 2000L), Map.entry("3000", 3000L));
        assertThat(redisSortedSet.zrange("test", 9, 10)).isEmpty();
    }
}
