package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class MockRedisTest {
    private MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void get() {
        redis.set("key1", "value");
        assertThat(redis.get("key1")).isEqualTo("value");
    }

    @Test
    void multiGet() {
        redis.set("key2", "value2");
        redis.set("key3", "value3");
        Map<String, String> values = redis.multiGet("key1", "key3", "key2");

        assertThat(values).containsOnly(entry("key2", "value2"), entry("key3", "value3"));
    }

    @Test
    void forEach() {
        redis.set("matched-1", "matched-value-1");
        redis.set("matched-2", "matched-value-2");
        redis.set("matched-3", "matched-value-3");
        redis.set("not-matched-1", "not-matched-value-1");
        redis.set("not-matched-2", "not-matched-value-2");

        var count = new AtomicInteger(0);
        redis.forEach("matched-*", key -> {
            String value = redis.get(key);
            assertThat(value).startsWith("matched-value-");
            count.incrementAndGet();
        });

        assertThat(count.get()).isEqualTo(3);
    }

    @Test
    void increaseBy() {
        long result = redis.increaseBy("counter", 1);
        assertThat(result).isEqualTo(1);
        assertThat(redis.get("counter")).isEqualTo("1");

        result = redis.increaseBy("counter", 5);
        assertThat(result).isEqualTo(6);
        assertThat(redis.get("counter")).isEqualTo("6");
    }

    @Test
    void set() {
        redis.set("key4", "value4", Duration.ZERO);
        assertThat(redis.get("key4")).isNull();
    }

    @Test
    void setIfAbsent() {
        redis.set("key8", "value8");
        assertThat(redis.set("key8", "value9", Duration.ofMinutes(5), true)).isFalse();
        assertThat(redis.get("key8")).isEqualTo("value8");

        assertThat(redis.set("key9", "value9", null, true)).isTrue();
        assertThat(redis.get("key9")).isEqualTo("value9");
    }

    @Test
    void expire() {
        redis.set("key5", "value5");
        redis.expire("key5", Duration.ZERO);
        assertThat(redis.get("key4")).isNull();
    }

    @Test
    void del() {
        redis.set("key6", "value6");

        assertThat(redis.del("key6")).isEqualTo(1);
        assertThat(redis.get("key6")).isNull();
    }

    @Test
    void multiSet() {
        redis.multiSet(Map.of("key7", "value7", "key8", "value8"));

        assertThat(redis.get("key7")).isEqualTo("value7");
        assertThat(redis.get("key8")).isEqualTo("value8");
    }
}
