package core.framework.impl.log;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("TEST_ERROR", logManager.errorCode(new TestException()));
        assertEquals("java.lang.Exception", logManager.errorCode(new Exception()));
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
