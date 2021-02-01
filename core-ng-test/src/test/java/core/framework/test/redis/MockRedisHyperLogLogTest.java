package core.framework.test.redis;

import core.framework.redis.RedisHyperLogLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
public class MockRedisHyperLogLogTest {
    private RedisHyperLogLog logLog;

    @BeforeEach
    void setup() {
        logLog = new MockRedis().hyperLogLog();
    }

    @Test
    void add() {
        assertThat(logLog.add("loglog", "value1")).isTrue();
        assertThat(logLog.add("loglog", "value1")).isFalse();

        assertThat(logLog.add("loglog", "value1", "value2")).isTrue();
        assertThat(logLog.add("loglog", "value1", "value2")).isFalse();
    }

    @Test
    void count() {
        assertThat(logLog.count("key1")).isEqualTo(0);

        logLog.add("key1", "value1");
        assertThat(logLog.count("key1")).isEqualTo(1);

        logLog.add("key1", "value1", "value2");
        assertThat(logLog.count("key1")).isEqualTo(2);

        assertThat(logLog.count("key1", "key2")).isEqualTo(2);
        logLog.add("key2", "value1");
        assertThat(logLog.count("key1", "key2")).isEqualTo(3);
    }
}
