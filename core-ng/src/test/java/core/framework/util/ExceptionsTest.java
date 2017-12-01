package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class ExceptionsTest {
    @Test
    void error() {
        Error error = Exceptions.error("message");
        assertEquals("message", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void errorWithMessageFormat() {
        Error error = Exceptions.error("message-{}", "1");
        assertEquals("message-1", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void errorWithMessageFormatAndCause() {
        Error error = Exceptions.error("message-{}", "1", new RuntimeException("cause"));
        assertEquals("message-1", error.getMessage());
        assertEquals("cause", error.getCause().getMessage());
    }

    @Test
    void errorWitCause() {
        Error error = Exceptions.error("message", new RuntimeException("cause"));
        assertEquals("message", error.getMessage());
        assertEquals("cause", error.getCause().getMessage());
    }

    @Test
    void stackTrace() {
        String trace = Exceptions.stackTrace(new Error("test-error"));
        assertThat(trace).contains("java.lang.Error: test-error");
    }
}
