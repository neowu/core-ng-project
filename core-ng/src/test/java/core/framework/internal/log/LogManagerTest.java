package core.framework.internal.log;

import core.framework.log.ErrorCode;
import core.framework.log.LogAppender;
import core.framework.log.Severity;
import core.framework.log.message.ActionLogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serial;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class LogManagerTest {
    LogManager logManager;
    @Mock
    LogAppender appender;

    @BeforeEach
    void createLogManager() {
        logManager = new LogManager();
        logManager.appender = appender;
    }

    @Test
    void errorCode() {
        assertThat(logManager.errorCode(new TestException())).isEqualTo("TEST_ERROR");
        assertThat(logManager.errorCode(new Exception())).isEqualTo("java.lang.Exception");
    }

    @Test
    void appName() {
        assertThat(LogManager.appName(Map.of("CORE_APP_NAME", "test"))).isEqualTo("test");

        assertThat(LogManager.appName(Map.of())).isEqualTo("local");

        System.setProperty("core.appName", "test");
        assertThat(LogManager.appName(Map.of())).isEqualTo("test");
        System.clearProperty("core.appName");
    }

    @Test
    void logError() {
        logManager.logError(new TestException());
    }

    @Test
    void endWithAppenderFailure() {
        doThrow(new Error("test")).when(appender).append(any(ActionLogMessage.class));

        logManager.begin("begin", null);
        logManager.end("end");
    }

    private static final class TestException extends Exception implements ErrorCode {
        @Serial
        private static final long serialVersionUID = 4243205974337190882L;

        @Override
        public String errorCode() {
            return "TEST_ERROR";
        }

        @Override
        public Severity severity() {
            return Severity.WARN;
        }
    }
}
