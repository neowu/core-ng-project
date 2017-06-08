package core.framework.impl.log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author neo
 */
public class ActionLogTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void contextValueIsTooLong() {
        exception.expectMessage("context value is too long");

        ActionLog log = new ActionLog("begin");
        log.context("key", longString(1001));
    }

    @Test
    public void duplicateContextKey() {
        exception.expectMessage("duplicate context key");

        ActionLog log = new ActionLog("begin");
        log.context("key", "value1");
        log.context("key", "value2");
    }

    private String longString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append("x");
        }
        return builder.toString();
    }
}