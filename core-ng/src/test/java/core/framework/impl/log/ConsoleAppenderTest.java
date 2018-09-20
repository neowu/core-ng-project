package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        action.correlationIds = List.of("refId1", "refId2");
        action.context("context", "value");
        action.track("db", 100, 1, 0);
        action.clients = List.of("service");
        action.refIds = List.of("refId3");
        action.stat("stat", 1);
        action.end("end");

        String message = appender.message(action);
        assertThat(message)
                .contains("| OK |")
                .contains("| correlationId=refId1,refId2 |")
                .contains("| action=action |")
                .contains("| context=value |")
                .contains("| client=service |")
                .contains("| refId=refId3 |")
                .contains("| stat=1.0 |")
                .contains("| dbCount=1 | dbReads=1 | dbWrites=0 | dbElapsed=100");

    }
}
