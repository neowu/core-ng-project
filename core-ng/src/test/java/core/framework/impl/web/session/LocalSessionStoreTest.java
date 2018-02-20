package core.framework.impl.web.session;

import core.framework.util.Maps;
import core.framework.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LocalSessionStoreTest {
    private LocalSessionStore localSessionStore;

    @BeforeEach
    void createLocalSessionStore() {
        localSessionStore = new LocalSessionStore();
    }

    @Test
    void getAndRefresh() {
        localSessionStore.values.put("sessionId", new LocalSessionStore.SessionValue(Instant.now().plus(Duration.ofHours(1)), Maps.newHashMap()));
        Map<String, String> values = localSessionStore.getAndRefresh("sessionId", Duration.ofSeconds(30));

        assertThat(values).isNotNull();
    }

    @Test
    void getAndRefreshWithExpiredSession() {
        localSessionStore.values.put("sessionId", new LocalSessionStore.SessionValue(Instant.now().minus(Duration.ofSeconds(30)), Maps.newHashMap()));
        Map<String, String> values = localSessionStore.getAndRefresh("sessionId", Duration.ofSeconds(30));

        assertThat(values).isNull();
    }

    @Test
    void getAndRefreshWithNotExistedSessionId() {
        Map<String, String> values = localSessionStore.getAndRefresh("sessionId", Duration.ofSeconds(30));

        assertThat(values).isNull();
    }

    @Test
    void save() {
        localSessionStore.save("sessionId", Maps.newHashMap("key", "value"), Sets.newHashSet(), Duration.ofSeconds(30));

        assertThat(localSessionStore.values).hasSize(1);
    }

    @Test
    void invalidate() {
        localSessionStore.values.put("sessionId", new LocalSessionStore.SessionValue(Instant.now().minus(Duration.ofSeconds(30)), Maps.newHashMap()));
        localSessionStore.invalidate("sessionId");

        assertThat(localSessionStore.values).isEmpty();
    }

    @Test
    void cleanup() {
        localSessionStore.values.put("sessionId", new LocalSessionStore.SessionValue(Instant.now().minus(Duration.ofSeconds(30)), Maps.newHashMap()));
        localSessionStore.cleanup();

        assertThat(localSessionStore.values).isEmpty();
    }
}
