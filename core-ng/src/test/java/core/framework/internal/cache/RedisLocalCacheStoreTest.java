package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.redis.RedisImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static core.framework.internal.cache.RedisLocalCacheStore.CHANNEL_INVALIDATE_CACHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class RedisLocalCacheStoreTest {
    private RedisLocalCacheStore cacheStore;
    private RedisImpl redis;
    private CacheStore localCacheStore;
    private CacheStore redisCacheStore;

    @BeforeEach
    void createRedisLocalCacheStore() {
        localCacheStore = mock(CacheStore.class);
        redis = mock(RedisImpl.class);
        redisCacheStore = mock(CacheStore.class);

        cacheStore = new RedisLocalCacheStore(localCacheStore, redisCacheStore, redis, new JSONMapper<>(InvalidateLocalCacheMessage.class));
    }

    @Test
    void getWithLocalHit() {
        byte[] value = new byte[0];
        when(localCacheStore.get("key")).thenReturn(value);

        assertThat(cacheStore.get("key")).isSameAs(value);
    }

    @Test
    void getWithRemoteHit() {
        byte[] value = new byte[0];
        when(localCacheStore.get("key")).thenReturn(null);
        when(redisCacheStore.get("key")).thenReturn(value);
        when(redis.expirationTime("key")).thenReturn(new long[]{1000});

        assertThat(cacheStore.get("key")).isSameAs(value);
        verify(localCacheStore).put(eq("key"), eq(value), any());
    }

    @Test
    void getWithRemoteHitButExpired() {
        when(localCacheStore.get("key")).thenReturn(null);
        when(redisCacheStore.get("key")).thenReturn(new byte[0]);
        when(redis.expirationTime("key")).thenReturn(new long[]{0});

        assertThat(cacheStore.get("key")).isNull();
        verify(localCacheStore, never()).put(any(), any(), any());
    }

    @Test
    void getWithMiss() {
        when(localCacheStore.get("key")).thenReturn(null);
        when(redisCacheStore.get("key")).thenReturn(null);

        assertThat(cacheStore.get("key")).isNull();
    }

    @Test
    void getAllWithLocalHit() {
        when(localCacheStore.get("key1")).thenReturn(new byte[0]);
        when(localCacheStore.get("key2")).thenReturn(new byte[0]);

        assertThat(cacheStore.getAll("key1", "key2")).containsKeys("key1", "key2");
    }

    @Test
    void getAllWithRemoteHit() {
        when(localCacheStore.get("key1")).thenReturn(new byte[0]);
        when(localCacheStore.get("key2")).thenReturn(null);
        when(redisCacheStore.getAll("key2")).thenReturn(Map.of("key2", new byte[0]));
        when(redis.expirationTime("key2")).thenReturn(new long[]{1000});

        assertThat(cacheStore.getAll("key1", "key2")).containsKeys("key1", "key2");
        verify(localCacheStore).put(eq("key2"), any(), any());
    }

    @Test
    void getAllWithRemoteHitButExpired() {
        when(localCacheStore.get("key1")).thenReturn(new byte[0]);
        when(localCacheStore.get("key2")).thenReturn(null);
        when(redisCacheStore.getAll("key2")).thenReturn(Map.of("key2", new byte[0]));
        when(redis.expirationTime("key2")).thenReturn(new long[]{0});

        assertThat(cacheStore.getAll("key1")).containsKeys("key1");
        verify(localCacheStore, never()).put(eq("key2"), any(), any());
    }

    @Test
    void getAllWithRemoteMiss() {
        when(localCacheStore.get("key1")).thenReturn(new byte[0]);
        when(localCacheStore.get("key2")).thenReturn(null);
        when(redisCacheStore.getAll("key2")).thenReturn(Map.of());

        assertThat(cacheStore.getAll("key1")).containsKeys("key1");
        verify(localCacheStore, never()).put(eq("key2"), any(), any());
    }

    @Test
    void put() {
        cacheStore.put("key", new byte[0], Duration.ofHours(1));

        verify(localCacheStore).put("key", new byte[0], Duration.ofHours(1));
        verify(redisCacheStore).put("key", new byte[0], Duration.ofHours(1));
        verify(redis).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }

    @Test
    void putAll() {
        Map<String, byte[]> values = Map.of("key", new byte[0]);
        Duration expiration = Duration.ofHours(1);
        cacheStore.putAll(values, expiration);

        verify(localCacheStore).putAll(values, expiration);
        verify(redisCacheStore).putAll(values, expiration);
        verify(redis).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }

    @Test
    void deleteWithRemoteExpired() {
        when(redisCacheStore.delete("key1")).thenReturn(false);

        cacheStore.delete("key1");

        verify(localCacheStore).delete("key1");
        verify(redis, never()).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }

    @Test
    void delete() {
        when(redisCacheStore.delete("key1")).thenReturn(true);

        cacheStore.delete("key1");

        verify(localCacheStore).delete("key1");
        verify(redis).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }
}
