package core.framework.impl.web.session;

import core.framework.redis.Redis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class RedisSessionStoreTest {
    private RedisSessionStore store;

    @BeforeEach
    void createRedisSessionStore() {
        store = new RedisSessionStore(mock(Redis.class));
    }

    @Test
    void sessionKey() {
        assertThat(store.sessionKey("someSessionId"))
                .doesNotContain("someSessionId")
                .startsWith("session:");
    }
}
