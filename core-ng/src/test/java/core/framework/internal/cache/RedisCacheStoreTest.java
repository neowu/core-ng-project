package core.framework.internal.cache;

import core.framework.internal.redis.RedisException;
import core.framework.internal.redis.RedisImpl;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class RedisCacheStoreTest {
    private RedisCacheStore cacheStore;
    private RedisImpl redis;

    @BeforeEach
    void createRedisCacheStore() {
        redis = mock(RedisImpl.class);
        cacheStore = new RedisCacheStore(redis);
    }

    @Test
    void delete() {
        when(redis.del("key1", "key2")).thenReturn(2L);
        assertThat(cacheStore.delete("key1", "key2")).isTrue();

        when(redis.del("key3")).thenReturn(0L);
        assertThat(cacheStore.delete("key3")).isFalse();
    }

    @Test
    void get() {
        byte[] value = Strings.bytes("value");
        when(redis.getBytes("key")).thenReturn(value);
        assertThat(cacheStore.get("key")).isEqualTo(value);
    }

    @Test
    void getWithFailure() {
        when(redis.getBytes("key")).thenThrow(new RedisException("unexpected"));
        assertThat(cacheStore.get("key")).isNull();
    }

    @Test
    void put() {
        Duration expiration = Duration.ofHours(1);
        byte[] value = new byte[0];
        cacheStore.put("key", value, expiration);
        verify(redis).set("key", value, expiration, false);
    }

    @Test
    void putWithFailure() {
        Duration expiration = Duration.ofHours(1);
        byte[] value = new byte[0];
        doThrow(new RedisException("unexpected")).when(redis).set("key", value, expiration, false);

        cacheStore.put("key", value, expiration);
    }
}
