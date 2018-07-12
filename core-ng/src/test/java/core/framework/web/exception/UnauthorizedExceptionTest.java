package core.framework.web.exception;

import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class UnauthorizedExceptionTest {
    @Test
    void severity() {
        var exception = new UnauthorizedException("error");
        assertThat(exception.severity()).isEqualTo(Severity.WARN);
    }

    @Test
    void errorCode() {
        var exception = new UnauthorizedException("error");
        assertThat(exception.errorCode()).isEqualTo("UNAUTHORIZED");
    }
}
