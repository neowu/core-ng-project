package core.framework.impl.log;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LogManagerTest {
    LogManager logManager;

    @BeforeEach
    void createLogManager() {
        logManager = new LogManager();
    }

    @Test
    void errorCode() {
        assertThat(logManager.errorCode(new TestException())).isEqualTo("TEST_ERROR");
        assertThat(logManager.errorCode(new Exception())).isEqualTo("java.lang.Exception");
    }

    @Test
    void appName() {
        assertThat(LogManager.appName(Map.of("APP_NAME", "test"))).isEqualTo("test");
        assertThat(LogManager.appName(Map.of())).isEqualTo("local");
    }

    private static class TestException extends Exception implements ErrorCode {
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
