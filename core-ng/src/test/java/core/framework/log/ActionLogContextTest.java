package core.framework.log;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class ActionLogContextTest {
    @Test
    public void withoutCurrentActionLog() {
        assertNull(ActionLogContext.id());

        ActionLogContext.put("key", "value");
        assertFalse(ActionLogContext.get("key").isPresent());

        ActionLogContext.stat("stat", 1);
    }
}
