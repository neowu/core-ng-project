package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class MockRedisHashTest {
    private MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void set() {
        redis.hash().set("key4", "field1", "value1");
        assertThat(redis.hash().get("key4", "field1")).isEqualTo("value1");

        redis.hash().set("key4", "field2", "value2");
        assertThat(redis.hash().get("key4", "field1")).isEqualTo("value1");
        assertThat(redis.hash().get("key4", "field2")).isEqualTo("value2");
    }

    @Test
    void multiSet() {
        redis.hash().multiSet("key5", Map.of("field1", "value1"));
        Map<String, String> hash = redis.hash().getAll("key5");
        assertThat(hash).containsExactly(entry("field1", "value1"));

        redis.hash().multiSet("key5", Map.of("field2", "value2"));
        hash = redis.hash().getAll("key5");
        assertThat(hash).containsEntry("field1", "value1").containsEntry("field2", "value2");
    }

    @Test
    void del() {
        redis.hash().set("key1", "field1", "value1");
        redis.hash().set("key1", "field2", "value2");
        assertThat(redis.hash().del("key1", "field1")).isEqualTo(1);

        Map<String, String> hash = redis.hash().getAll("key1");
        assertThat(hash).containsExactly(entry("field2", "value2"));
    }

    @Test
    void increaseBy() {
        long result = redis.hash().increaseBy("key1", "field1", 1);
        assertThat(result).isEqualTo(1);
        assertThat(redis.hash().get("key1", "field1")).isEqualTo("1");

        result = redis.hash().increaseBy("key1", "field1", 1);
        assertThat(result).isEqualTo(2);
        assertThat(redis.hash().get("key1", "field1")).isEqualTo("2");
    }
}
