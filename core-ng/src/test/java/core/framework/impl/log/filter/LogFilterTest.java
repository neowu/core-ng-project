package core.framework.impl.log.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
        String message = filter.format("message, null={}, object={}, map={}, object[]={}, int[]={}, boolean[]={}, char[]={}",
                null,
                "string",
                Map.of("key", "value"),
                new Object[]{1, "string", null},
                new int[]{1, 2, 3},
                new boolean[]{true},
                new char[]{'1', '2'});
        assertThat(message).contains("message,")
                           .contains("null=null,")
                           .contains("object=string,")
                           .contains("map={key=value},")
                           .contains("object[]=[1, string, null],")
                           .contains("int[]=[1, 2, 3],")
                           .contains("boolean[]=[true],")
                           .contains("char[]=[1, 2]");

        assertThat(filter.format("message", (Object[]) null)).isEqualTo("message");
        assertThat(filter.format("message")).isEqualTo("message");
    }

    @Test
    void truncate() {
        var message = "1234567890";
        String value = filter.truncate(message, 5);
        assertThat(value).isEqualTo("12345...(truncated)");
    }
}
