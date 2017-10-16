package core.framework.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class ActionLogContextTest {
    @Test
    void withoutCurrentActionLog() {
        assertNull(ActionLogContext.id());

        ActionLogContext.put("key", "value");
        assertFalse(ActionLogContext.get("key").isPresent());

        ActionLogContext.stat("stat", 1);
    }
}
