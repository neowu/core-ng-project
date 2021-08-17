package core.framework.test.redis;

import core.framework.redis.RedisHyperLogLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
class MockRedisHyperLogLogTest {
    private RedisHyperLogLog log;

    @BeforeEach
    void createMockRedisHyperLogLog() {
        log = new MockRedis().hyperLogLog();
    }

    @Test
    void add() {
        assertThat(log.add("log1", "value1")).isTrue();
        assertThat(log.add("log1", "value1")).isFalse();

        assertThat(log.add("log1", "value1", "value2")).isTrue();
        assertThat(log.add("log1", "value1", "value2")).isFalse();
    }

    @Test
    void count() {
        assertThat(log.count("key1")).isEqualTo(0);

        log.add("key1", "value1");
        assertThat(log.count("key1")).isEqualTo(1);

        log.add("key1", "value1", "value2");
        assertThat(log.count("key1")).isEqualTo(2);
        assertThat(log.count("key1", "key2")).isEqualTo(2);

        log.add("key2", "value1");
        assertThat(log.count("key1", "key2")).isEqualTo(2);

        log.add("key2", "value3");
        assertThat(log.count("key1", "key2")).isEqualTo(3);
    }
}
