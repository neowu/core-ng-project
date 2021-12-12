package core.framework.internal.web.session;

import core.framework.internal.redis.RedisException;
import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class RedisSessionStoreTest {
    @Mock
    Redis redis;
    @Mock
    RedisHash redisHash;
    private RedisSessionStore store;

    @BeforeEach
    void createRedisSessionStore() {
        store = new RedisSessionStore(redis);
    }

    @Test
    void sessionKey() {
        assertThat(store.sessionKey("someSessionId", "domain"))
            .doesNotContain("someSessionId")
            .doesNotContain("domain")
            .startsWith("session:");
    }

    @Test
    void getAndRefreshWithRedisDown() {
        // redis shutdown in the middle
        when(redis.hash()).thenReturn(redisHash);
        when(redisHash.getAll(anyString())).thenThrow(new UncheckedIOException(new IOException("unexpected end of stream")));

        assertThatThrownBy(() -> store.getAndRefresh("sessionId", "localhost", Duration.ofMinutes(30)))
            .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void getAndRefreshWithInvalidRedisData() {
        // session value in redis is invalid
        when(redis.hash()).thenReturn(redisHash);
        when(redisHash.getAll(anyString())).thenThrow(new RedisException("WRONGTYPE Operation against a key holding the wrong kind of value"));
        assertThat(store.getAndRefresh("sessionId", "localhost", Duration.ofMinutes(30))).isNull();
    }

    @Test
    void getAndRefresh() {
        var timeout = Duration.ofSeconds(30);
        var values = Map.of("USER_ID", "1");

        when(redis.hash()).thenReturn(redisHash);
        when(redisHash.getAll(anyString())).thenReturn(values);

        store.getAndRefresh("sessionId", "localhost", timeout);

        verify(redis).expire(anyString(), eq(timeout));
    }

    @Test
    void save() {
        when(redis.hash()).thenReturn(redisHash);
        var timeout = Duration.ofSeconds(30);
        var values = Map.of("USER_ID", "1");
        store.save("sessionId", "localhost", values, values.keySet(), timeout);

        verify(redisHash).multiSet(anyString(), eq(values));
        verify(redis).expire(anyString(), eq(timeout));
    }
}
