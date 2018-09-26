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
    void appendWithNullMessage() {
        var builder = new StringBuilder();
        filter.append(builder, null, 1, 2, 3);
        assertThat(builder.toString()).isEqualTo("null");
    }

    @Test
    void appendWithNullArguments() {
        var builder = new StringBuilder();
        filter.append(builder, "message", (Object[]) null);
        assertThat(builder.toString()).isEqualTo("message");
    }

    @Test
    void appendWithRedundantArguments() {
        var builder = new StringBuilder();
        filter.append(builder, "message-{}-{}", 1, 2, 3, 4);
        assertThat(builder.toString()).isEqualTo("message-1-2");
    }

    @Test
    void appendWithLessArguments() {
        var builder = new StringBuilder();
        filter.append(builder, "message-{}-{}", 1);
        assertThat(builder.toString()).isEqualTo("message-1-{}");

        builder = new StringBuilder();
        filter.append(builder, "message-{}-{}");
        assertThat(builder.toString()).isEqualTo("message-{}-{}");
    }

    @Test
    void append() {
        var builder = new StringBuilder();
        filter.append(builder, "{}", 1);
        assertThat(builder.toString()).isEqualTo("1");

        builder = new StringBuilder();
        filter.append(builder, "{}-text", 1);
        assertThat(builder.toString()).isEqualTo("1-text");

        builder = new StringBuilder();
        filter.append(builder, "message, null={}, object={}, map={}, object[]={}, int[]={}, boolean[]={}, char[]={}", null,
                "string",
                Map.of("key", "value"),
                new Object[]{1, "string", null},
                new int[]{1, 2, 3},
                new boolean[]{true},
                new char[]{'1', '2'});
        assertThat(builder.toString()).contains("message,")
                                      .contains("null=null,")
                                      .contains("object=string,")
                                      .contains("map={key=value},")
                                      .contains("object[]=[1, string, null],")
                                      .contains("int[]=[1, 2, 3],")
                                      .contains("boolean[]=[true],")
                                      .contains("char[]=[1, 2]");
    }

    @Test
    void truncate() {
        var builder = new StringBuilder();
        filter.truncate(builder, "1234567890", 5);
        assertThat(builder.toString()).isEqualTo("12345...(truncated)");
    }
}
