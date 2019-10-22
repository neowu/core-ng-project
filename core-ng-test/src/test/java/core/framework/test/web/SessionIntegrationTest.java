package core.framework.test.web;

import core.framework.impl.web.session.RedisSessionStore;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import core.framework.test.redis.MockRedis;
import core.framework.web.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SessionIntegrationTest extends IntegrationTest {
    @Inject
    SessionContext sessionContext;
    private RedisSessionStore store;

    @BeforeEach
    void createRedisSessionStore() {
        store = new RedisSessionStore(new MockRedis());
    }

    @Test
    void invalidate() {
        store.save("session1", Map.of("key", "v1"), Set.of("key"), Duration.ofMinutes(30));
        store.save("session2", Map.of("key", "v1"), Set.of("key"), Duration.ofMinutes(30));
        store.save("session3", Map.of("key", "v2"), Set.of("key"), Duration.ofMinutes(30));

        store.invalidate("key", "v1");

        assertThat(store.getAndRefresh("session1", Duration.ofMinutes(30))).isNull();
        assertThat(store.getAndRefresh("session2", Duration.ofMinutes(30))).isNull();
        assertThat(store.getAndRefresh("session3", Duration.ofMinutes(30))).containsEntry("key", "v2");
    }

    @Test
    void sessionContext() {  // check session context is registered
        assertThat(sessionContext).isNotNull();
    }
}
