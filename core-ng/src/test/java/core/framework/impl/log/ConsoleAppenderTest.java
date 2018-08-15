package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ConsoleAppenderTest {
    private ConsoleAppender appender;

    @BeforeEach
    void createConsoleAppender() {
        appender = new ConsoleAppender();
    }

    @Test
    void filterLineSeparator() {
        String message = "line1\nline2";
        String filteredMessage = appender.filterLineSeparator(message);
        assertEquals("line1 line2", filteredMessage);
    }

    @Test
    void message() {
        ActionLog action = new ActionLog("begin");
        action.action("action");
        action.end("end");

        String message = appender.message(action);
        assertThat(message).contains("| OK |").contains("| action=action |");
    }
}
