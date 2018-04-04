package core.framework.impl.log.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LogFilterTest {
    private LogFilter filter;

    @BeforeEach
    void createLogFilter() {
        filter = new LogFilter();
    }

    @Test
    void format() {
        String message = filter.format("message-{}", 1);
        assertThat(message).isEqualTo("message-1");
    }

    @Test
    void truncate() {
        String message = "1234567890";
        String value = filter.truncate(message, 5);
        assertThat(value).isEqualTo("12345...(truncated)");
    }
}
