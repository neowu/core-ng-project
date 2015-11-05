package core.framework.impl.log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class ActionLoggerTest {
    private ActionLogger actionLogger;

    @Before
    public void createActionLogger() {
        actionLogger = ActionLogger.console();
    }

    @Test
    public void filterLineSeparator() {
        String message = "line1\nline2";
        String filteredMessage = actionLogger.filterLineSeparator(message);
        Assert.assertEquals("line1 line2", filteredMessage);
    }
}