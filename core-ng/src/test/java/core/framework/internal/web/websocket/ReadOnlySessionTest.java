package core.framework.internal.web.websocket;

import core.framework.internal.web.session.SessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ReadOnlySessionTest {
    private ReadOnlySession session;

    @BeforeEach
    void createReadOnlySession() {
        session = new ReadOnlySession(new SessionImpl("localhost"));
    }

    @Test
    void get() {
        assertThat(session.get("key")).isEmpty();
    }

    @Test
    void invalidate() {
        assertThatThrownBy(() -> session.invalidate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("readonly");
    }

    @Test
    void set() {
        assertThatThrownBy(() -> session.set("key", "value"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("readonly");
    }
}
