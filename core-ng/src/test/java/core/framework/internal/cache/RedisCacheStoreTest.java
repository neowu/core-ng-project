package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.redis.RedisException;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.validate.Validator;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class RedisCacheStoreTest {
    private final JSONMapper<TestCache> mapper = new JSONMapper<>(TestCache.class);
    private final Validator validator = Validator.of(TestCache.class);
    private RedisCacheStore cacheStore;
    private RedisImpl redis;

    @BeforeEach
    void createRedisCacheStore() {
        redis = mock(RedisImpl.class);
        cacheStore = new RedisCacheStore(redis);
    }

    @Test
    void get() {
        when(redis.getBytes("key")).thenReturn(Strings.bytes("{\"stringField\":\"value\"}"));
        assertThat(cacheStore.get("key", mapper, validator).stringField).isEqualTo("value");
    }

    @Test
    void getWithStaleData() {
        when(redis.getBytes("key")).thenReturn(Strings.bytes("{}"));
        assertThat(cacheStore.get("key", mapper, validator)).isNull();
    }

    @Test
    void getWithInvalidJSON() {
        when(redis.getBytes("key")).thenReturn(Strings.bytes("{\"listField\": 1}"));
        assertThat(cacheStore.get("key", mapper, validator)).isNull();
    }

    @Test
    void getWithFailure() {
        when(redis.getBytes("key")).thenThrow(new RedisException("unexpected"));
        assertThat(cacheStore.get("key", mapper, validator)).isNull();
    }

    @Test
    void getAll() {
        Map<String, byte[]> values = Map.of("key", Strings.bytes("{\"stringField\":\"value\"}"));
        when(redis.multiGetBytes("key")).thenReturn(values);
        Map<String, TestCache> results = cacheStore.getAll(new String[]{"key"}, mapper, validator);
        assertThat(results).hasSize(1);
        assertThat(results.get("key").stringField).isEqualTo("value");
    }

    @Test
    void getAllWithStaleAndInvalidData() {
        Map<String, byte[]> values = Map.of("key1", Strings.bytes("{\"stringField\":\"value\"}"),
                "key2", Strings.bytes("{}"),
                "key3", Strings.bytes("{\"listField\": 1}"));
        when(redis.multiGetBytes("key1", "key2", "key3")).thenReturn(values);
        Map<String, TestCache> results = cacheStore.getAll(new String[]{"key1", "key2", "key3"}, mapper, validator);
        assertThat(results).hasSize(1);
        assertThat(results.get("key1").stringField).isEqualTo("value");
    }

    @Test
    void getAllWithFailure() {
        when(redis.multiGetBytes("key")).thenThrow(new RedisException("unexpected"));
        assertThat(cacheStore.getAll(new String[]{"key"}, mapper, validator)).isEmpty();
    }

    @Test
    void put() {
        Duration expiration = Duration.ofHours(1);
        var value = new TestCache();
        cacheStore.put("key", value, expiration, mapper);
        verify(redis).set("key", mapper.toJSON(value), expiration, false);
    }

    @Test
    void putWithFailure() {
        var value = new TestCache();
        Duration expiration = Duration.ofHours(1);
        doThrow(new RedisException("unexpected")).when(redis).set("key", mapper.toJSON(value), expiration, false);

        cacheStore.put("key", value, expiration, mapper);
    }

    @Test
    void putAll() {
        Duration expiration = Duration.ofHours(1);
        List<CacheStore.Entry<TestCache>> values = List.of(new CacheStore.Entry<>("key", new TestCache()));
        cacheStore.putAll(values, expiration, mapper);
        verify(redis).multiSet(anyMap(), eq(expiration));
    }

    @Test
    void putAllWithFailure() {
        Duration expiration = Duration.ofHours(1);
        doThrow(new RedisException("unexpected")).when(redis).multiSet(anyMap(), eq(expiration));

        cacheStore.putAll(List.of(new CacheStore.Entry<>("key", new TestCache())), expiration, mapper);
    }

    @Test
    void delete() {
        when(redis.del("key1", "key2")).thenReturn(2L);
        assertThat(cacheStore.delete("key1", "key2")).isTrue();

        when(redis.del("key3")).thenReturn(0L);
        assertThat(cacheStore.delete("key3")).isFalse();
    }

    @Test
    void deleteWithFailure() {
        when(redis.del("key")).thenThrow(new RedisException("unexpected"));
        assertThat(cacheStore.delete("key")).isFalse();
    }
}
