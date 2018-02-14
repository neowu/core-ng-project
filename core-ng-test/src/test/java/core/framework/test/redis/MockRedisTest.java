package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class MockRedisTest {
    MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void get() {
        redis.set("key1", "value");

        String value = redis.get("key1");
        assertEquals("value", value);
    }

    @Test
    void multiGet() {
        redis.set("key2", "value2");
        redis.set("key3", "value3");
        Map<String, String> values = redis.multiGet("key1", "key3", "key2");

        assertEquals("value2", values.get("key2"));
        assertEquals("value3", values.get("key3"));
        assertNull(values.get("key1"));
    }

    @Test
    void forEach() {
        redis.set("matched-1", "matched-value-1");
        redis.set("matched-2", "matched-value-2");
        redis.set("matched-3", "matched-value-3");
        redis.set("not-matched-1", "not-matched-value-1");
        redis.set("not-matched-2", "not-matched-value-2");

        AtomicInteger count = new AtomicInteger(0);
        redis.forEach("matched-*", key -> {
            String value = redis.get(key);
            assertNotNull(value);
            assertTrue(value.startsWith("matched-value-"));
            count.incrementAndGet();
        });

        assertEquals(3, count.get());
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
}
