package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ActionLoggerTest {
    private ActionLogger actionLogger;

    @BeforeEach
    void createActionLogger() {
        actionLogger = ActionLogger.console();
    }

    @Test
    void filterLineSeparator() {
        String message = "line1\nline2";
        String filteredMessage = actionLogger.filterLineSeparator(message);
        assertEquals("line1 line2", filteredMessage);
    }
}
