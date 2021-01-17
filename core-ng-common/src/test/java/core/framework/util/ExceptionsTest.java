package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ExceptionsTest {
    @Test
    void stackTrace() {
        String trace = Exceptions.stackTrace(new Error("test-error"));
        assertThat(trace).contains("java.lang.Error: test-error");
    }
}
