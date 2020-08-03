package core.framework.web.exception;

import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ForbiddenExceptionTest {
    @Test
    void errorCode() {
        var exception = new ForbiddenException("test", new Error());
        assertThat(exception.errorCode()).isEqualTo("FORBIDDEN");
        assertThat(exception.severity()).isEqualTo(Severity.WARN);
    }
}
