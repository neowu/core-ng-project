package core.framework.impl.web.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SessionImplTest {
    private SessionImpl session;

    @BeforeEach
    void createSessionImpl() {
        session = new SessionImpl();
    }

    @Test
    void set() {
        session.set("key", "value");
        assertThat(session.changedFields).containsOnly("key");
        assertThat(session.get("key")).get().isEqualTo("value");

        session.set("key", null);
        assertThat(session.changedFields).containsOnly("key");
        assertThat(session.get("key")).isNotPresent();
    }

    @Test
    void setWithoutChange() {
        session.set("key", null);
        assertThat(session.changedFields).isEmpty();

        session.values.put("key", "value");
        session.set("key", "value");
        assertThat(session.changedFields).isEmpty();
    }
}
