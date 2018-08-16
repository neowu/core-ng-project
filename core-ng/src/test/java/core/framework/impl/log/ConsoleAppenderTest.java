package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(appender.filterLineSeparator("line1\nline2")).isEqualTo("line1 line2");
        assertThat(appender.filterLineSeparator("line1\r\nline2")).isEqualTo("line1  line2");
    }

    @Test
    void message() {
        var action = new ActionLog("begin");
        action.action("action");
        action.end("end");

        String message = appender.message(action);
        assertThat(message).contains("| OK |").contains("| action=action |");
    }
}
