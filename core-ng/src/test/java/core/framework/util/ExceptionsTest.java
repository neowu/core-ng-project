package core.framework.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class ExceptionsTest {
    @Test
    public void error() {
        Error error = Exceptions.error("message");
        assertEquals("message", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    public void errorWithMessageFormat() {
        Error error = Exceptions.error("message-{}", "1");
        assertEquals("message-1", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    public void errorWithMessageFormatAndCause() {
        Error error = Exceptions.error("message-{}", "1", new RuntimeException("cause"));
        assertEquals("message-1", error.getMessage());
        assertEquals("cause", error.getCause().getMessage());
    }

    @Test
    public void errorWitCause() {
        Error error = Exceptions.error("message", new RuntimeException("cause"));
        assertEquals("message", error.getMessage());
        assertEquals("cause", error.getCause().getMessage());
    }

    @Test
    public void stackTrace() {
        String trace = Exceptions.stackTrace(new Error("test-error"));
        assertThat(trace, containsString("java.lang.Error: test-error"));
    }
}
