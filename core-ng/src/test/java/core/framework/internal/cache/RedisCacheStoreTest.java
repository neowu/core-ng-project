package core.framework.internal.cache;

import core.framework.internal.redis.RedisImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
}
