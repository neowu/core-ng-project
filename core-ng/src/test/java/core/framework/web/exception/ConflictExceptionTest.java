package core.framework.web.exception;

import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ConflictExceptionTest {
    @Test
    void severity() {
        var exception = new ConflictException("error", "CONFLICT");
        assertThat(exception.severity()).isEqualTo(Severity.WARN);
    }
}
