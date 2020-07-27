package core.framework.internal.cache;

import core.framework.internal.redis.RedisImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
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

        cacheStore = new RedisLocalCacheStore(localCacheStore, redisCacheStore, redis);
    }

    @Test
    void getWithLocalHit() {
        var value = new TestCache();
        when(localCacheStore.get("key", null, null)).thenReturn(value);

        assertThat(cacheStore.<TestCache>get("key", null, null)).isSameAs(value);
    }

    @Test
    void getWithRemoteHit() {
        var value = new TestCache();
        when(localCacheStore.get("key", null, null)).thenReturn(null);
        when(redisCacheStore.get("key", null, null)).thenReturn(value);
        when(redis.expirationTime("key")).thenReturn(new long[]{1000});

        assertThat(cacheStore.<TestCache>get("key", null, null)).isSameAs(value);
        verify(localCacheStore).put(eq("key"), eq(value), any(), any());
    }

    @Test
    void getWithRemoteHitButExpired() {
        when(localCacheStore.get("key", null, null)).thenReturn(null);
        when(redisCacheStore.get("key", null, null)).thenReturn(new byte[0]);
        when(redis.expirationTime("key")).thenReturn(new long[]{0});

        assertThat(cacheStore.<TestCache>get("key", null, null)).isNull();
        verify(localCacheStore, never()).put(any(), any(), any(), any());
    }

    @Test
    void getWithMiss() {
        when(localCacheStore.get("key", null, null)).thenReturn(null);
        when(redisCacheStore.get("key", null, null)).thenReturn(null);

        assertThat(cacheStore.<TestCache>get("key", null, null)).isNull();
    }

    @Test
    void getAllWithLocalHit() {
        when(localCacheStore.get("key1", null, null)).thenReturn(new TestCache());
        when(localCacheStore.get("key2", null, null)).thenReturn(new TestCache());

        assertThat(cacheStore.getAll(new String[]{"key1", "key2"}, null, null)).containsKeys("key1", "key2");
    }

    @Test
    void getAllWithRemoteHit() {
        when(localCacheStore.get("key1", null, null)).thenReturn(new TestCache());
        when(localCacheStore.get("key2", null, null)).thenReturn(null);
        when(redisCacheStore.getAll(new String[]{"key2"}, null, null)).thenReturn(Map.of("key2", new TestCache()));
        when(redis.expirationTime("key2")).thenReturn(new long[]{1000});

        assertThat(cacheStore.getAll(new String[]{"key1", "key2"}, null, null)).containsKeys("key1", "key2");
        verify(localCacheStore).put(eq("key2"), any(), any(), any());
    }

    @Test
    void getAllWithRemoteHitButExpired() {
        when(localCacheStore.get("key1", null, null)).thenReturn(new TestCache());
        when(localCacheStore.get("key2", null, null)).thenReturn(null);
        when(redisCacheStore.getAll(new String[]{"key2"}, null, null)).thenReturn(Map.of("key2", new TestCache()));
        when(redis.expirationTime("key2")).thenReturn(new long[]{0});

        assertThat(cacheStore.getAll(new String[]{"key1", "key2"}, null, null)).containsKeys("key1");
        verify(localCacheStore, never()).put(eq("key2"), any(), any(), any());
    }

    @Test
    void getAllWithRemoteMiss() {
        when(localCacheStore.get("key1", null, null)).thenReturn(new TestCache());
        when(localCacheStore.get("key2", null, null)).thenReturn(null);
        when(redisCacheStore.getAll(new String[]{"key2"}, null, null)).thenReturn(Map.of());

        assertThat(cacheStore.getAll(new String[]{"key1", "key2"}, null, null)).containsKeys("key1");
        verify(localCacheStore, never()).put(eq("key2"), any(), any(), any());
    }

    @Test
    void put() {
        var value = new TestCache();
        cacheStore.put("key", value, Duration.ofHours(1), null);

        verify(localCacheStore).put("key", value, Duration.ofHours(1), null);
        verify(redisCacheStore).put("key", value, Duration.ofHours(1), null);
        verify(redis).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }

    @Test
    void putAll() {
        List<CacheStore.Entry<TestCache>> values = List.of(new CacheStore.Entry<>("key", new TestCache()));
        Duration expiration = Duration.ofHours(1);
        cacheStore.putAll(values, expiration, null);

        verify(localCacheStore).putAll(values, expiration, null);
        verify(redisCacheStore).putAll(values, expiration, null);
        verify(redis).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }

    @Test
    void deleteWithRemoteExpired() {
        when(redisCacheStore.delete("key1")).thenReturn(Boolean.FALSE);

        cacheStore.delete("key1");

        verify(localCacheStore).delete("key1");
        verify(redis, never()).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }

    @Test
    void delete() {
        when(redisCacheStore.delete("key1")).thenReturn(Boolean.TRUE);

        cacheStore.delete("key1");

        verify(localCacheStore).delete("key1");
        verify(redis).publish(eq(CHANNEL_INVALIDATE_CACHE), any());
    }
}
