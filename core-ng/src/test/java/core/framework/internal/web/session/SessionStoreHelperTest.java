package core.framework.internal.web.session;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SessionStoreHelperTest {
    @Test
    void timeout() {
        assertThat(SessionStoreHelper.timeout(Map.of(), Duration.ofSeconds(30))).hasSeconds(30);

        assertThat(SessionStoreHelper.timeout(Map.of(SessionImpl.TIMEOUT_FIELD, "100"), Duration.ofSeconds(30))).hasSeconds(100);

        assertThat(SessionStoreHelper.timeout(Map.of(SessionImpl.TIMEOUT_FIELD, "invalid"), Duration.ofSeconds(30))).hasSeconds(30);
    }
}
