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
        var exception = new ForbiddenException("test", "CUSTOM_FORBIDDEN_ERROR_CODE", new Error());
        assertThat(exception.errorCode()).isEqualTo("CUSTOM_FORBIDDEN_ERROR_CODE");
        assertThat(exception.severity()).isEqualTo(Severity.WARN);

        assertThat(new ForbiddenException("test", "FORBIDDEN").errorCode()).isEqualTo("FORBIDDEN");
    }
}
