package core.framework.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionLogContextTest {
    @Test
    void withoutCurrentActionLog() {
        assertThat(ActionLogContext.id()).isNull();

        ActionLogContext.put("key", "value");
        assertThat(ActionLogContext.get("key")).isNotPresent();

        ActionLogContext.stat("stat", 1);
    }
}
