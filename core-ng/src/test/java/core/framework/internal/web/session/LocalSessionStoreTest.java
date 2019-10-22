package core.framework.internal.web.session;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

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
        localSessionStore.values.put("sessionId", sessionValue(Instant.now().plus(Duration.ofHours(1)), Map.of()));
        Map<String, String> values = localSessionStore.getAndRefresh("sessionId", Duration.ofSeconds(30));

        assertThat(values).isNotNull();
    }

    @Test
    void getAndRefreshWithExpiredSession() {
        localSessionStore.values.put("sessionId", sessionValue(Instant.now().minus(Duration.ofSeconds(30)), Map.of()));
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
        localSessionStore.save("sessionId", Map.of("key", "value"), Set.of(), Duration.ofSeconds(30));

        assertThat(localSessionStore.values).hasSize(1);
    }

    @Test
    void invalidate() {
        localSessionStore.values.put("sessionId", sessionValue(Instant.now().minus(Duration.ofSeconds(30)), Map.of()));
        localSessionStore.invalidate("sessionId");

        assertThat(localSessionStore.values).isEmpty();
    }

    @Test
    void invalidateByKey() {
        localSessionStore.values.put("session1", sessionValue(Instant.now().minus(Duration.ofSeconds(30)), Map.of("key", "v1")));
        localSessionStore.values.put("session2", sessionValue(Instant.now().minus(Duration.ofSeconds(30)), Map.of("key", "v1")));
        localSessionStore.values.put("session3", sessionValue(Instant.now().minus(Duration.ofSeconds(30)), Map.of("key", "v2")));

        localSessionStore.invalidate("key", "v1");

        assertThat(localSessionStore.values).containsOnlyKeys("session3");
    }

    @Test
    void cleanup() {
        localSessionStore.values.put("sessionId", sessionValue(Instant.now().minus(Duration.ofSeconds(30)), Map.of()));
        localSessionStore.cleanup();

        assertThat(localSessionStore.values).isEmpty();
    }

    @NotNull
    private LocalSessionStore.SessionValue sessionValue(Instant exp, Map<String, String> values) {
        return new LocalSessionStore.SessionValue(exp, values);
    }
}
